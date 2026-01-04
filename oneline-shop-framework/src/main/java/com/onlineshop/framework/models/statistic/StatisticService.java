package com.onlineshop.framework.models.statistic;

import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.category.Category;
import com.onlineshop.framework.models.category.ICategoryService;
import com.onlineshop.framework.models.favorite.Favorite;
import com.onlineshop.framework.models.favorite.IFavoriteService;
import com.onlineshop.framework.models.goods.Goods;
import com.onlineshop.framework.models.goods.IGoodsService;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.entity.OrderItem;
import com.onlineshop.framework.models.order.enums.OrderStatus;
import com.onlineshop.framework.models.order.enums.OrderType;
import com.onlineshop.framework.models.statistic.vo.*;
import com.onlineshop.framework.models.user.UserRole;
import com.onlineshop.framework.utils.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
public class StatisticService implements IStatisticService {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IOrderItemService orderItemService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private IFavoriteService favoriteService;

    @Autowired
    private ICategoryService categoryService;

    @Override
    public List<GoodsSalesTopVO> queryGoodsSalesRankMerchant(Integer top) {
        int topNum = top == null || top <= 0 ? 10 : top;
        log.info("商家查询自己店铺的商品销售排行，top: {}", topNum);
        Long storeId = getCurrentStoreId();
        List<OrderItem> orderItems = fetchFinishedOrderItems();
        if (orderItems.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, GoodsSalesTopVO> rankMap = calculateGoodsSalesRank(orderItems, storeId);
        List<GoodsSalesTopVO> rankList = sortAndRankList(rankMap.values(), topNum,
                                                         this::compareSalesTop);

        log.info("商家获取商品销售排行 {} 条", rankList.size());
        return rankList;
    }

    /**
     * 获取当前用户的店铺ID
     * - 管理员：返回 null（表示查询所有店铺）
     * - 商家：返回 JWT token中的店铺ID
     *
     * @return 店铺ID，或null表示不限制店铺
     * @throws BusinessException 当商家用户未绑定店铺时或无权限时
     */
    private Long getCurrentStoreId() {
        UserRole role = UserRole.of(UserContextHolder.getUserRoleCode());

        if (UserRole.ADMIN.equals(role)) {
            log.debug("管理员查询，不限制店铺");
            return null;
        } else if (UserRole.MERCHANT.equals(role)) {
            Long storeId = UserContextHolder.getStoreId();
            if (storeId == null) {
                throw new BusinessException(BizErrorCode.MERCHANT_NO_SHOP);
            }

            log.debug("商家查询，店铺ID: {}", storeId);
            return storeId;
        } else {
            throw new BusinessException(BizErrorCode.INVALID_ROLE);
        }
    }

    /**
     * 获取已完成订单的所有订单项
     * 该方法不分页，获取全量数据，请谨慎使用
     *
     * @return 已完成订单的订单项列表
     */
    private List<OrderItem> fetchFinishedOrderItems() {
        // 查询已完成的订单：FINISHED状态且类型为NORMAL或SUB（不包括PARENT订单）
        List<Order> finishedOrders = orderService.lambdaQuery()
                                                 .eq(Order::getStatus, "FINISHED")
                                                 .in(Order::getOrderType, "NORMAL", "SUB")
                                                 .list();

        if (finishedOrders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderIds = finishedOrders.stream()
                                            .map(Order::getId)
                                            .collect(Collectors.toList());

        return orderItemService.lambdaQuery()
                               .in(OrderItem::getOrderId, orderIds)
                               .list();
    }

    /**
     * 计算商品销售排行
     *
     * @param orderItems 订单项列表
     * @param storeId    店铺ID，null表示所有店铺
     * @return 商品销售排行映射
     */
    private Map<Long, GoodsSalesTopVO> calculateGoodsSalesRank(List<OrderItem> orderItems,
                                                               Long storeId) {
        Map<Long, GoodsSalesTopVO> rankMap = new HashMap<>();

        Map<Long, Goods> goodsMap = loadGoodsMap(orderItems);

        for (OrderItem item : orderItems) {
            Goods goods = goodsMap.get(item.getGoodsId());
            if (goods == null || (storeId != null && !storeId.equals(goods.getStoreId()))) {
                continue;
            }

            GoodsSalesTopVO rankVO = rankMap.computeIfAbsent(item.getGoodsId(), goodsId ->
                    GoodsSalesTopVO.builder()
                                   .goodsId(goodsId)
                                   .goodsName(goods.getName())
                                   .goodsCover(goods.getImg())
                                   .saleCount(0L)
                                   .saleAmount("0")
                                   .build());

            rankVO.setSaleCount(rankVO.getSaleCount() + item.getQuantity());
            rankVO.setSaleAmount(
                    String.valueOf(Long.parseLong(rankVO.getSaleAmount()) + item.getGoodsPrice()));
        }

        return rankMap;
    }

    /**
     * 排序和排名列表
     *
     * @param collection 待排序集合
     * @param limit      限制条数
     * @param comparator 比较器
     * @param <T>        泛型类型
     * @return 排序后的列表
     */
    private <T> List<T> sortAndRankList(Collection<T> collection, int limit,
                                        Comparator<T> comparator) {
        List<T> list = collection.stream()
                                 .sorted(comparator)
                                 .limit(limit)
                                 .collect(Collectors.toList());
        addRanking(list);
        return list;
    }

    /**
     * 比较商品销售排行（GoodsSalesTopVO版本）
     *
     * @param a 商品A
     * @param b 商品B
     * @return 比较结果
     */
    private int compareSalesTop(GoodsSalesTopVO a, GoodsSalesTopVO b) {
        // 按销售量降序排列
        return Long.compare(b.getSaleCount(), a.getSaleCount());
    }

    /**
     * 加载订单项中的商品信息
     *
     * @param orderItems 订单项列表
     * @return 商品ID与商品对象的映射
     */
    private Map<Long, Goods> loadGoodsMap(List<OrderItem> orderItems) {
        Set<Long> goodsIds = orderItems.stream()
                                       .map(OrderItem::getGoodsId)
                                       .collect(Collectors.toSet());

        return goodsService.lambdaQuery()
                           .in(Goods::getId, goodsIds)
                           .list()
                           .stream()
                           .collect(Collectors.toMap(Goods::getId, goods -> goods));
    }

    /**
     * 为列表中的对象添加排名信息
     *
     * @param list 待排名的列表
     */
    private void addRanking(List<?> list) {
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item instanceof GoodsSalesTopVO) {
                ((GoodsSalesTopVO) item).setRank(i + 1);
            } else if (item instanceof FavoriteGoodsTopVO) {
                ((FavoriteGoodsTopVO) item).setRank(i + 1);
            }
        }
    }

