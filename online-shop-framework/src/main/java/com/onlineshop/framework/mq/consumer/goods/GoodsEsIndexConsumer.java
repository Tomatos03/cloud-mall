package com.onlineshop.framework.mq.consumer.goods;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.onlineshop.framework.event.MQTag;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.search.index.GoodsIndex;
import com.onlineshop.framework.models.search.service.IGoodsEsService;

/**
 * 商品 ES 索引消费者
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
        selectorExpression = MQTag.GOODS_SYNC_TO_ES + " || " + MQTag.GOODS_DELETE_FROM_ES,
        consumerGroup = "${mq.group.goods}"
)
public class GoodsEsIndexConsumer implements RocketMQListener<Message<Long>> {
    private final IGoodsEsService goodsEsService;
    private final IGoodsService goodsService;

    @Override
    public void onMessage(Message<Long> message) {
        Long goodsId = message.getPayload();
        String tag = resolveTag(message);
        if (goodsId == null) {
            log.warn("商品索引消息无效, tag: {}, goodsId: null", tag);
            return;
        }

        if (MQTag.GOODS_SYNC_TO_ES.equals(tag)) {
            syncToEs(goodsId);
            return;
        }
        if (MQTag.GOODS_DELETE_FROM_ES.equals(tag)) {
            deleteFromEs(goodsId);
            return;
        }

        log.warn("未识别的商品索引消息标签, tag: {}, goodsId: {}", tag, goodsId);
    }

    private void syncToEs(Long goodsId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            log.warn("同步商品索引失败, 商品不存在, goodsId: {}", goodsId);
            return;
        }
        GoodsIndex goodsIndex = GoodsIndex.convertToGoodsIndex(goods);
        goodsEsService.saveGoodsIndex(goodsIndex);
        log.info("同步商品索引成功, goodsId: {}", goodsId);
    }

    private void deleteFromEs(Long goodsId) {
        goodsEsService.deleteGoodsIndex(goodsId);
        log.info("删除商品索引成功, goodsId: {}", goodsId);
    }

    private String resolveTag(Message<Long> message) {
        Object tag = message.getHeaders().get(RocketMQHeaders.TAGS);
        return tag == null ? null : tag.toString();
    }
}
