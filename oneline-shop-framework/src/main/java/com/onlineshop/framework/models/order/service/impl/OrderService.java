package com.onlineshop.framework.models.order.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.address.Address;
import com.onlineshop.framework.models.address.IAddressService;
import com.onlineshop.framework.models.cart.CartType;
import com.onlineshop.framework.models.cart.ICartService;
import com.onlineshop.framework.models.cart.dto.CartCacheItemDTO;
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
import com.onlineshop.framework.utils.MoneyUtil;
import com.onlineshop.framework.utils.OrderNoUtil;
import com.onlineshop.framework.utils.context.UserContextHolder;
import io.micrometer.common.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final ICartService cartService;
    private final StringRedisTemplate stringRedisTemplate;


    /**
     * 用户端：分页查询聚合订单（查询父订单和普通订单，并聚合子订单和商品明细）
     *
     * @param queryDTO 查询条件
     * @return 订单聚合视图分页结果
     */
    @Override
    public IPage<OrderAggregateVO> pageQueryForUser(OrderQueryDTO queryDTO) {
        UserRole role = UserRole.of(UserContextHolder.getUserRoleCode());

        Page<Order> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
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
        return StoreOrderItemVO.builder()
                               .goodsId(item.getGoodsId())
                               .goodsName(item.getGoodsName())
                               .goodsImg(item.getGoodsImg())
                               .goodsPrice(item.getGoodsPrice())
                               .goodsPriceText(MoneyUtil.fenToYuan(item.getGoodsPrice()))
                               .quantity(item.getQuantity())
                               .totalPrice(item.getTotalPrice())
                               .totalPriceText(MoneyUtil.fenToYuan(item.getTotalPrice()))
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
        Long totalPrice = calculateOrderTotalPrice(items);
        Long count = calculateOrderGoodsTotalNum(items);
        return StoreOrderVO.builder()
                           .orderNo(order.getNo())
                           .storeId(order.getStoreId())
                           .storeName(storeName)
                           .status(order.getStatus())
                           .items(items)
                           .totalPrice(totalPrice)
                           .totalPriceText(MoneyUtil.fenToYuan(totalPrice))
                           .count(count)
                           .build();
    }

    private OrderAggregateVO buildOrderAggregateVO(Order topOrder, List<StoreOrderVO> storeOrders) {
        Long totalPrice = calculateAggregateOrderTotalPrice(storeOrders);
        Long totalCount = calculateAggregateOrderGoodsTotalNum(storeOrders);

        return OrderAggregateVO.builder()
                               .orderNo(topOrder.getNo())
                               .status(topOrder.getStatus())
                               .createTime(topOrder.getCreateTime())
                               .storeOrders(storeOrders)
                               .totalPrice(totalPrice)
                               .totalPriceText(MoneyUtil.fenToYuan(totalPrice))
                               .count(totalCount)
                               .build();
    }

    private Long calculateOrderTotalPrice(List<StoreOrderItemVO> items) {
        return items.stream()
                    .mapToLong(StoreOrderItemVO::getTotalPrice)
                    .sum();
    }

    private Long calculateOrderGoodsTotalNum(List<StoreOrderItemVO> items) {
        return items.stream()
                    .mapToLong(StoreOrderItemVO::getQuantity)
                    .sum();
    }

    private Long calculateAggregateOrderTotalPrice(List<StoreOrderVO> storeOrders) {
        return storeOrders.stream()
                          .mapToLong(StoreOrderVO::getTotalPrice)
                          .sum();
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
        List<OrderCreateStrategy.OrderBuildResult> buildResults = orderStrategyContext.buildOrders(
                cartType, tradeDTO
        );

        String orderNo = processOrderAggregate(buildResults, address);

        log.debug("订单创建成功, orderNo: {}", orderNo);

        cleanCartGoods(tradeDTO.getTradeItems());
        return OrderCreateResultDTO.builder()
                                   .orderNo(orderNo)
                                   .build();
    }

    private @NonNull Address loadValidateAddress(TradeDTO tradeDTO) {
        Address address = getAddress(tradeDTO);
        validateAddress(address);
        return address;
    }

    @Transactional(rollbackFor = Exception.class)
    public String processOrderAggregate(
            List<OrderCreateStrategy.OrderBuildResult> buildResults,
            Address address
    ) {
        validateOrderBuildResult(buildResults);
        Order parentOrder = buildParentOrderIfNecessary(buildResults, address);
        saveParentOrderIfExist(parentOrder);

        List<Order> orders = buildAndAssembleOrders(address, buildResults, parentOrder);
        saveOrders(orders);

        List<OrderItem> orderItems = buildOrderItemsAndBindOrderId(buildResults, orders);
        saveOrderItems(orderItems);

        return extractOrderNo(parentOrder, orders);
    }

    private void cleanCartGoods(List<TradeShopDTO> tradeItems) {
        List<CartCacheItemDTO> itemList = new ArrayList<>();
        for (TradeShopDTO tradeItem : tradeItems) {
            List<TradeShopItemDTO> tradeShopItemList = tradeItem.getTradeShopItemList();
            for (TradeShopItemDTO tradeShopItem : tradeShopItemList) {
                CartCacheItemDTO cartCacheItemDTO = new CartCacheItemDTO();
                cartCacheItemDTO.setGoodsId(tradeShopItem.getGoodsId());
                cartCacheItemDTO.setStoreId(tradeItem.getStoreId());

                itemList.add(cartCacheItemDTO);
            }
        }
        cartService.removeCartItems(itemList);
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

    private void validateOrderBuildResult(List<OrderCreateStrategy.OrderBuildResult> buildResults) {
        if (buildResults == null || buildResults.isEmpty()) {
            throw new BusinessException(BizErrorCode.ORDER_DATA_IS_NULL);
        }
    }

    private Order buildParentOrderIfNecessary(
            List<OrderCreateStrategy.OrderBuildResult> buildResults,
            Address address
    ) {
        Order parentOrder = null;
        if (buildResults.size() > 1) {
            parentOrder = createParentOrder(buildResults, address);
            log.debug("父订单创建成功, parentOrderId: {}, parentOrderNo: {}, 子订单数量: {}",
                      parentOrder.getId(), parentOrder.getNo(), buildResults.size());
        }
        return parentOrder;
    }

    private void saveParentOrderIfExist(Order parentOrder) {
        if (parentOrder == null) {
            return;
        }
        if (!save(parentOrder)) {
            log.error("父订单保存失败");
            throw new BusinessException(BizErrorCode.ORDER_CREATE_FAILED);
        }
    }

    private List<Order> buildAndAssembleOrders(
            Address address,
            List<OrderCreateStrategy.OrderBuildResult> buildResults,
            Order parentOrder
    ) {
        return buildResults.stream()
                           .map(OrderCreateStrategy.OrderBuildResult::getOrder)
                           .peek(order -> assembleOrder(address, order, parentOrder))
                           .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrders(List<Order> orders) {
        if (!saveBatch(orders)) {
            log.error("订单保存失败");
            throw new BusinessException(BizErrorCode.ORDER_CREATE_FAILED);
        }
    }

    private List<OrderItem> buildOrderItemsAndBindOrderId(List<OrderCreateStrategy.OrderBuildResult> buildResults, List<Order> orders) {
        int orderItemNum = buildResults.stream()
                                       .mapToInt(result ->
                                                         result.getOrderItems()
                                                               .size())
                                       .sum();
        List<OrderItem> orderItemList = new ArrayList<>(orderItemNum);
        int size = buildResults.size();
        for (int i = 0; i < size; ++i) {
            Long orderId = orders.get(i)
                                 .getId();

            List<OrderItem> orderItems = buildResults.get(i)
                                                     .getOrderItems();
            bindOrderId(orderId, orderItems);
            orderItemList.addAll(orderItems);
        }
        return orderItemList;
    }

    private void saveOrderItems(List<OrderItem> orderItems) {
        if (!orderItemService.saveBatchItems(orderItems)) {
            log.error("订单明细保存失败");
            throw new BusinessException(BizErrorCode.ORDER_CREATE_FAILED);
        }
    }

    private String extractOrderNo(Order parentOrder, List<Order> orders) {
        return (parentOrder != null) ? parentOrder.getNo() : orders.get(0)
                                                                   .getNo();
    }

    /**
     * 创建父订单
     *
     * @param buildResults 订单构建结果列表
     * @param address      收货地址
     * @return 父订单
     */
    private Order createParentOrder(
            List<OrderCreateStrategy.OrderBuildResult> buildResults,
            Address address
    ) {
        // 计算所有子订单的总价
        long totalPrice = buildResults.stream()
                                      .mapToLong(r -> r.getOrder()
                                                       .getTotalPrice())
                                      .sum();

        return Order.builder()
                    .no(OrderNoUtil.generateParentOrderNo())
                    .userId(UserContextHolder.getUserId())
                    .totalPrice(totalPrice)
                    .quantity(buildResults.size())  // 子订单数量
                    .status(OrderStatus.CREATED.getCode())
                    .orderType(OrderType.PARENT.getCode())
                    .userName(address.getReceiver())
                    .address(address.getFullAddress() + "/" + address.getDetail())
                    .phone(address.getPhone())
                    .build();
    }

    private void assembleOrder(
            Address address,
            Order order,
            Order parentOrder
    ) {
        order.setUserName(address.getReceiver());
        order.setAddress(address.getFullAddress() + "/" + address.getDetail());
        order.setPhone(address.getPhone());

        // 设置订单类型和父订单关联
        if (parentOrder != null) {
            order.setOrderType(OrderType.SUB.getCode());
            order.setParentId(parentOrder.getId());
        } else {
            order.setOrderType(OrderType.NORMAL.getCode());
        }
    }

    private void bindOrderId(Long orderId, List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orderId);
        }
    }

    @Override
    public boolean queryPaymentStatus(String orderNo) {
        log.info("开始查询支付状态, orderNo: {}", orderNo);

        try {
            // 模拟支付查询延迟（休眠2秒）
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error("支付状态查询被中断, orderNo: {}", orderNo, e);
            Thread.currentThread()
                  .interrupt();
        }

        // 模拟支付成功，实际项目中应该调用真实的支付平台API
        log.info("支付查询成功（模拟）, orderNo: {}, 支付状态: 成功", orderNo);

        Order order = queryOrderByOrderNo(orderNo);
        validateOrder(order);
        return updateOrderStatusByOrderNo(order, OrderStatus.PAID);
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
    public boolean shipOrder(String orderNo) {
        Order order = queryUserOrderByOrderNo(orderNo);
        validateOrder(order);
        return updateOrderStatusByOrderNo(order, OrderStatus.SHIPPED);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelOrder(String orderNo) {
        Order order = queryUserOrderByOrderNo(orderNo);
        validateOrder(order);
        return updateOrderStatusByOrderNo(order, OrderStatus.CANCELED);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean finishOrder(String orderNo) {
        Order order = queryUserOrderByOrderNo(orderNo);
        validateOrder(order);
        return updateOrderStatusByOrderNo(order, OrderStatus.FINISHED);
    }

    @Override
    public IPage<OrderVO> pageQueryForAdmin(OrderQueryDTO queryDTO) {
        UserRole role = UserRole.of(UserContextHolder.getUserRoleCode());
        // 构建查询条件（管理员可以查询所有订单）
        LambdaQueryWrapper<Order> wrapper = OrderQueryWrapper.build(role, queryDTO, null);

        // 执行分页查询
        Page<Order> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
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
        Page<Order> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<Order> orderPage = this.page(page, wrapper);

        return orderPage.convert(OrderVO::buildOrderVO);
    }

    @Override
    public boolean shipOrderMerchant(String orderNo) {
        List<Store> stores = queryUserStore();
        validateStore(stores);

        Order order = queryStoreOrder(orderNo, stores);
        validateStoreOrder(order);
        return updateOrderStatusByOrderNo(order, OrderStatus.SHIPPED);
    }

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

    private Order queryOrderByOrderNo(String orderNo) {
        return lambdaQuery().eq(Order::getNo, orderNo)
                            .one();
    }

    private boolean updateOrderStatusByOrderNo(Order order, OrderStatus newStatus) {
        OrderStatusMachine.validateTransition(OrderStatus.of(order.getStatus()), newStatus);

        order.setStatus(newStatus.getCode());
        syncOrderStatus(order);
        return this.updateById(order);
    }

    private Order queryUserOrderByOrderNo(String orderNo) {
        return lambdaQuery().eq(Order::getNo, orderNo)
                            .eq(Order::getUserId, UserContextHolder.getUserId())
                            .one();
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new BusinessException(BizErrorCode.ORDER_NOT_EXIST);
        }
    }

    private void syncOrderStatus(Order order) {
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
}
