package com.onlineshop.framework.models.order.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.cart.ClearCartEvent;
import com.onlineshop.framework.event.order.OrderTimeoutCancelEvent;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.address.Address;
import com.onlineshop.framework.models.address.IAddressService;
import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.order.dto.*;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.entity.OrderItem;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.enums.OrderType;
import com.onlineshop.framework.models.order.enums.ParentOrderStatus;
import com.onlineshop.framework.models.order.mapper.OrderMapper;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.order.state.OrderStatusMachine;
import com.onlineshop.framework.models.order.strategy.OrderCreateStrategy;
import com.onlineshop.framework.models.order.strategy.OrderStrategyContext;
import com.onlineshop.framework.models.order.vo.OrderAggregateVO;
import com.onlineshop.framework.models.order.vo.OrderVO;
import com.onlineshop.framework.models.order.vo.StoreOrderItemVO;
import com.onlineshop.framework.models.order.vo.StoreOrderVO;
import com.onlineshop.framework.models.order.wrapper.OrderQueryWrapper;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.user.UserRole;
import com.onlineshop.framework.utils.DateTimeUtil;
import com.onlineshop.framework.utils.context.UserContextHolder;
import com.onlineshop.framework.utils.money.Money;
import com.onlineshop.framework.utils.money.MoneyUtil;
import io.micrometer.common.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author : Tomatos
 * @date : 2025/12/24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    private final OrderStrategyContext orderStrategyContext;
    private final IOrderItemService orderItemService;
    private final IAddressService addressService;
    private final IStoreService storeService;
    private final IGoodsService goodsService;
    private final IGoodsSkuService goodsSkuService;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 用户端：分页查询聚合订单（查询父订单和普通订单，并聚合子订单和商品明细）
     *
     * @param queryDTO 查询条件
     * @return 订单聚合视图分页结果
     */
    @Override
    public IPage<OrderAggregateVO> pageQueryForUser(OrderQueryDTO queryDTO) {
        UserRole role = UserRole.of(UserContextHolder.getUserRoleCode());

        Page<Order> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        LambdaQueryWrapper<Order> wrapper = OrderQueryWrapper.build(role, queryDTO, null);

        IPage<Order> orderPage = page(page, wrapper);
        filterIfOrderStatusIsSpecified(queryDTO, orderPage);
        return orderPage.convert(this::createOrderAggregateVO);
    }

    private static void filterIfOrderStatusIsSpecified(
            OrderQueryDTO queryDTO,
            IPage<Order> orderPage
    ) {
        if (StringUtils.isNotBlank(queryDTO.getStatus())) {
            log.debug("按状态过滤订单, status: {}", queryDTO.getStatus());
            orderPage.getRecords()
                     .removeIf(order ->
                                       !order.getStatus()
                                             .equals(queryDTO.getStatus())
                     );
        }
    }

    /**
     * 构建订单聚合视图
     * 用于前端展示：包含订单基本信息 + 店铺订单列表 + 商品明细
     *
     * @param topOrder 顶层订单（父订单或普通订单）
     * @return 订单聚合视图
     */
    private OrderAggregateVO createOrderAggregateVO(Order topOrder) {
        List<StoreOrderVO> storeOrders = new ArrayList<>();
        if (OrderType.PARENT.getCode()
                            .equals(topOrder.getOrderType())) {
            List<Order> subOrders = querySubOrder(topOrder.getId());

            List<Long> storeIds = subOrders.stream()
                                           .map(Order::getStoreId)
                                           .distinct()
                                           .collect(Collectors.toList());

            List<Store> stores = storeService.listByIds(storeIds);
            Map<Long, String> storeNameMap = stores.stream()
                                                   .collect(
                                                           Collectors.toMap(Store::getId,
                                                                            Store::getName)
                                                   );

            // 一个店铺对应一个子订单
            for (Order subOrder : subOrders) {
                // 查询订单商品明细
                List<StoreOrderItemVO> items = orderItemService.lambdaQuery()
                                                               .eq(OrderItem::getOrderId,
                                                                   subOrder.getId())
                                                               .list()
                                                               .stream()
                                                               .map(this::buildStoreOrderItemVO)
                                                               .toList();

                StoreOrderVO storeOrderVO = buildStoreOrderVO(
                        items,
                        subOrder,
                        storeNameMap.get(subOrder.getStoreId())
                );
                storeOrders.add(storeOrderVO);
            }
        } else {
            Store store = storeService.getById(topOrder.getStoreId());
            List<StoreOrderItemVO> items = orderItemService.lambdaQuery()
                                                           .eq(OrderItem::getOrderId,
                                                               topOrder.getId())
                                                           .list()
                                                           .stream()
                                                           .map(this::buildStoreOrderItemVO)
                                                           .toList();
            StoreOrderVO shopOrderVO = buildStoreOrderVO(items, topOrder, store.getName());
            storeOrders.add(shopOrderVO);
        }

        return buildOrderAggregateVO(topOrder, storeOrders);
    }

    private List<Order> querySubOrder(Long id) {
        return lambdaQuery().eq(Order::getParentId, id)
                            .list();
    }

    private StoreOrderItemVO buildStoreOrderItemVO(OrderItem item) {
        // 解析SKU规格快照（格式：颜色=黑色;尺码=L）
        Map<String, String> selectedSpecs = parseSkuSpecs(item.getSkuSpecs());

        return StoreOrderItemVO.builder()
                               .orderItemId(item.getId())
                               .goodsId(item.getGoodsId())
                               .goodsName(item.getGoodsName())
                               .goodsMainImageUrl(item.getGoodsMainImageUrl())
                               .goodsPrice(
                                       Money.ofCents(item.getGoodsPrice())
                                            .toYuanString()
                               )
                               .quantity(item.getQuantity())
                               .totalPrice(
                                       Money.ofCents(item.getTotalPrice())
                                            .toYuanString()
                               )
                               .commentStatus(item.getCommentStatus())
                               .selectedSpecs(selectedSpecs)
                               .build();
    }

    /**
     * 构建店铺订单VO
     *
     * @param order     订单
     * @param storeName 店铺名称
     * @return 店铺订单VO
     */
    private StoreOrderVO buildStoreOrderVO(
            List<StoreOrderItemVO> items,
            Order order,
            String storeName
    ) {
        return StoreOrderVO.builder()
                           .orderNo(order.getNo())
                           .storeId(order.getStoreId())
                           .storeName(storeName)
                           .status(order.getStatus())
                           .items(items)
                           .totalPrice(calculateOrderTotalPrice(items))
                           .count(calculateOrderGoodsTotalNum(items))
                           .build();
    }

    private OrderAggregateVO buildOrderAggregateVO(Order topOrder, List<StoreOrderVO> storeOrders) {
        return OrderAggregateVO.builder()
                               .orderNo(topOrder.getNo())
                               .status(topOrder.getStatus())
                               .createTime(topOrder.getCreateTime())
                               .expireTime(topOrder.getCreateTime()
                                                   .plusMinutes(30L))
                               .reason(topOrder.getReason())
                               .storeOrders(storeOrders)
                               .totalPrice(calculateAggregateOrderTotalPrice(storeOrders))
                               .count(calculateAggregateOrderGoodsTotalNum(storeOrders))
                               .build();
    }

    /**
     * 解析SKU规格快照字符串
     * 格式：颜色=黑色;尺码=L
     *
     * @param skuSpecs SKU规格快照字符串
     * @return 规格Map，key: 规格名称，value: 规格值名称
     */
    private Map<String, String> parseSkuSpecs(String skuSpecs) {
        if (skuSpecs == null || skuSpecs.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> specMap = new java.util.LinkedHashMap<>();
        String[] specPairs = skuSpecs.split(";");

        for (String pair : specPairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                specMap.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return specMap;
    }

    private String calculateOrderTotalPrice(List<StoreOrderItemVO> items) {
        List<Money> monies = items.stream()
                                  .map(item -> Money.ofYuan(item.getTotalPrice()))
                                  .toList();
        return MoneyUtil.sum(monies)
                        .toYuanString();
    }

    private Long calculateOrderGoodsTotalNum(List<StoreOrderItemVO> items) {
        return items.stream()
                    .mapToLong(StoreOrderItemVO::getQuantity)
                    .sum();
    }

    private String calculateAggregateOrderTotalPrice(List<StoreOrderVO> storeOrders) {
        List<Money> monies = storeOrders.stream()
                                        .map(order -> Money.ofYuan(order.getTotalPrice()))
                                        .toList();
        return MoneyUtil.sum(monies)
                        .toYuanString();
    }

    private Long calculateAggregateOrderGoodsTotalNum(List<StoreOrderVO> items) {
        return items.stream()
                    .mapToLong(StoreOrderVO::getCount)
                    .sum();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResultDTO createOrder(TradeDTO tradeDTO, CartType cartType) {
        Address address = loadValidateAddress(tradeDTO);

        orderStrategyContext.validate(cartType, tradeDTO);
        OrderCreateStrategy.OrderBuildResult orderBuildResult = orderStrategyContext.buildOrders(
                cartType, tradeDTO, address
        );

        OrderCreateResultDTO result = processOrderAggregate(orderBuildResult);
        log.info("订单创建成功, orderNo: {}", result.getOrderNo());
        pushCleanCartGoodsEvent(tradeDTO.getTradeItems());
        pushOrderTimeoutCancelEvent(orderBuildResult.getPayOrder());
        return result;
    }

    private @NonNull Address loadValidateAddress(TradeDTO tradeDTO) {
        Address address = getAddress(tradeDTO);
        validateAddress(address);
        return address;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResultDTO processOrderAggregate(
            @NonNull OrderCreateStrategy.OrderBuildResult buildResults
    ) {
        Order payOrder = buildResults.getPayOrder();
        savePayOrder(payOrder);
        List<Order> orders = buildResults.getSubOrders()
                                         .stream()
                                         .peek(build ->
                                                       build.getOrder()
                                                            .setParentId(payOrder.getId())
                                         )
                                         .map(OrderCreateStrategy.RawOrderBuild::getOrder)
                                         .toList();
        saveOrders(orders);

        List<OrderItem> orderItems = buildResults.getSubOrders()
                                                 .stream()
                                                 .peek(this::supplementOrderItemOrderId)
                                                 .map(OrderCreateStrategy.RawOrderBuild::getOrderItems)
                                                 .flatMap(List::stream)
                                                 .toList();
        saveOrderItems(orderItems);
        return OrderCreateResultDTO.builder()
                                   .orderNo(payOrder.getNo())
                                   .expireTime(
                                           payOrder.getCreateTime()
                                                   .plusMinutes(30)
                                   )
                                   .build();
    }

    private Address getAddress(TradeDTO tradeDTO) {
        Long userId = UserContextHolder.getUserId();
        return addressService.lambdaQuery()
                             .eq(Address::getId, tradeDTO.getAddressId())
                             .eq(Address::getUserId, userId)
                             .one();
    }

    private void validateAddress(Address address) {
        if (address == null) {
            throw new BusinessException(BizErrorCode.ADDRESS_NOT_EXIST);
        }
    }

    private void savePayOrder(Order payOrder) {
        if (!OrderType.PARENT.getCode()
                             .equals(payOrder.getOrderType())) {
            return;
        }

        if (!this.save(payOrder)) {
            log.error("支付订单保存失败");
            throw new BusinessException(BizErrorCode.ORDER_CREATE_FAILED);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrders(List<Order> orders) {
        if (CollectionUtil.isEmpty(orders)) {
            return;
        }

        if (!saveBatch(orders)) {
            log.error("订单保存失败");
            throw new BusinessException(BizErrorCode.ORDER_CREATE_FAILED);
        }
    }

    private void supplementOrderItemOrderId(OrderCreateStrategy.RawOrderBuild orderBuild) {
        Order order = orderBuild.getOrder();
        for (OrderItem orderItem : orderBuild.getOrderItems()) {
            orderItem.setOrderId(order.getId());
        }
    }

    private void saveOrderItems(List<OrderItem> orderItems) {
        if (!orderItemService.saveBatchItems(orderItems)) {
            log.error("订单明细保存失败");
            throw new BusinessException(BizErrorCode.ORDER_CREATE_FAILED);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean queryPaymentStatus(String orderNo) {
        try {
            // 模拟支付查询延迟（休眠2秒）
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("支付状态查询被中断, orderNo: {}", orderNo, e);
            Thread.currentThread()
                  .interrupt();
        }

        // 模拟支付成功，实际项目中应该调用真实的支付平台API
        log.info("支付成功（模拟）, orderNo: {}, 支付状态: 成功", orderNo);
        Order order = queryOrderByOrderNo(orderNo);
        validateOrder(order);
        return syncUpdateOrderStatus(order, OrderStatus.PAID);
    }

    private Order queryOrderByOrderNo(String orderNo) {
        return lambdaQuery().eq(Order::getNo, orderNo)
                            .one();
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new BusinessException(BizErrorCode.ORDER_NOT_EXIST);
        }
    }

    private boolean syncUpdateOrderStatus(Order order, OrderStatus newStatus) {
        String oldStatus = order.getStatus();
        // 验证状态迁移的合法性
        if (!OrderStatusMachine.validateTransition(OrderStatus.of(oldStatus), newStatus)) {
            return false;
        }

        order.setStatus(newStatus.getCode());
        syncUpdateParentOrSubOrderStatus(order);
        return lambdaUpdate().eq(Order::getStatus, oldStatus)
                             .eq(Order::getId, order.getId())
                             .set(Order::getStatus, newStatus)
                             .set(StrUtil.isNotBlank(order.getReason()), Order::getReason,
                                  order.getReason())
                             .update();
    }

    private void syncUpdateParentOrSubOrderStatus(Order order) {
        switch (OrderType.of(order.getOrderType())) {
            case PARENT -> syncSubOrderStatus(order);
            case SUB -> syncParentOrderStatus(order);
        }
    }

    private void syncSubOrderStatus(Order order) {
        List<Order> subOrders = querySubOrder(order.getId());
        for (Order subOrder : subOrders) {
            subOrder.setStatus(order.getStatus());
        }
        this.updateBatchById(subOrders);
    }

    private void syncParentOrderStatus(Order order) {
        Order parentOrder = queryParentOrder(order.getParentId());
        boolean allSameStatus = querySubOrder(parentOrder.getId()).stream()
                                                                  .allMatch(o ->
                                                                                    o.getStatus()
                                                                                     .equals(order.getStatus())
                                                                  );
        parentOrder.setStatus(
                allSameStatus ? order.getStatus() : ParentOrderStatus.PROCESSING.getCode()
        );
        this.updateById(parentOrder);
    }

    private @NonNull Order queryParentOrder(Long parentId) {
        return lambdaQuery().eq(Order::getId, parentId)
                            .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductInventoryByOrderNo(String orderNo) {
        List<OrderItem> orderItems = getOrderItemsByOrderNo(orderNo);
        if (CollectionUtil.isEmpty(orderItems)) {
            log.warn("订单明细为空，无需扣减库存, orderNo: {}", orderNo);
            return;
        }

        // 遍历订单明细，对每个SKU扣减库存并增加销量
        for (OrderItem orderItem : orderItems) {
            Long skuId = orderItem.getSkuId();
            Integer quantity = orderItem.getQuantity();

            // 扣减SKU库存并增加销量
            goodsSkuService.deductInventoryAndIncreaseSales(skuId, quantity);
            // 增加商品销量
            goodsService.increaseSales(orderItem.getGoodsId(), quantity);
        }
        log.info("订单库存扣减和销量更新完成, orderNo: {}, 明细数量: {}", orderNo,
                 orderItems.size());
    }

    @Override
    public OrderAggregateVO getOrderDetailByOrderNo(String orderNo) {
        Order order = queryOrderByOrderNo(orderNo);

        if (OrderType.PARENT == OrderType.of(order.getOrderType())) {
            List<StoreOrderVO> storeOrderVOS = new ArrayList<>();
            List<Order> subOrders = querySubOrder(order.getId());
            List<Long> storeId = subOrders.stream()
                                          .map(Order::getStoreId)
                                          .toList();
            Map<Long, String> storeIdMap = storeService.lambdaQuery()
                                                       .in(Store::getId, storeId)
                                                       .list()
                                                       .stream()
                                                       .collect(Collectors.toMap(Store::getId,
                                                                                 Store::getName));

            for (Order subOrder : subOrders) {
                List<StoreOrderItemVO> storeOrderItemVOS = orderItemService.listByOrderId(
                                                                                   subOrder.getId())
                                                                           .stream()
                                                                           .map(this::buildStoreOrderItemVO)
                                                                           .toList();

                StoreOrderVO storeOrderVO = buildStoreOrderVO(
                        storeOrderItemVOS,
                        subOrder,
                        storeIdMap.get(subOrder.getStoreId())
                );
                storeOrderVOS.add(storeOrderVO);
            }
            return buildOrderAggregateVO(order, storeOrderVOS);
        }

        Store store = storeService.getById(order.getStoreId());
        List<StoreOrderItemVO> storeOrderItemVOS = orderItemService.listByOrderId(order.getId())
                                                                   .stream()
                                                                   .map(this::buildStoreOrderItemVO)
                                                                   .toList();
        StoreOrderVO storeOrderVO = buildStoreOrderVO(storeOrderItemVOS, order, store.getName());
        return buildOrderAggregateVO(order, Collections.singletonList(storeOrderVO));
    }

    /**
     * 关闭订单接口 - DTO版本
     * 功能：关闭订单，将订单状态改为 CLOSED
     * 支持多种场景（用户、商家、管理员等）
     * - 如果 closeDTO.userId 为 null，则使用当前登录用户ID
     * - 如果 closeDTO.userId 不为 null，则为指定用户的订单
     *
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean closeOrder(Order order) {
        return syncUpdateOrderStatus(order, OrderStatus.CLOSED);
    }

    /**
     * 取消订单接口 - DTO版本
     * 功能：取消订单，将订单状态从 CREATED 改为 CANCELED
     * 支持用户端、商家端、管理员等多种场景
     * - 如果 cancelDTO.userId 为 null，则使用当前登录用户ID
     * - 如果 cancelDTO.userId 不为 null，则为指定用户的订单
     *
     * @param cancelDTO 取消订单DTO
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelOrder(OrderCancelDTO cancelDTO) {
        Long userId = UserContextHolder.getUserId();
        log.info("取消订单, orderNo: {}, userId: {}", cancelDTO.getOrderNo(), userId);
        Order order = queryOrder(cancelDTO.getOrderNo(), userId);
        order.setReason(cancelDTO.getReason());

        validateOrder(order);
        return syncUpdateOrderStatus(order, OrderStatus.CANCELED);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean finishOrder(String orderNo) {
        Order order = queryUserOrderByOrderNo(orderNo);
        validateOrder(order);
        if (syncUpdateOrderStatus(order, OrderStatus.FINISHED)) {
            deductInventoryByOrderNo(orderNo);
            return true;
        }
        return false;
    }

    @Override
    public IPage<OrderVO> pageQueryForAdmin(OrderQueryDTO queryDTO) {
        UserRole role = UserRole.of(UserContextHolder.getUserRoleCode());
        // 构建查询条件（管理员可以查询所有订单）
        LambdaQueryWrapper<Order> wrapper = OrderQueryWrapper.build(role, queryDTO, null);

        // 执行分页查询
        Page<Order> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        IPage<Order> orderPage = this.page(page, wrapper);

        // 转换为OrderVO
        return orderPage.convert(OrderVO::buildOrderVO);
    }

    @Override
    public IPage<OrderVO> pageQueryForMerchant(OrderQueryDTO queryDTO) {
        List<Store> stores = queryUserStore();
        validateStore(stores);

        // 构建查询条件（商家只能查询自己店铺的订单）
        LambdaQueryWrapper<Order> wrapper = OrderQueryWrapper.build(UserRole.MERCHANT, queryDTO,
                                                                    stores);
        wrapper.orderByDesc(Order::getCreateTime);

        // 执行分页查询
        Page<Order> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        IPage<Order> orderPage = this.page(page, wrapper);

        return orderPage.convert(OrderVO::buildOrderVO);
    }

    @Override
    public boolean shipOrderMerchant(String orderNo) {
        List<Store> stores = queryUserStore();
        validateStore(stores);

        Order order = queryStoreOrder(orderNo, stores);
        validateStoreOrder(order);
        return syncUpdateOrderStatus(order, OrderStatus.SHIPPED);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelOrderMerchant(OrderCancelDTO cancelDTO) {
        List<Store> storeList = queryUserStore();
        validateStore(storeList);

        Order order = queryStoreOrder(cancelDTO.getOrderNo(), storeList);
        validateStoreOrder(order);
        return cancelOrder(cancelDTO);
    }

    @Override
    public OrderVO getOrderCommentMerchant(String orderNo) {
        List<Store> stores = queryUserStore();
        Order order = queryStoreOrder(orderNo, stores);
        validateStoreOrder(order);
        return OrderVO.buildOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoReceiveOrder(Order order) {
        if (
                !DateTimeUtil.isExpired(order.getCreateTime()
                                             .plusMinutes(30L))
                        || !OrderStatus.SHIPPED.getCode()
                                               .equals(order.getStatus())
        ) {
            return false;
        }
        return syncUpdateOrderStatus(order, OrderStatus.FINISHED);
    }

    private Order queryOrder(String orderNo, Long userId) {
        return lambdaQuery().eq(Order::getNo, orderNo)
                            .eq(Order::getUserId, userId)
                            .one();
    }

    private Order queryStoreOrder(String orderNo, List<Store> stores) {
        List<Long> storeIds = stores.stream()
                                    .map(Store::getId)
                                    .toList();

        return this.lambdaQuery()
                   .eq(Order::getNo, orderNo)
                   .in(Order::getStoreId, storeIds)
                   .one();
    }

    private static void validateStoreOrder(Order order) {
        if (order == null) {
            throw new BusinessException(BizErrorCode.ORDER_NOT_EXIST);
        }
    }

    private List<Store> queryUserStore() {
        return storeService.lambdaQuery()
                           .eq(Store::getUserId, UserContextHolder.getUserId())
                           .list();
    }

    private static void validateStore(List<Store> storeList) {
        if (CollectionUtil.isEmpty(storeList)) {
            throw new BusinessException(BizErrorCode.MERCHANT_NO_SHOP);
        }
    }

    private Order queryUserOrderByOrderNo(String orderNo) {
        return lambdaQuery().eq(Order::getNo, orderNo)
                            .eq(Order::getUserId, UserContextHolder.getUserId())
                            .one();
    }

    public List<OrderItem> getOrderItemsByOrderNo(String orderNo) {
        Order order = lambdaQuery().eq(Order::getNo, orderNo)
                                   .one();
        List<Long> orderIds = Collections.singletonList(order.getId());

        if (OrderType.PARENT == OrderType.of(order.getOrderType())) {
            orderIds = lambdaQuery().eq(Order::getParentId, order.getId())
                                    .list()
                                    .stream()
                                    .map(Order::getId)
                                    .toList();
        }

        return orderItemService.lambdaQuery()
                               .in(OrderItem::getOrderId, orderIds)
                               .list();
    }

    private void pushCleanCartGoodsEvent(List<TradeShopDTO> tradeItems) {
        List<Long> skuIds = tradeItems.stream()
                                      .map(TradeShopDTO::getTradeShopItemList)
                                      .flatMap(Collection::stream)
                                      .map(TradeShopItemDTO::getSkuId)
                                      .toList();

        applicationEventPublisher.publishEvent(
                ClearCartEvent.builder()
                              .skuIds(skuIds)
                              .build()
        );
    }

    private void pushOrderTimeoutCancelEvent(Order payOrder) {
        applicationEventPublisher.publishEvent(
                OrderTimeoutCancelEvent.builder()
                                       .orderId(payOrder.getId())
                                       .orderNo(payOrder.getNo())
                                       .build()
        );
    }
}
