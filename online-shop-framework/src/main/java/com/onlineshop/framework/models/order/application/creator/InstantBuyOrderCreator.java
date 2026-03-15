package com.onlineshop.framework.models.order.application.creator;

import com.onlineshop.framework.event.MQTopicProperties;
import com.onlineshop.framework.models.address.IAddressService;
import com.onlineshop.framework.models.cart.PurchaseMode;
import com.onlineshop.framework.models.goods.sku.IGoodsSkuService;
import com.onlineshop.framework.models.goods.spec.service.IGoodsSkuSpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecService;
import com.onlineshop.framework.models.goods.spec.service.ISpecValueService;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.store.IStoreService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 立即购买下单创建器
 *
 * @author : Tomatos
 * @date : 2026/03/10
 */
@Component
public class InstantBuyOrderCreator extends AbstractOrderCreator {

    public InstantBuyOrderCreator(
            IOrderService orderService,
            IOrderItemService orderItemService,
            IAddressService addressService,
            IGoodsService goodsService,
            IGoodsSkuService goodsSkuService,
            IStoreService storeService,
            ApplicationEventPublisher applicationEventPublisher,
            MQTopicProperties mqTopicProperties
    ) {
        super(
                orderService,
                orderItemService,
                addressService,
                goodsService,
                goodsSkuService,
                storeService,
                applicationEventPublisher,
                mqTopicProperties
        );
    }

    @Override
    public PurchaseMode getSupportPurchaseMode() {
        return PurchaseMode.INSTANT_BUY;
    }
}