    @Override
    public List<FavoriteGoodsTopVO> queryGoodsFavoriteRankMerchant(Integer top) {
        int topNum = top == null || top <= 0 ? 10 : top;
        log.info("商家查询自己店铺的商品收藏排行，top: {}", topNum);
        Long storeId = getCurrentStoreId();
        List<Goods> goods = fetchShelfGoods(storeId);
        if (goods.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> favoriteCountMap = calculateFavoriteCount(goods);
        Map<Long, Long> favoriteLast7DaysMap = calculateFavoriteLast7Days(goods);
        List<FavoriteGoodsTopVO> rankList = buildFavoriteGoodsTopList(goods, favoriteCountMap,
                                                                      favoriteLast7DaysMap);
        List<FavoriteGoodsTopVO> finalList = sortAndRankList(rankList, topNum,
                                                             (a, b) -> Integer.compare(
                                                                     b.getFavoriteTotal(),
                                                                     a.getFavoriteTotal()));
        addRanking(finalList);

        log.info("商家获取商品收藏排行 {} 条", finalList.size());
        return finalList;
    }

    @Override
    public List<RevenueVO> queryRevenueByDaysTrendMerchant(int days) {
        log.info("商家查询{}天的营收趋势", days);
        Long storeId = getCurrentStoreId();
        return queryRevenueByDaysTrendInternal(storeId, days);
    }

    /**
     * 内部方法：查询指定店铺的营收趋势
     *
     * @param storeId 店铺ID，null表示所有店铺
     * @param days    查询天数
     * @return 每日营收趋势列表
     */
    private List<RevenueVO> queryRevenueByDaysTrendInternal(Long storeId, int days) {
        // 查询N天数据：包含今天和前面的N-1天
        // 例如：查询7天 = 今天 + 前6天
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 转换为 LocalDateTime，包含完整的时间范围
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // 获取时间范围内的所有订单
        List<Order> orders = orderService.lambdaQuery()
                                         .eq(Order::getStatus, OrderStatus.FINISHED.getCode())
                                         .in(Order::getOrderType, OrderType.NORMAL.getCode(),
                                             OrderType.SUB.getCode())
                                         .ge(Order::getCreateTime, startDateTime)
                                         .le(Order::getCreateTime, endDateTime)
                                         .apply(storeId != null, "store_id = {0}", storeId)
                                         .list();

        // 按日期分组统计营收
        Map<LocalDate, RevenueVO> trendMap = new TreeMap<>();

        // 初始化所有日期的数据
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            trendMap.put(date, RevenueVO.builder()
                                        .date(date)
                                        .revenue(0L)
                                        .build());
        }

        // 统计每日的营收
        for (Order order : orders) {
            LocalDate orderDate = order.getCreateTime()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalDate();

            RevenueVO revenueVO = trendMap.get(orderDate);
            if (revenueVO != null) {
                revenueVO.setRevenue(revenueVO.getRevenue() + order.getTotalPrice());
            }
        }

        log.info("查询{}天营收趋势: {} 条记录", days, trendMap.size());
        return new ArrayList<>(trendMap.values());
    }

