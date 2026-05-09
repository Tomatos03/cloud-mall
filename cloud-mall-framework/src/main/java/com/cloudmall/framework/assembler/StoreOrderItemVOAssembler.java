package com.cloudmall.framework.assembler;

import com.cloudmall.framework.models.order.entity.OrderItem;
import com.cloudmall.framework.models.order.utils.OrderUtil;
import com.cloudmall.framework.models.order.vo.StoreOrderItemVO;
import com.cloudmall.framework.utils.money.Money;

/**
 * StoreOrderItemVO 组装器
 */
public final class StoreOrderItemVOAssembler {

    private StoreOrderItemVOAssembler() {
    }

    /**
     * OrderItem -> StoreOrderItemVO
     */
    public static StoreOrderItemVO assembler(OrderItem item) {
        return StoreOrderItemVO.builder()
                               .orderItemId(item.getId())
                               .goodsId(item.getGoodsId())
                               .goodsName(item.getGoodsName())
                               .goodsMainImageUrl(item.getGoodsMainImageUrl())
                               .goodsPrice(Money.ofCents(item.getGoodsPrice()).toYuanString())
                               .quantity(item.getQuantity())
                               .originalPrice(item.getOriginalPrice() != null ? Money.ofCents(item.getOriginalPrice()).toYuanString() : null)
                               .discountAmount(item.getDiscountAmount() != null ? Money.ofCents(item.getDiscountAmount()).toYuanString() : null)
                               .totalPrice(Money.ofCents(item.getTotalPrice()).toYuanString())
                               .commentStatus(item.getCommentStatus())
                               .selectedSpecs(OrderUtil.parseSkuSpecs(item.getSkuSpecs()))
                               .build();
    }
}
