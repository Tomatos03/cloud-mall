package com.cloudmall.framework.mq.consumer.goods;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.cloudmall.framework.event.MQTag;
import com.cloudmall.framework.models.goods.spu.Goods;
import com.cloudmall.framework.models.goods.spu.IGoodsService;
import com.cloudmall.framework.models.search.index.GoodsIndex;
import com.cloudmall.framework.models.search.service.IGoodsEsService;

/**
 * 商品同步到 ES 消费者
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
        selectorExpression = MQTag.GOODS_SYNC_TO_ES,
        consumerGroup = "${mq.group.goods-sync}"
)
public class GoodsSyncToEsConsumer implements RocketMQListener<Long> {
    private final IGoodsEsService goodsEsService;
    private final IGoodsService goodsService;

    @Override
    public void onMessage(Long goodsId) {
        log.info("收到商品同步 ES 消息, goodsId: {}", goodsId);
        if (goodsId == null) {
            log.warn("商品同步 ES 消息无效, goodsId: null");
            return;
        }

        try {
            Goods goods = goodsService.getById(goodsId);
            if (goods == null) {
                log.warn("同步商品索引失败, 商品不存在, goodsId: {}", goodsId);
                return;
            }
            GoodsIndex goodsIndex = GoodsIndex.convertToGoodsIndex(goods);
            goodsEsService.saveGoodsIndex(goodsIndex);
            log.info("同步商品索引成功, goodsId: {}", goodsId);
        } catch (Exception e) {
            log.error("处理商品同步 ES 消息失败, goodsId: {}", goodsId, e);
            throw new RuntimeException("处理商品同步 ES 消息失败", e);
        }
    }
}