    @Override
    public List<CategorySalesRatioVO> queryCategorySalesRatioMerchant() {
        log.info("商家查询自己店铺的类目销售占比");
        Long storeId = getCurrentStoreId();
        return queryCategorySalesRatio(storeId);
    }

    // ========== 管理员方法 ==========

    @Override
    public DashboardOverviewVO queryDashboardOverviewMerchant() {
        log.info("商家查询仪表板概览信息");
        Long storeId = getCurrentStoreId();
        return queryDashboardOverviewInternal(storeId);
    }

    @Override
    public List<GoodsSalesTopVO> queryGoodsSalesRankAdmin(Integer top) {
        int topNum = top == null || top <= 0 ? 10 : top;
        log.info("管理员查询商品销售排行，top: {}", topNum);
        // 管理员查询全平台销售排行（不限制店铺）
        List<OrderItem> orderItems = fetchFinishedOrderItems();
        if (orderItems.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, GoodsSalesTopVO> rankMap = calculateGoodsSalesRank(orderItems, null);
        List<GoodsSalesTopVO> rankList = sortAndRankList(rankMap.values(), topNum,
                                                         this::compareSalesTop);

        log.info("管理员获取商品销售排行 {} 条", rankList.size());
        return rankList;
    }

    @Override
    public List<FavoriteGoodsTopVO> queryGoodsFavoriteRankAdmin(Integer top) {
        int topNum = top == null || top <= 0 ? 10 : top;
        log.info("管理员查询商品收藏排行，top: {}", topNum);
        // 管理员查询全平台收藏排行（不限制店铺）
        List<Goods> goods = fetchShelfGoods(null);
        if (goods.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> favoriteCountMap = calculateFavoriteCount(goods);
        Map<Long, Long> favoriteLast7DaysMap = calculateFavoriteLast7Days(goods);
        List<FavoriteGoodsTopVO> rankList = buildFavoriteGoodsTopList(goods, favoriteCountMap,
                                                                      favoriteLast7DaysMap);
        List<FavoriteGoodsTopVO> finalList = sortAndRankList(rankList, topNum,
                                                             (a, b) -> Integer.compare(
                                                                     b.getFavoriteTotal(),
                                                                     a.getFavoriteTotal()));
        addRanking(finalList);

        log.info("管理员获取商品收藏排行 {} 条", finalList.size());
        return finalList;
    }


    // ========== 营收趋势方法 ==========

    @Override
    public List<RevenueVO> queryRevenueByDaysTrendAdmin(int days) {
        log.info("管理员查询{}天的营收趋势", days);
        return queryRevenueByDaysTrendInternal(null, days);
    }

    @Override
    public List<CategorySalesRatioVO> queryCategorySalesRatioAdmin() {
        log.info("管理员查询所有店铺的类目销售占比");
        return queryCategorySalesRatio(null);
    }

    @Override
    public DashboardOverviewVO queryDashboardOverviewAdmin() {
        log.info("管理员查询平台仪表板概览信息");
        return queryDashboardOverviewInternal(null);
    }

    /**
     * 内部方法：查询仪表板概览信息
     * 包含：今日营收、今日订单数、今日新增用户数、累计营收
     *
     * @param storeId 店铺ID，null表示所有店铺
     * @return 仪表板概览信息
     */
    private DashboardOverviewVO queryDashboardOverviewInternal(Long storeId) {
        // 获取今天的开始和结束时间
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);

        // 查询今日已完成订单的营收和订单数
        List<Order> todayOrders = orderService.lambdaQuery()
                                              .eq(Order::getStatus, OrderStatus.FINISHED.getCode())
                                              .in(Order::getOrderType, OrderType.NORMAL.getCode(),
                                                  OrderType.SUB.getCode())
                                              .ge(Order::getCreateTime, todayStart)
                                              .le(Order::getCreateTime, todayEnd)
                                              .apply(storeId != null, "store_id = {0}", storeId)
                                              .list();

        // 统计今日营收和订单数
        BigDecimal todayRevenue = BigDecimal.ZERO;
        int todayOrderCount = 0;
        if (!todayOrders.isEmpty()) {
            // 统计营收（单位：分）
            todayRevenue = BigDecimal.valueOf(todayOrders.stream()
                                                         .mapToLong(Order::getTotalPrice)
                                                         .sum());
            // 统计订单数
            todayOrderCount = todayOrders.size();
        }

        // TODO: 暂时不统计新增用户
        int todayNewUserCount = 0;

        // 查询累计营收（所有已完成订单）
        List<Order> allOrders = orderService.lambdaQuery()
                                            .eq(Order::getStatus, OrderStatus.FINISHED.getCode())
                                            .in(Order::getOrderType, OrderType.NORMAL.getCode(),
                                                OrderType.SUB.getCode())
                                            .apply(storeId != null, "store_id = {0}", storeId)
                                            .list();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (!allOrders.isEmpty()) {
            totalRevenue = BigDecimal.valueOf(allOrders.stream()
                                                       .mapToLong(Order::getTotalPrice)
                                                       .sum());
        }

        log.info("仪表板概览 - 今日营收: {}, 今日订单数: {}, 今日新增用户: {}, 累计营收: {}",
                 todayRevenue, todayOrderCount, todayNewUserCount, totalRevenue);

        return DashboardOverviewVO.builder()
                                  .todayRevenue(todayRevenue)
                                  .todayOrderCount(todayOrderCount)
                                  .todayNewUserCount(todayNewUserCount)
                                  .totalRevenue(totalRevenue)
                                  .build();
    }

    /**
     * 获取上架的商品列表
     *
     * @param storeId 店铺ID，null表示所有店铺
     * @return 上架商品列表
     */
    private List<Goods> fetchShelfGoods(Long storeId) {
        return goodsService.lambdaQuery()
                           .eq(storeId != null, Goods::getStoreId, storeId)
                           .eq(Goods::getStatus, true)
                           .list();
    }

    /**
     * 计算商品收藏数
     *
     * @param goods 商品列表
     * @return 商品ID与收藏数的映射
     */
    private Map<Long, Long> calculateFavoriteCount(List<Goods> goods) {
        List<Long> goodsIds = goods.stream()
                                   .map(Goods::getId)
                                   .collect(Collectors.toList());

        List<Favorite> favorites = favoriteService.lambdaQuery()
                                                  .in(Favorite::getGoodsId, goodsIds)
                                                  .list();

        return favorites.stream()
                        .collect(
                                Collectors.groupingBy(Favorite::getGoodsId, Collectors.counting()));
    }

    /**
     * 计算最近7天的商品收藏数
     *
     * @param goods 商品列表
     * @return 商品ID与最近7天收藏数的映射
     */
    private Map<Long, Long> calculateFavoriteLast7Days(List<Goods> goods) {
        List<Long> goodsIds = goods.stream()
                                   .map(Goods::getId)
                                   .collect(Collectors.toList());

        LocalDate sevenDaysAgo = LocalDate.now()
                                          .minusDays(6);
        LocalDateTime startDateTime = sevenDaysAgo.atStartOfDay();

        List<Favorite> favorites = favoriteService.lambdaQuery()
                                                  .in(Favorite::getGoodsId, goodsIds)
                                                  .ge(Favorite::getAddedAt, startDateTime)
                                                  .list();

        return favorites.stream()
                        .collect(
                                Collectors.groupingBy(Favorite::getGoodsId, Collectors.counting()));
    }

    /**
     * 构建收藏排行列表（FavoriteGoodsTopVO）
     *
     * @param goods                商品列表
     * @param favoriteCountMap     总收藏数映射
     * @param favoriteLast7DaysMap 最近7天收藏数映射
     * @return 收藏排行VO列表
     */
    private List<FavoriteGoodsTopVO> buildFavoriteGoodsTopList(List<Goods> goods,
                                                               Map<Long, Long> favoriteCountMap,
                                                               Map<Long, Long> favoriteLast7DaysMap) {
        return goods.stream()
                    .map(g -> FavoriteGoodsTopVO.builder()
                                                .goodsId(g.getId())
                                                .goodsName(g.getName())
                                                .goodsImage(g.getImg())
                                                .favoriteTotal(
                                                        favoriteCountMap.getOrDefault(g.getId(), 0L)
                                                                        .intValue())
                                                .favoriteLast7Days(
                                                        favoriteLast7DaysMap.getOrDefault(g.getId(),
                                                                                          0L)
                                                                            .intValue())
                                                .build())
                    .collect(Collectors.toList());
    }

    // ========== 类目销售占比方法 ==========

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

    /**
     * 查询类目销售占比
     * 只展示顶级分类，将子分类的销售额统计到其顶级分类
     *
     * @param storeId 店铺ID，null表示所有店铺
     * @return 类目销售占比列表
     */
    private List<CategorySalesRatioVO> queryCategorySalesRatio(Long storeId) {
        // 获取已完成的订单项
        List<OrderItem> orderItems = fetchFinishedOrderItems();
        if (orderItems.isEmpty()) {
            return Collections.emptyList();
        }

        // 加载商品和分类信息
        Map<Long, Goods> goodsMap = loadGoodsMap(orderItems);
        List<Category> allCategories = categoryService.list();
        Map<Long, Category> categoryMap = allCategories.stream()
                                                       .collect(Collectors.toMap(Category::getId,
                                                                                 c -> c));

        // 按顶级分类统计销售额
        Map<Long, Long> categorySalesMap = new HashMap<>();
        for (OrderItem item : orderItems) {
            Goods goods = goodsMap.get(item.getGoodsId());
            if (goods == null || (storeId != null && !storeId.equals(goods.getStoreId()))) {
                continue;
            }

            Long topCategoryId = getTopCategoryId(Long.valueOf(goods.getCategoryId()), categoryMap);
            if (topCategoryId != null) {
                categorySalesMap.merge(topCategoryId, item.getGoodsPrice(), Long::sum);
            }
        }

        if (categorySalesMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算总销售额
        long totalSaleAmount = categorySalesMap.values()
                                               .stream()
                                               .mapToLong(Long::longValue)
                                               .sum();

        // 构建返回列表，只包含有销售数据的顶级分类
        List<CategorySalesRatioVO> resultList = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : categorySalesMap.entrySet()) {
            Long categoryId = entry.getKey();
            Long saleAmount = entry.getValue();
            Category category = categoryMap.get(categoryId);

            // 分类必须存在，否则说明数据不一致
            if (category == null) {
                log.warn("分类ID: {} 不存在，跳过处理", categoryId);
                continue;
            }

            BigDecimal saleAmountDecimal = new BigDecimal(saleAmount);
            BigDecimal ratio = saleAmountDecimal.divide(new BigDecimal(totalSaleAmount), 4,
                                                        BigDecimal.ROUND_HALF_UP);

            resultList.add(CategorySalesRatioVO.builder()
                                               .categoryId(categoryId)
                                               .categoryName(category.getName())
                                               .saleAmount(saleAmountDecimal)
                                               .saleRatio(ratio)
                                               .build());
        }

        // 按销售额降序排序
        resultList.sort((a, b) -> b.getSaleAmount()
                                   .compareTo(a.getSaleAmount()));

        log.info("类目销售占比查询完成，共 {} 个顶级类目", resultList.size());
        return resultList;
    }

    /**
     * 内部方法：查询指定店铺的营收数据
     *
     * @param storeId 店铺ID，null表示所有店铺
     * @param days    查询天数
     * @return 营收统计信息
     */
    private RevenueStatisticVO queryRevenueByDaysInternal(Long storeId, int days) {
        LocalDateTime startDateTime = calculateStartDateTime(days);

        List<Order> orders = orderService.lambdaQuery()
                                         .eq(Order::getStatus, OrderStatus.FINISHED.getCode())
                                         .ge(Order::getCreateTime, startDateTime)
                                         .apply(storeId != null, "store_id = {0}", storeId)
                                         .list();

        long totalRevenue = orders.stream()
                                  .mapToLong(Order::getTotalPrice)
                                  .sum();
        long orderCount = orders.size();

        log.info("查询{}天营收额: {}分, 订单数: {}", days, totalRevenue, orderCount);

        return RevenueStatisticVO.builder()
                                 .totalRevenue(totalRevenue)
                                 .orderCount(orderCount)
                                 .days(days)
                                 .build();
    }

    /**
     * 根据天数计算起始日期时间
     *
     * @param days 天数
     * @return 起始日期时间
     */
    private LocalDateTime calculateStartDateTime(int days) {
        return LocalDateTime.now()
                            .minusDays(days)
                            .withHour(0)
                            .withMinute(0)
                            .withSecond(0)
                            .withNano(0);
    }

    @Override
    public FavoriteOverviewVO queryFavoriteOverviewMerchant() {
        log.info("商家查询收藏概览信息");
        Long storeId = getCurrentStoreId();
        return queryFavoriteOverviewInternal(storeId);
    }

    @Override
    public FavoriteOverviewVO queryFavoriteOverviewAdmin() {
        log.info("管理员查询收藏概览信息");
        return queryFavoriteOverviewInternal(null);
    }

    /**
     * 内部方法：查询收藏概览信息
     * 包含：今日新增收藏、今日取消收藏、今日净增加、累计收藏总数
     *
     * @param storeId 店铺ID，null表示所有店铺
     * @return 收藏概览信息
     */
    private FavoriteOverviewVO queryFavoriteOverviewInternal(Long storeId) {
        // 获取今天的开始和结束时间
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);

        // 查询今日新增收藏
        List<Favorite> todayFavorites = favoriteService.lambdaQuery()
                                                       .ge(Favorite::getAddedAt, todayStart)
                                                       .le(Favorite::getAddedAt, todayEnd)
                                                       .apply(storeId != null, "store_id = {0}", storeId)
                                                       .list();

        int todayFavoriteAdd = todayFavorites.size();

        // 注：数据库中Favorite表只记录添加，没有记录取消
        // 取消收藏通常是物理删除，无法统计历史取消数据
        // 暂时设为0，实际应用中可考虑添加删除记录表
        int todayFavoriteCancel = 0;

        // 今日净增加 = 新增 - 取消
        int todayFavoriteNetIncrease = todayFavoriteAdd - todayFavoriteCancel;

        // 查询累计收藏总数
        long totalFavoriteCount = favoriteService.lambdaQuery()
                                                 .apply(storeId != null, "store_id = {0}", storeId)
                                                 .count();

        log.info("收藏概览 - 今日新增收藏: {}, 今日取消收藏: {}, 今日净增加: {}, 累计收藏总数: {}", 
                 todayFavoriteAdd, todayFavoriteCancel, todayFavoriteNetIncrease, totalFavoriteCount);

        return FavoriteOverviewVO.builder()
                                .todayFavoriteAdd(todayFavoriteAdd)
                                .todayFavoriteCancel(todayFavoriteCancel)
                                .todayFavoriteNetIncrease(todayFavoriteNetIncrease)
                                .totalFavoriteCount((int) totalFavoriteCount)
                                .build();
    }
}