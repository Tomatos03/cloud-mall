package com.onlineshop.framework.models.statistic;

import cn.hutool.core.collection.CollectionUtil;
import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.favorite.Favorite;
import com.onlineshop.framework.models.favorite.IFavoriteService;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.entity.OrderItem;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.enums.OrderType;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.statistic.vo.*;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.image.ImageUtil;
import com.onlineshop.framework.utils.money.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

/**
 * 统计服务实现类
 * 继承 ServiceImpl 获得 MyBatis-Plus 提供的公共方法
 * 所有业务逻辑通过调用其他 Service 的 MyBatis-Plus 方法实现
 *
 * @author Tomatos
 * @date 2025/12/24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticService implements IStatisticService {
    private final IOrderService orderService;
    private final IOrderItemService orderItemService;
    private final IGoodsService goodsService;
    private final IFavoriteService favoriteService;
    private final ICategoryService categoryService;

    @Override
    public Map<LocalDate, BigDecimal> queryLastRevenueTrend(int days) {
        List<Order> orders = queryLastOrders(days, OrderStatus.FINISHED);
        return createDailyRevenueMap(orders, days);
    }

    private List<GoodsSalesTopVO> queryGoodsSalesRank(Integer top) {
        List<OrderItem> orderItems = queryLast7DaysOrderItemsByStatus(OrderStatus.FINISHED);
        List<Long> goodsIds = convertDistinctGoodsIds(orderItems);
        List<Goods> goods = goodsService.queryGoodsListByIds(goodsIds);
        Map<Long, Goods> goodsMap = convertGoodsIdToGoodsMap(goods);
        return calculateGoodsSalesRank(goodsMap, orderItems);
    }

    private List<FavoriteGoodsTopVO> queryGoodsFavoriteRank(Integer top) {
        List<Goods> goods = goodsService.queryEnableGoodsList();
        List<Favorite> favorites = favoriteService.queryLast7DaysFavoritesByGoodsIds(convertGoodsIds(goods));
        Map<Long, Long> favoriteCountMap = createFavoriteIdToFavourteCountMap(favorites);
        return createFavoriteGoodsTopVOList(goods, favoriteCountMap);
    }

    private List<CategorySalesRatioVO> queryCategorySalesRatio() {
        List<OrderItem> orderItems = queryLast7DaysOrderItemsByStatus(OrderStatus.FINISHED);
        List<Long> goodsIds = convertDistinctGoodsIds(orderItems);
        List<Goods> goodsList = goodsService.queryGoodsListByIds(goodsIds);
        Map<Long, Goods> goodsMap = convertGoodsIdToGoodsMap(goodsList);

        List<Category> allCategories = categoryService.list();
        Map<Long, Category> categoryMap = convertCategoryIdToCategoryMap(allCategories);
        Map<Long, Long> categorySalesMap = createTopCategoryIdToSaleAmountMap(orderItems, goodsMap, categoryMap);
        return createCategorySalesRatioList(categorySalesMap, categoryMap);
    }

    private DashboardOverviewVO queryDashboardOverview() {
        List<Order> todayOrders = queryLastOrders(0, OrderStatus.FINISHED);
        List<Order> last7DaysOrders = queryLastOrders(7, OrderStatus.FINISHED);
        return buildDashBoardOverviewVo(
                calculateTotalRevenue(todayOrders),
                todayOrders.size(),
                queryTodayNewAddUserCount(),
                calculateTotalRevenue(last7DaysOrders)
        );
    }

    @Override
    public StatisticDataVO queryAllStatisticData() {
        return StatisticDataVO.builder()
                              .dashboardOverview(queryDashboardOverview())
                              .goodsSalesRank(queryGoodsSalesRank(10))
//                              .goodsFavoriteRank(queryGoodsFavoriteRank(10))
                              .categorySalesRatio(queryCategorySalesRatio())
                              .revenueTrend(queryLastRevenueTrend(7))
                              .build();
    }

    private static CategorySalesRatioVO buildCategorySalesRatioVO(
            Long categoryId,
            Category category,
            BigDecimal saleAmountDecimal,
            BigDecimal ratio
    ) {
        return CategorySalesRatioVO.builder()
                                   .categoryId(categoryId)
                                   .categoryName(category.getName())
                                   .saleAmount(saleAmountDecimal)
                                   .saleRatio(ratio)
                                   .build();
    }

    private static DashboardOverviewVO buildDashBoardOverviewVo(
            BigDecimal todayRevenue,
            Integer todayOrderCount,
            Long todayNewUserCount,
            BigDecimal totalRevenue
    ) {
        return DashboardOverviewVO.builder()
                                  .todayRevenue(todayRevenue)
                                  .todayOrderCount(todayOrderCount)
                                  .todayNewUserCount(todayNewUserCount)
                                  .totalRevenue(totalRevenue)
                                  .build();
    }

    private GoodsSalesTopVO buildGoodsSalesTopVO(Goods goods, OrderItem orderItem) {
        return GoodsSalesTopVO.builder()
                              .goodsId(goods.getId())
                              .goodsName(goods.getName())
                              .mainImageUrl(ImageUtil.getMainImageUrl(goods.getDisplayImages()))
                              .saleCount(Long.valueOf(orderItem.getQuantity()))
                              .saleAmount(
                                      Money.ofCents(orderItem.getTotalPrice())
                                           .toYuan()
                              )
                              .build();
    }

    /**
     * 计算商品销售排行
     *
     * @return 商品销售排行映射
     */
    private List<GoodsSalesTopVO> calculateGoodsSalesRank(
            Map<Long, Goods> goodsMap,
            List<OrderItem> orderItems
    ) {
        if (CollectionUtil.isEmpty(goodsMap)) {
            return Collections.emptyList();
        }

        Map<Long, GoodsSalesTopVO> goodsIdToGoodsSalesTopVOMap = new HashMap<>(goodsMap.size());
        for (OrderItem item : orderItems) {
            Goods goods = goodsMap.get(item.getGoodsId());
            goodsIdToGoodsSalesTopVOMap.merge(
                    goods.getId(),
                    buildGoodsSalesTopVO(goods, item),
                    (oldV, newV) -> {
                        oldV.setSaleCount(oldV.getSaleCount() + item.getQuantity());
                        oldV.setSaleAmount(oldV.getSaleAmount()
                                               .add(BigDecimal.valueOf(item.getGoodsPrice())));
                        return oldV;
                    });
        }
        return convertSortedGoodsSalesTops(goodsIdToGoodsSalesTopVOMap);
    }

    private static long calculateSaleAmount(Map<Long, Long> categorySalesMap) {
        return categorySalesMap.values()
                               .stream()
                               .mapToLong(Long::longValue)
                               .sum();
    }

    /**
     * 计算订单总营收
     *
     * @param orders 订单列表
     * @return 总营收金额（BigDecimal）
     */
    private BigDecimal calculateTotalRevenue(List<Order> orders) {
        if (CollectionUtil.isEmpty(orders)) {
            return BigDecimal.ZERO;
        }

        return orders.stream()
                     .map(order -> Money
                             .ofCents(order.getTotalPrice())
                             .toYuan()
                     )
                     .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void calculateTrendMap(List<Order> orders, Map<LocalDate, BigDecimal> trendMap) {
        for (Order order : orders) {
            LocalDate orderTime = order.getCreateTime()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalDate();
            trendMap.computeIfPresent(orderTime, (date, saleAmount) ->
                    saleAmount.add(
                            Money.ofCents(order.getTotalPrice())
                                 .toYuan()
                    )
            );
        }
    }

    /**
     *
     * @param a 商品A
     * @param b 商品B
     * @return 比较结果
     */
    private int compareSalesCount(GoodsSalesTopVO a, GoodsSalesTopVO b) {
        return Long.compare(b.getSaleCount(), a.getSaleCount());
    }

    private static Map<Long, Category> convertCategoryIdToCategoryMap(List<Category> allCategories) {
        if (CollectionUtil.isEmpty(allCategories)) {
            return Collections.emptyMap();
        }

        return allCategories.stream()
                            .collect(Collectors.toMap(Category::getId,
                                                      c -> c));
    }

    private static List<Long> convertDistinctGoodsIds(List<OrderItem> orderItems) {
        return orderItems.stream()
                         .map(OrderItem::getGoodsId)
                         .distinct()
                         .toList();
    }

    private Map<Long, Goods> convertGoodsIdToGoodsMap(List<Goods> goods) {
        if (CollectionUtil.isEmpty(goods)) {
            return Collections.emptyMap();
        }
        return goods.stream()
                    .collect(Collectors.toMap(Goods::getId, g -> g));
    }

    private static List<Long> convertGoodsIds(List<Goods> goods) {
        return goods.stream()
                    .map(Goods::getId)
                    .toList();
    }

    private static List<Long> convertOrderIds(List<Order> finishedOrders) {
        return finishedOrders.stream()
                             .map(Order::getId)
                             .collect(Collectors.toList());
    }

    private List<GoodsSalesTopVO> convertSortedGoodsSalesTops(Map<Long, GoodsSalesTopVO> goodsIdToGoodsSalesTopVOMap) {
        return goodsIdToGoodsSalesTopVOMap.values()
                                          .stream()
                                          .sorted(this::compareSalesCount)
                                          .collect(Collectors.toList());
    }

    private static List<CategorySalesRatioVO> createCategorySalesRatioList(
            Map<Long, Long> categorySalesMap,
            Map<Long, Category> categoryMap
    ) {
        long totalSaleAmount = calculateSaleAmount(categorySalesMap);
        List<CategorySalesRatioVO> resultList = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : categorySalesMap.entrySet()) {
            Long categoryId = entry.getKey();
            Category category = categoryMap.get(categoryId);

            BigDecimal saleAmountDecimal = new BigDecimal(entry.getValue());
            BigDecimal ratio = saleAmountDecimal.divide(new BigDecimal(totalSaleAmount), 4, HALF_UP);

            resultList.add(buildCategorySalesRatioVO(categoryId, category, saleAmountDecimal, ratio));
        }
        return resultList;
    }

    /**
     * 初始化每日营收数据，为指定天数范围内的每一天创建RevenueVO对象
     *
     * @param orders
     * @param days   天数
     * @return 日期到营收数据的映射
     */
    private Map<LocalDate, BigDecimal> createDailyRevenueMap(List<Order> orders, int days) {
        Map<LocalDate, BigDecimal> trendMap = initTrendMap(days);
        calculateTrendMap(orders, trendMap);
        return trendMap;
    }

    /**
     * 构建收藏排行列表（FavoriteGoodsTopVO）
     *
     * @param goods            商品列表
     * @param favoriteCountMap 总收藏数映射
     * @return 收藏排行VO列表
     */
    private List<FavoriteGoodsTopVO> createFavoriteGoodsTopVOList(
            List<Goods> goods,
            Map<Long, Long> favoriteCountMap
    ) {
        if (CollectionUtil.isEmpty(goods)) {
            return Collections.emptyList();
        }

        return goods.stream()
                    .map(g -> FavoriteGoodsTopVO.builder()
                                                .goodsId(g.getId())
                                                .goodsName(g.getName())
                                                .goodsMainImageUrl(ImageUtil.getMainImageUrl(g.getDisplayImages()))
                                                .favoriteTotal(favoriteCountMap.getOrDefault(g.getId(), 0L))
                                                .build())
                    .collect(Collectors.toList());
    }

    private Map<Long, Long> createFavoriteIdToFavourteCountMap(List<Favorite> favorites) {
        return favorites.stream()
                        .collect(Collectors.groupingBy(Favorite::getGoodsId, Collectors.counting()));
    }

    private Map<Long, Long> createTopCategoryIdToSaleAmountMap(
            List<OrderItem> orderItems,
            Map<Long, Goods> goodsMap,
            Map<Long, Category> categoryMap
    ) {
        Map<Long, Long> categorySalesMap = new HashMap<>();
        for (OrderItem item : orderItems) {
            Goods goods = goodsMap.get(item.getGoodsId());
            Long topCategoryId = getTopCategoryId(goods.getCategoryId(), categoryMap);
            categorySalesMap.merge(topCategoryId, item.getGoodsPrice(), Long::sum);
        }
        return categorySalesMap;
    }

    /**
     * 获取分类的顶级分类ID
     *
     * @param categoryId  分类ID
     * @param categoryMap 分类映射
     * @return 顶级分类ID
     */
    private Long getTopCategoryId(Long categoryId, Map<Long, Category> categoryMap) {
        Category category = categoryMap.get(categoryId);
        if (category == null) {
            return null;
        }

        if (category.getParentId() == null || category.getParentId() == 0) {
            return categoryId;
        }

        return getTopCategoryId(category.getParentId(), categoryMap);
    }

    private static Map<LocalDate, BigDecimal> initTrendMap(int days) {
        Map<LocalDate, BigDecimal> trendMap = new HashMap<>(days);
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            trendMap.put(today.minusDays(i), BigDecimal.ZERO);
        }
        return trendMap;
    }

    /**
     * 获取已完成订单的所有订单项
     * 该方法不分页，获取全量数据，请谨慎使用
     *
     * @return 已完成订单的订单项列表
     */
    private List<OrderItem> queryLast7DaysOrderItemsByStatus(OrderStatus status) {
        List<Order> finishedOrders = queryLastOrders(7, status);
        List<Long> orderIds = convertOrderIds(finishedOrders);
        return queryOrderItems(orderIds);
    }

    /**
     * 查询指定天数范围内的已完成订单
     *
     * @param days 天数
     * @return 订单列表
     */
    private List<Order> queryLastOrders(int days, OrderStatus status) {
        Long storeId = AuthUserUtils.getStoreId();
        LocalDate startDate = LocalDate.now()
                                       .minusDays(Math.max(days - 1, 0));
        LocalDateTime startDateTime = startDate.atStartOfDay();

        return orderService.lambdaQuery()
                           .eq(Order::getStatus, status.getCode())
                           .in(Order::getOrderType, OrderType.NORMAL.getCode(), OrderType.SUB.getCode())
                           .ge(Order::getCreateTime, startDateTime)
                           .eq(storeId != null, Order::getStoreId, storeId)
                           .list();
    }

    private List<OrderItem> queryOrderItems(List<Long> orderIds) {
        if (CollectionUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return orderItemService.lambdaQuery()
                               .in(OrderItem::getOrderId, orderIds)
                               .list();
    }

    // TODO: 暂时不计算,返回0
    private Long queryTodayNewAddUserCount() {
        return 0L;
    }
}

