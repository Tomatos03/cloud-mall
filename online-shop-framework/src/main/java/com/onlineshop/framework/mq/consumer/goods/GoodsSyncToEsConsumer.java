package com.onlineshop.framework.mq.consumer.goods;

import com.onlineshop.framework.models.goods.spu.IGoodsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.search.index.GoodsIndex;
import com.onlineshop.framework.models.search.service.IGoodsEsService;

/**
 * 同步商品到 ES 索引消费者
 *
 * @author : Tomatos
 * @date : 2026/3/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.name-server")
@RocketMQMessageListener(
        topic = "${mq.topic.goods}",
        selectorExpression = MQTag.GOODS_SYNC_TO_ES,
        consumerGroup = "${mq.group.goods}"
)
public class GoodsSyncToEsConsumer implements RocketMQListener<Long> {
    private final IGoodsEsService goodsEsService;
    private final IGoodsService goodsService;

    @Override
    public void onMessage(Long goodsId) {
        Goods goods = goodsService.getById(goodsId);
        GoodsIndex goodsIndex = GoodsIndex.convertToGoodsIndex(goods);
        goodsEsService.saveGoodsIndex(goodsIndex);
        log.info("同步商品索引成功, goodsId: {}", goodsId);
    }
}
