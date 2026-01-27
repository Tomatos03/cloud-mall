package com.onlineshop.framework.models.order.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.cart.ClearCartEvent;
import com.onlineshop.framework.event.order.OrderCreatedEvent;
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
            List<Order> subOrders = querySubOrder(topOrder);

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

    private List<Order> querySubOrder(Order order) {
        return lambdaQuery().eq(Order::getParentId, order.getId())
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
                cartType, tradeDTO
        );
        OrderCreateResultDTO result = processOrderAggregate(orderBuildResult, address);
        log.info("订单创建成功, orderNo: {}", result.getOrderNo());
        pushCleanCartGoodsEvent(tradeDTO.getTradeItems());
        return result;
    }

    private @NonNull Address loadValidateAddress(TradeDTO tradeDTO) {
        Address address = getAddress(tradeDTO);
        validateAddress(address);
        return address;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResultDTO processOrderAggregate(
            @NonNull OrderCreateStrategy.OrderBuildResult buildResults,
            Address address
    ) {
        savePayOrder(buildResults);
        Order payOrder = buildResults.getPayOrder();
        List<Order> orders = buildResults.getSubOrders()
                                         .stream()
                                         .map(OrderCreateStrategy.RawOrderBuild::getOrder)
                                         .toList();
        fillAddressInfo(orders, address);
        saveOrders(orders);

        bindOrderId(buildResults.getSubOrders());
        List<OrderItem> orderItems = buildResults.getSubOrders()
                                                 .stream()
                                                 .map(OrderCreateStrategy.RawOrderBuild::getOrderItems)
                                                 .flatMap(List::stream)
                                                 .toList();
        saveOrderItems(orderItems);

        applicationEventPublisher.publishEvent(
                OrderCreatedEvent.builder()
                                 .orderId(payOrder.getId())
                                 .orderNo(payOrder.getNo())
                                 .build()
        );

        return OrderCreateResultDTO.builder()
                                   .orderNo(payOrder.getNo())
                                   .expireTime(payOrder.getCreateTime()
                                                       .plusMinutes(30)
                                   )
                                   .build();
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

    private void savePayOrder(OrderCreateStrategy.OrderBuildResult buildResult) {
        Order payOrder = buildResult.getPayOrder();
        if (!OrderType.PARENT.getCode()
                             .equals(payOrder.getOrderType())) {
            return;
        }

        if (!this.save(payOrder)) {
            log.error("支付订单保存失败");
            throw new BusinessException(BizErrorCode.ORDER_CREATE_FAILED);
        }
    }

    private void fillAddressInfo(List<Order> orders, Address address) {
        for (Order order : orders) {
            order.setUserName(address.getReceiver());
            order.setPhone(address.getPhone());
            order.setAddress(address.getFullAddress() + "/" + address.getDetail());
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

    private void bindOrderId(List<OrderCreateStrategy.RawOrderBuild> orderBuilds) {
        for (OrderCreateStrategy.RawOrderBuild orderBuild : orderBuilds) {
            Order order = orderBuild.getOrder();
            for (OrderItem orderItem : orderBuild.getOrderItems()) {
                orderItem.setOrderId(order.getId());
            }
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

    //    /**
    //     * 创建父订单
    //     *
    //     * @param buildResults 订单构建结果列表
    //     * @param address      收货地址
    //     * @return 父订单
    //     */
    //    private Order createParentOrder(
    //            List<OrderCreateStrategy.OrderBuildResult> buildResults,
    //            Address address
    //    ) {
    //        // 计算所有子订单的总价
    //        long totalPrice = buildResults.stream()
    //                                      .mapToLong(r -> r.getOrder()
    //                                                       .getTotalPrice())
    //                                      .sum();
    //
    //        return Order.builder()
    //                    .no(OrderNoUtil.generateParentOrderNo())
    //                    .userId(UserContextHolder.getUserId())
    //                    .totalPrice(totalPrice)
    //                    .quantity(buildResults.size())  // 子订单数量
    //                    .status(OrderStatus.CREATED.getCode())
    //                    .orderType(OrderType.PARENT.getCode())
    //                    .userName(address.getReceiver())
    //                    .address(address.getFullAddress() + "/" + address.getDetail())
    //                    .phone(address.getPhone())
    //                    .build();
    //    }

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
        if (OrderStatusMachine.validateTransition(OrderStatus.of(oldStatus), newStatus)) {
            return false;
        }

        order.setStatus(newStatus.getCode());
        syncUpdateParentOrSubOrderStatus(order);
        // 乐观锁
        return lambdaUpdate().eq(Order::getStatus, oldStatus)
                             .set(Order::getStatus, newStatus)
                             .set(StrUtil.isNotBlank(order.getReason()), Order::getReason,
                                  order.getReason())
                             .update();
    }

    private void syncUpdateParentOrSubOrderStatus(Order order) {
        if (OrderType.PARENT == OrderType.of(order.getOrderType())) {
            syncSubOrderStatus(order);
        } else {
            syncParentOrderStatus(order);
        }
    }

    private void syncSubOrderStatus(Order order) {
        List<Order> subOrders = querySubOrder(order);
        for (Order subOrder : subOrders) {
            subOrder.setStatus(order.getStatus());
        }
        this.updateBatchById(subOrders);
    }

    private void syncParentOrderStatus(Order order) {
        Order parentOrder = queryParentOrder(order);
        if (parentOrder == null) {
            return;
        }
        List<Order> subOrder = querySubOrder(parentOrder);
        boolean allSameStatus = subOrder.stream()
                                        .allMatch(o -> o.getStatus()
                                                        .equals(order.getStatus()));
        parentOrder.setStatus(
                allSameStatus ? order.getStatus() : ParentOrderStatus.PROCESSING.getCode());
        this.updateById(parentOrder);
    }

    private Order queryParentOrder(Order order) {
        return lambdaQuery().eq(Order::getId, order.getParentId())
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
            List<Order> subOrders = querySubOrder(order);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelOrder(String orderNo) {
        return cancelOrder(orderNo, UserContextHolder.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelOrder(String orderNo, Long userId) {
        log.info("取消订单, orderNo: {}, userId: {}", orderNo, userId);
        Order order = queryOrderByOrderNoAndUserId(orderNo, userId);
        validateOrder(order);
        return syncUpdateOrderStatus(order, OrderStatus.CANCELED);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean closeOrder(Order order) {
        return syncUpdateOrderStatus(order, OrderStatus.CLOSED);
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
    public boolean finishOrder(Order order) {
        return syncUpdateOrderStatus(order, OrderStatus.FINISHED);
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
    public boolean cancelOrderMerchant(String orderNo) {
        List<Store> storeList = queryUserStore();
        validateStore(storeList);

        Order order = queryStoreOrder(orderNo, storeList);
        validateStoreOrder(order);
        return this.cancelOrder(orderNo);
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

    private Order queryOrderByOrderNoAndUserId(String orderNo, Long userId) {
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

    /**
     * 批量同步更新订单状态（按订单号）
     * 根据订单号列表批量更新订单状态
     *
     * @param orderNos 订单号列表
     * @param newStatus 新状态
     * @return 成功更新的订单数
     */
    // TODO: ai 生成需要code_view
    @Transactional(rollbackFor = Exception.class)
    public int batchSyncUpdateOrderStatusByOrderNos(List<String> orderNos, OrderStatus newStatus) {
        if (CollectionUtil.isEmpty(orderNos)) {
            log.warn("批量更新订单状态: 订单号列表为空");
            return 0;
        }

        List<Order> orders = lambdaQuery().in(Order::getNo, orderNos)
                                          .list();

        if (CollectionUtil.isEmpty(orders)) {
            log.warn("批量更新订单状态: 未找到匹配的订单, orderNos: {}", orderNos);
            return 0;
        }

        return batchSyncUpdateOrderStatus(orders, newStatus);
    }

    /**
     * 批量同步更新订单状态
     * 支持批量更新订单及其关联的父/子订单状态
     *
     * @param orders 待更新的订单列表
     * @param newStatus 新状态
     * @return 成功更新的订单数
     */
    private int batchSyncUpdateOrderStatus(List<Order> orders, OrderStatus newStatus) {
        if (CollectionUtil.isEmpty(orders)) {
            log.warn("批量更新订单状态: 订单列表为空");
            return 0;
        }

        // 验证并过滤所有订单的状态迁移合法性
        List<Order> validOrders = validateAndFilterOrders(orders, newStatus);
        if (CollectionUtil.isEmpty(validOrders)) {
            log.warn("批量更新订单状态: 没有有效的订单需要更新");
            return 0;
        }

        // 准备批量更新数据
        List<Order> ordersToUpdate = prepareBatchUpdateData(validOrders, newStatus);

        // 批量更新订单
        if (!updateBatchById(ordersToUpdate)) {
            log.error("批量更新订单状态失败");
            return 0;
        }

        // 同步更新关联的父/子订单
        batchSyncUpdateParentOrSubOrderStatus(validOrders);

        log.info("批量更新订单状态成功, 更新数量: {}, 新状态: {}", validOrders.size(),
                 newStatus.getCode());
        return validOrders.size();
    }

    /**
     * 验证并过滤订单的状态迁移合法性
     *
     * @param orders 原始订单列表
     * @param newStatus 新状态
     * @return 有效的订单列表
     */
    private List<Order> validateAndFilterOrders(List<Order> orders, OrderStatus newStatus) {
        return orders.stream()
                     .filter(order -> {
                         String oldStatus = order.getStatus();
                         if (OrderStatusMachine.validateTransition(OrderStatus.of(oldStatus),
                                                                   newStatus)) {
                             log.warn("订单状态迁移非法, orderId: {}, oldStatus: {}, newStatus: {}",
                                      order.getId(), oldStatus, newStatus.getCode());
                             return false;
                         }
                         return true;
                     })
                     .collect(Collectors.toList());
    }

    /**
     * 准备批量更新数据
     * 包括主订单、子订单等所有需要更新的订单
     *
     * @param orders 有效的订单列表
     * @param newStatus 新状态
     * @return 所有需要更新的订单列表
     */
    private List<Order> prepareBatchUpdateData(List<Order> orders, OrderStatus newStatus) {
        List<Order> ordersToUpdate = new ArrayList<>();

        for (Order order : orders) {
            order.setStatus(newStatus.getCode());
            ordersToUpdate.add(order);

            // 如果是父订单，同步准备更新所有子订单
            if (OrderType.PARENT == OrderType.of(order.getOrderType())) {
                List<Order> subOrders = querySubOrder(order);
                for (Order subOrder : subOrders) {
                    subOrder.setStatus(newStatus.getCode());
                    ordersToUpdate.add(subOrder);
                }
            }
        }

        return ordersToUpdate;
    }

    /**
     * 批量同步更新父/子订单状态
     * 根据原始订单列表的类型分别处理父订单和子订单
     * 实现真正的批量处理，减少数据库操作次数
     *
     * @param orders 原始有效订单列表
     */
    private void batchSyncUpdateParentOrSubOrderStatus(List<Order> orders) {
        // 按订单类型分组
        Map<String, List<Order>> ordersByType = orders.stream()
                                                      .collect(Collectors.groupingBy(
                                                              Order::getOrderType
                                                      ));

        // 批量处理子订单（当父订单被更新时）
        List<Order> parentOrders = ordersByType.getOrDefault(OrderType.PARENT.getCode(),
                                                             Collections.emptyList());
        if (!parentOrders.isEmpty()) {
            batchSyncSubOrderStatus(parentOrders);
        }

        // 批量处理父订单（当子订单被更新时）
        List<Order> subOrders = ordersByType.getOrDefault(OrderType.SUB.getCode(),
                                                          Collections.emptyList());
        if (!subOrders.isEmpty()) {
            batchSyncParentOrderStatus(subOrders);
        }
    }

    /**
     * 批量同步子订单状态
     * 为一批父订单的所有子订单同步更新状态
     *
     * @param parentOrders 父订单列表
     */
    private void batchSyncSubOrderStatus(List<Order> parentOrders) {
        // 获取所有父订单ID
        List<Long> parentIds = parentOrders.stream()
                                           .map(Order::getId)
                                           .toList();

        // 一次查询获取所有子订单
        List<Order> allSubOrders = lambdaQuery().in(Order::getParentId, parentIds)
                                                .list();

        // 分组统计：按父订单ID分组，方便后续处理
        Map<Long, List<Order>> subOrdersByParentId = allSubOrders.stream()
                                                                 .collect(Collectors.groupingBy(
                                                                         Order::getParentId
                                                                 ));

        // 准备批量更新的子订单列表
        List<Order> subOrdersToUpdate = new ArrayList<>();

        for (Order parentOrder : parentOrders) {
            // 获取该父订单对应的所有子订单
            List<Order> subOrders = subOrdersByParentId.getOrDefault(parentOrder.getId(),
                                                                     Collections.emptyList());

            // 更新子订单的状态为父订单的状态
            for (Order subOrder : subOrders) {
                subOrder.setStatus(parentOrder.getStatus());
                subOrdersToUpdate.add(subOrder);
            }
        }

        // 一次批量更新所有子订单
        if (!subOrdersToUpdate.isEmpty()) {
            updateBatchById(subOrdersToUpdate);
            log.debug("批量同步子订单状态完成, 更新子订单数量: {}", subOrdersToUpdate.size());
        }
    }

    /**
     * 批量同步父订单状态
     * 为一批子订单的父订单同步更新状态
     * 父订单状态规则：
     * - 所有子订单状态相同 => 父订单状态等于子订单状态
     * - 子订单状态不全相同 => 父订单状态为 PROCESSING
     *
     * @param subOrders 子订单列表
     */
    private void batchSyncParentOrderStatus(List<Order> subOrders) {
        // 获取所有不重复的父订单ID
        List<Long> parentIds = subOrders.stream()
                                        .map(Order::getParentId)
                                        .filter(java.util.Objects::nonNull)
                                        .distinct()
                                        .toList();

        if (parentIds.isEmpty()) {
            return;
        }

        // 一次查询获取所有父订单
        List<Order> parentOrders = lambdaQuery().in(Order::getId, parentIds)
                                                .list();

        // 构建父订单和其子订单的映射关系（一次查询）
        Map<Long, List<Order>> childrenByParentId = lambdaQuery().in(Order::getParentId,
                                                                     parentIds)
                                                                 .list()
                                                                 .stream()
                                                                 .collect(Collectors.groupingBy(
                                                                         Order::getParentId
                                                                 ));

        // 准备批量更新的父订单列表
        List<Order> parentOrdersToUpdate = new ArrayList<>();

        for (Order parentOrder : parentOrders) {
            // 获取该父订单的所有子订单
            List<Order> childOrders = childrenByParentId.getOrDefault(parentOrder.getId(),
                                                                      Collections.emptyList());

            if (childOrders.isEmpty()) {
                continue;
            }

            // 检查所有子订单状态是否相同
            String firstStatus = childOrders.get(0)
                                            .getStatus();
            boolean allSameStatus = childOrders.stream()
                                               .allMatch(o -> o.getStatus()
                                                               .equals(firstStatus));

            // 根据子订单状态决定父订单状态
            String newParentStatus = allSameStatus ? firstStatus
                    : ParentOrderStatus.PROCESSING.getCode();

            // 只有当父订单状态需要变更时才标记为更新
            if (!newParentStatus.equals(parentOrder.getStatus())) {
                parentOrder.setStatus(newParentStatus);
                parentOrdersToUpdate.add(parentOrder);
            }
        }

        // 一次批量更新所有需要更新的父订单
        if (!parentOrdersToUpdate.isEmpty()) {
            updateBatchById(parentOrdersToUpdate);
            log.debug("批量同步父订单状态完成, 更新父订单数量: {}", parentOrdersToUpdate.size());
        }
    }
}
