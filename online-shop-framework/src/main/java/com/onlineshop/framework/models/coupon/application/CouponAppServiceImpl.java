package com.onlineshop.framework.models.coupon.application;

import cn.hutool.core.collection.CollUtil;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.coupon.application.vo.CouponCalcResult;
import com.onlineshop.framework.models.coupon.application.vo.CouponPoolVO;
import com.onlineshop.framework.models.coupon.entity.CouponTemplate;
import com.onlineshop.framework.models.coupon.entity.CouponTemplateScope;
import com.onlineshop.framework.models.coupon.entity.CouponUsageLog;
import com.onlineshop.framework.models.coupon.entity.UserCoupon;
import com.onlineshop.framework.models.coupon.enums.CouponClaimLuaResult;
import com.onlineshop.framework.models.coupon.enums.CouponScopeType;
import com.onlineshop.framework.models.coupon.enums.CouponStatus;
import com.onlineshop.framework.models.coupon.enums.CouponType;
import com.onlineshop.framework.models.coupon.enums.CouponUserStatus;
import com.onlineshop.framework.models.coupon.service.ICouponTemplateScopeService;
import com.onlineshop.framework.models.coupon.service.ICouponTemplateService;
import com.onlineshop.framework.models.coupon.service.ICouponUsageLogService;
import com.onlineshop.framework.models.coupon.service.IUserCouponService;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CouponAppServiceImpl implements ICouponAppService {

    private static final String COUPON_STOCK_KEY_PREFIX = "coupon:stock:";
    private static final String COUPON_USER_CLAIM_KEY_PREFIX = "coupon:user:claim:";

    private static final DefaultRedisScript<Long> CLAIM_SCRIPT;

    static {
        CLAIM_SCRIPT = new DefaultRedisScript<>();
        CLAIM_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/coupon_claim.lua")));
        CLAIM_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    private ICouponTemplateService couponTemplateService;
    @Autowired
    private IUserCouponService userCouponService;
    @Autowired
    private ICouponUsageLogService couponUsageLogService;
    @Autowired
    private ICouponTemplateScopeService couponTemplateScopeService;
    @Autowired
    private IStoreService storeService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<CouponPoolVO> listCouponPool() {
        Long userId = AuthUserUtils.getUserId();
        List<CouponTemplate> templates = couponTemplateService.lambdaQuery()
                .eq(CouponTemplate::getStatus, CouponStatus.ACTIVE.getCode())
                .le(CouponTemplate::getStartTime, LocalDateTime.now())
                .ge(CouponTemplate::getEndTime, LocalDateTime.now())
                .list();

        List<CouponPoolVO> pool = new ArrayList<>();
        for (CouponTemplate template : templates) {
            CouponPoolVO vo = new CouponPoolVO();
            vo.setTemplateId(template.getId());
            vo.setName(template.getName());
            vo.setType(template.getType());
            vo.setStoreId(template.getStoreId());
            vo.setDiscountAmount(template.getDiscountAmount());
            vo.setDiscountRate(template.getDiscountRate());
            vo.setMaxDiscountAmount(template.getMaxDiscountAmount());
            vo.setMinOrderAmount(template.getMinOrderAmount());
            vo.setScopeType(template.getScopeType());
            vo.setStartTime(template.getStartTime());
            vo.setEndTime(template.getEndTime());
            vo.setTotalCount(template.getTotalCount());
            vo.setIssuedCount(template.getIssuedCount());
            vo.setPerUserLimit(template.getPerUserLimit());

            if (template.getStoreId() != null) {
                Store store = storeService.getById(template.getStoreId());
                if (store != null) {
                    vo.setStoreName(store.getName());
                }
            }

            List<UserCoupon> userCoupons = userCouponService.listByUserAndTemplate(userId, template.getId());
            vo.setUserClaimedCount(userCoupons.size());
            pool.add(vo);
        }
        return pool;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claimCoupon(Long templateId) {
        Long userId = AuthUserUtils.getUserId();
        CouponTemplate template = couponTemplateService.getById(templateId);
        AssertUtils.notNull(template, BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE);
        AssertUtils.isTrue(template.getStatus() == CouponStatus.ACTIVE.getCode(),
                BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE);
        AssertUtils.isTrue(template.getEndTime().isAfter(LocalDateTime.now()),
                BizErrorCode.COUPON_EXPIRED);

        String stockKey = COUPON_STOCK_KEY_PREFIX + templateId;
        String userClaimKey = COUPON_USER_CLAIM_KEY_PREFIX + userId + ":" + templateId;

        Long result = stringRedisTemplate.execute(CLAIM_SCRIPT, List.of(stockKey, userClaimKey),
                String.valueOf(template.getPerUserLimit()));

        if (result == null) {
            throw new BizException(BizErrorCode.COUPON_CLAIM_FAILED);
        }

        if (result == CouponClaimLuaResult.STOCK_NOT_ENOUGH.getCode()) {
            throw new BizException(BizErrorCode.COUPON_STOCK_NOT_ENOUGH);
        }
        if (result == CouponClaimLuaResult.REACH_LIMIT.getCode()) {
            throw new BizException(BizErrorCode.COUPON_REACH_LIMIT);
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setTemplateId(templateId);
        userCoupon.setUserId(userId);
        userCoupon.setStatus(CouponUserStatus.UNUSED.getCode());
        userCoupon.setExpireTime(template.getEndTime());
        userCoupon.setCreateTime(LocalDateTime.now());
        userCouponService.save(userCoupon);

        couponTemplateService.lambdaUpdate()
                .eq(CouponTemplate::getId, templateId)
                .setSql("issued_count = issued_count + 1")
                .update();

        log.info("用户领取优惠券成功, userId: {}, templateId: {}", userId, templateId);
        return true;
    }

    @Override
    public void lockCoupon(Long userCouponId, String orderNo) {
        boolean locked = userCouponService.lockCoupon(userCouponId, orderNo);
        AssertUtils.isTrue(locked, BizErrorCode.COUPON_NOT_AVAILABLE);
        log.info("优惠券锁定成功, userCouponId: {}, orderNo: {}", userCouponId, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(String orderNo) {
        UserCoupon userCoupon = userCouponService.lambdaQuery()
                .eq(UserCoupon::getOrderNo, orderNo)
                .eq(UserCoupon::getStatus, CouponUserStatus.LOCKED.getCode())
                .one();
        if (userCoupon == null) {
            return;
        }

        boolean used = userCouponService.useCoupon(orderNo);
        if (used) {
            CouponUsageLog log = new CouponUsageLog();
            log.setUserCouponId(userCoupon.getId());
            log.setUserId(userCoupon.getUserId());
            log.setOrderNo(orderNo);
            log.setCreateTime(LocalDateTime.now());
            couponUsageLogService.save(log);
        }
        log.info("优惠券使用成功, orderNo: {}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseCoupon(String orderNo) {
        boolean released = userCouponService.releaseCoupon(orderNo);
        if (released) {
            log.info("优惠券释放成功, orderNo: {}", orderNo);
        }
    }

    @Override
    public Map<Long, CouponCalcResult> calculateDiscount(
            Map<Long, Long> shopCouponIds,
            Map<Long, Long> shopTotalPrices,
            Map<Long, Map<Long, Long>> shopItemPrices
    ) {

        Map<Long, CouponCalcResult> results = new HashMap<>();

        for (Map.Entry<Long, Long> entry : shopCouponIds.entrySet()) {
            Long storeId = entry.getKey();
            Long userCouponId = entry.getValue();
            if (userCouponId == null) {
                continue;
            }

            UserCoupon userCoupon = userCouponService.getById(userCouponId);
            AssertUtils.notNull(userCoupon, BizErrorCode.COUPON_NOT_EXIST);
            AssertUtils.isTrue(userCoupon.getStatus() == CouponUserStatus.UNUSED.getCode(),
                    BizErrorCode.COUPON_NOT_AVAILABLE);

            CouponTemplate template = couponTemplateService.getById(userCoupon.getTemplateId());
            AssertUtils.notNull(template, BizErrorCode.COUPON_TEMPLATE_NOT_ACTIVE);

            Long orderTotalPrice = shopTotalPrices.get(storeId);
            AssertUtils.notNull(orderTotalPrice, BizErrorCode.ORDER_DATA_IS_NULL);

            if (template.getMinOrderAmount() != null && orderTotalPrice < template.getMinOrderAmount()) {
                throw new BizException(BizErrorCode.COUPON_NOT_MEET_MIN_AMOUNT);
            }

            long totalDiscount = calculateCouponDiscount(template, orderTotalPrice);

            Map<Long, Long> itemPrices = shopItemPrices.get(storeId);
            Map<Long, Long> itemDiscounts = allocateDiscount(totalDiscount, itemPrices);

            results.put(storeId, CouponCalcResult.builder()
                    .userCouponId(userCouponId)
                    .totalDiscount(totalDiscount)
                    .itemDiscounts(itemDiscounts)
                    .build());
        }

        return results;
    }

    private long calculateCouponDiscount(CouponTemplate template, long orderTotalPrice) {
        CouponType type = CouponType.of(template.getType());
        return switch (type) {
            case FIXED_AMOUNT -> template.getDiscountAmount();
            case PERCENTAGE -> {
                BigDecimal rate = BigDecimal.valueOf(template.getDiscountRate())
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                long discount = BigDecimal.valueOf(orderTotalPrice)
                        .multiply(BigDecimal.ONE.subtract(rate))
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValue();
                if (template.getMaxDiscountAmount() != null) {
                    discount = Math.min(discount, template.getMaxDiscountAmount());
                }
                yield discount;
            }
            case FREE_SHIPPING -> 0L;
        };
    }

    private Map<Long, Long> allocateDiscount(long totalDiscount, Map<Long, Long> itemPrices) {
        if (totalDiscount <= 0 || CollUtil.isEmpty(itemPrices)) {
            return new HashMap<>();
        }

        long totalPrice = itemPrices.values().stream().mapToLong(Long::longValue).sum();
        Map<Long, Long> result = new HashMap<>();
        long allocated = 0;

        Long maxPriceSkuId = null;
        long maxPrice = 0;
        for (Map.Entry<Long, Long> e : itemPrices.entrySet()) {
            if (e.getValue() > maxPrice) {
                maxPrice = e.getValue();
                maxPriceSkuId = e.getKey();
            }
        }

        for (Map.Entry<Long, Long> entry : itemPrices.entrySet()) {
            Long skuId = entry.getKey();
            Long itemPrice = entry.getValue();
            long discount = BigDecimal.valueOf(totalDiscount)
                    .multiply(BigDecimal.valueOf(itemPrice))
                    .divide(BigDecimal.valueOf(totalPrice), 0, RoundingMode.HALF_UP)
                    .longValue();
            result.put(skuId, discount);
            allocated += discount;
        }

        long diff = totalDiscount - allocated;
        if (diff != 0 && maxPriceSkuId != null) {
            result.merge(maxPriceSkuId, diff, Long::sum);
        }

        return result;
    }
}
