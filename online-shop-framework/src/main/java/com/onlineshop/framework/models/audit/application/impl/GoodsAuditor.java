package com.onlineshop.framework.models.audit.application.impl;

import com.alibaba.fastjson2.JSON;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.event.goods.SyncGoodsToEsEvent;
import com.onlineshop.framework.models.audit.application.AbstractAuditor;
import com.onlineshop.framework.models.audit.domain.GoodsAuditRequest;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.goods.application.GoodsPublishCommand;
import com.onlineshop.framework.models.goods.application.IGoodsAppService;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 商品审核处理器
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Component
@RequiredArgsConstructor
public class GoodsAuditor extends AbstractAuditor<GoodsAuditRequest> {
    private final IStoreService storeService;
    private final IGoodsAppService goodsAppService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    protected boolean support(AuditType type) {
        return AuditType.GOODS == type;
    }

    @Override
    protected void validateRequest(GoodsAuditRequest request) {
        AssertUtils.isTrue(request.getStoreId() != null && request.getStoreId() > 0, BizErrorCode.INVALID_PARAM);

        Store store = storeService.getById(request.getStoreId());
        AssertUtils.notNull(store, BizErrorCode.STORE_NOT_EXIST);
        request.setApplicantName(store.getName());
    }

    @Override
    protected Long onApproved(GoodsAuditRequest request) {
        Goods goods = goodsAppService.publishGoods(convertToGoodsPublishCommand(request));
        eventPublisher.publishEvent(SyncGoodsToEsEvent.builder().goods(goods).build());
        return goods.getId();
    }

    private GoodsPublishCommand convertToGoodsPublishCommand(GoodsAuditRequest request) {
        return GoodsPublishCommand.builder()
                                  .goodsId(request.getGoodsId())
                                  .goodsName(request.getGoodsName())
                                  .categoryId(request.getCategoryId())
                                  .unitId(request.getUnitId())
                                  .unitName(request.getUnitName())
                                  .sellPoint(request.getSellPoint())
                                  .displayImageUrls(request.getDisplayImageUrls())
                                  .descriptionImageUrls(request.getDescriptionImageUrls())
                                  .storeId(request.getStoreId())
                                  .storeName(request.getStoreName())
                                  .status(request.getStatus())
                                  .specifications(request.getSpecifications())
                                  .skus(request.getSkus())
                                  .build();
    }

    @Override
    protected String generateSnapshot(GoodsAuditRequest request) {
        return JSON.toJSONString(request);
    }

    @Override
    protected GoodsAuditRequest rebuildRequest(String snapshot) {
        return JSON.parseObject(snapshot, GoodsAuditRequest.class);
    }
}
