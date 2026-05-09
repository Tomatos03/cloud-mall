package com.cloudmall.framework.mq.consumer.goods;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.cloudmall.framework.event.MQTag;
import com.cloudmall.framework.models.search.service.IGoodsEsService;

/**
 * 商品从 ES 删除消费者
 *
 * @author : Tomatos
 * @date : 2026/3/21
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.name-server")
@RocketMQMessageListener(
        topic = "${mq.topic.goods}",
        selectorExpression = MQTag.GOODS_DELETE_FROM_ES,
        consumerGroup = "${mq.group.goods-delete}"
)
public class GoodsDeleteFromEsConsumer implements RocketMQListener<Long> {
    private final IGoodsEsService goodsEsService;

    @Override
    public void onMessage(Long goodsId) {
        log.info("收到商品删除 ES 消息, goodsId: {}", goodsId);
        if (goodsId == null) {
            log.warn("商品删除 ES 消息无效, goodsId: null");
            return;
        }

        try {
            goodsEsService.deleteGoodsIndex(goodsId);
            log.info("删除商品索引成功, goodsId: {}", goodsId);
        } catch (Exception e) {
            log.error("处理商品删除 ES 消息失败, goodsId: {}", goodsId, e);
            throw new RuntimeException("处理商品删除 ES 消息失败", e);
        }
    }
}
