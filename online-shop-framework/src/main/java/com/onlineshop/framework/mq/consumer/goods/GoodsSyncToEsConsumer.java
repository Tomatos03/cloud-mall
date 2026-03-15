package com.onlineshop.framework.mq.consumer.goods;

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
public class GoodsSyncToEsConsumer implements RocketMQListener<Goods> {
    private final IGoodsEsService goodsEsService;

    @Override
    public void onMessage(Goods message) {
        if (message == null || message.getId() == null || message.getCreateTime() == null) {
            log.warn("同步商品索引消息无效, message: {}", message);
            return;
        }

        GoodsIndex goodsIndex = GoodsIndex.convertToGoodsIndex(message);
        goodsEsService.saveGoodsIndex(goodsIndex);
        log.info("同步商品索引成功, goodsId: {}", message.getId());
    }
}
