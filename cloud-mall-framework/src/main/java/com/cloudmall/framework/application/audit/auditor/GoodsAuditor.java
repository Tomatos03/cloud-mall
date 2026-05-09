package com.cloudmall.framework.application.audit.auditor;

import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.event.MQTag;
import com.cloudmall.framework.event.MQTopicProperties;
import com.cloudmall.framework.event.TransactionCommitSendMQEvent;
import com.cloudmall.framework.models.audit.dto.AuditSubmitDTO;
import com.cloudmall.framework.models.audit.dto.GoodsAuditItemDTO;
import com.cloudmall.framework.models.audit.entity.Audit;
import com.cloudmall.framework.models.audit.entity.AuditItem;
import com.cloudmall.framework.models.audit.enums.AuditBizType;
import com.cloudmall.framework.models.audit.enums.AuditItemStatus;
import com.cloudmall.framework.models.category.Category;
import com.cloudmall.framework.models.category.ICategoryService;
import com.cloudmall.framework.models.goods.application.GoodsPublishCommand;
import com.cloudmall.framework.models.goods.application.IGoodsAppService;
import com.cloudmall.framework.models.goods.sku.SkuDTO;
import com.cloudmall.framework.models.goods.spu.Goods;
import com.cloudmall.framework.models.goods.unit.IUnitService;
import com.cloudmall.framework.models.goods.unit.Unit;
import com.cloudmall.framework.models.store.IStoreService;
import com.cloudmall.framework.models.store.Store;
import com.cloudmall.framework.utils.AssertUtils;
import com.cloudmall.framework.utils.money.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 商品审核处理器
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsAuditor extends AbstractAuditor<GoodsAuditItemDTO> {
    private final IGoodsAppService goodsAppService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MQTopicProperties mqTopicProperties;
    private final ICategoryService categoryService;
    private final IUnitService unitService;
    private final IStoreService storeService;

    @Override
    protected void validateAndFill(AuditSubmitDTO<GoodsAuditItemDTO> submitDTO) {
        Collection<GoodsAuditItemDTO> items = submitDTO.getItems();
        log.info("验证和填充商品审核项，共 {} 个", items.size());

        for (GoodsAuditItemDTO goods : items) {
            Category category = categoryService.getById(goods.getCategoryId());
            AssertUtils.notNull(category, BizErrorCode.CATEGORY_NOT_EXIST_OR_NO_ENABLE);

            String categoryPath = categoryService.buildCategoryPathByLeafCategoryId(goods.getCategoryId());
            goods.setCategoryIdPath(categoryPath);

            Unit unit = unitService.getById(goods.getUnitId());
            AssertUtils.notNull(unit, BizErrorCode.GOODS_UNIT_NOT_EXIST);
            goods.setUnitName(unit.getName());

            Store store = storeService.getById(goods.getStoreId());
            AssertUtils.notNull(store, BizErrorCode.STORE_NOT_EXIST);
            goods.setStoreName(store.getName());

            fillPriceRange(goods);
        }

        log.info("商品审核项验证和填充完成");
    }


    /**
     * 批量处理商品审核决策
     * <p>
     * 逻辑：
     * 1. 遍历所有审核项
     * 2. 对于通过的项：发布商品
     * 3. 对于拒绝的项：仅记录（可选业务处理）
     *
     * @param audit 审核批次
     * @param items 批次中的所有项（已按审核决策更新状态）
     */
    @Override
    protected void onProcessed(Audit audit, List<AuditItem> items) {
        Long auditId = audit.getId();
        log.info("处理商品审核结果，批次ID: {}，项数: {}", auditId, items.size());

        for (AuditItem item : items) {
            if (AuditItemStatus.APPROVED.getCode()
                                        .equals(item.getStatus())) {
                GoodsAuditItemDTO goodsItem = parseSnapshot(item.getSnapshot(), GoodsAuditItemDTO.class);
                GoodsPublishCommand command = convertToGoodsPublishCommand(goodsItem);
                Goods goods = goodsAppService.publishGoods(command);
                applicationEventPublisher.publishEvent(
                        new TransactionCommitSendMQEvent(
                                mqTopicProperties.getGoods(),
                                MQTag.GOODS_SYNC_TO_ES,
                                goods.getId()
                        )
                );
            }
        }

        log.info("商品审核结果处理完成，批次ID: {}", auditId);
    }

    @Override
    protected boolean support(AuditBizType auditBizType) {
        return AuditBizType.GOODS == AuditBizType.of(auditBizType.getCode());
    }

    private GoodsPublishCommand convertToGoodsPublishCommand(GoodsAuditItemDTO item) {
        return GoodsPublishCommand.builder()
                                  .goodsId(item.getGoodsId())
                                  .goodsName(item.getGoodsName())
                                  .categoryId(item.getCategoryId())
                                  .unitId(item.getUnitId())
                                  .unitName(item.getUnitName())
                                  .sellPoint(item.getSellPoint())
                                  .displayImageUrls(item.getDisplayImageUrls())
                                  .descriptionImageUrls(item.getDescriptionImageUrls())
                                  .storeId(item.getStoreId())
                                  .storeName(item.getStoreName())
                                  .status(item.getStatus())
                                  .specifications(item.getSpecifications())
                                  .skus(item.getSkus())
                                  .build();
    }

    /**
     * 计算并填充商品SKU价格区间（单位：分）
     */
    private void fillPriceRange(GoodsAuditItemDTO item) {
        AssertUtils.notEmpty(item.getSkus(), BizErrorCode.SKUS_CANNOT_BE_EMPTY);

        Long minPrice = null;
        Long maxPrice = null;
        for (SkuDTO sku : item.getSkus()) {
            long currentPrice = Money.ofYuan(sku.getPrice())
                                     .getCents();
            if (minPrice == null || currentPrice < minPrice) {
                minPrice = currentPrice;
            }
            if (maxPrice == null || currentPrice > maxPrice) {
                maxPrice = currentPrice;
            }
        }
        item.setMinPrice(Money.ofCents(minPrice)
                              .toYuanString());
        item.setMaxPrice(Money.ofCents(maxPrice)
                              .toYuanString());
    }
}
