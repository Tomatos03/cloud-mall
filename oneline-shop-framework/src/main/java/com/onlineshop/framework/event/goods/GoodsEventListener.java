package com.onlineshop.framework.event.goods;

import com.onlineshop.framework.models.search.index.GoodsIndex;
import com.onlineshop.framework.models.search.service.IGoodsEsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 商品事件监听器
 *
 * 目前仅提供接收事件的骨架，具体同步到 Elasticsearch 等业务逻辑后续补充实现。
 *
 * @author : Tomatos
 * @date : 2026/1/27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsEventListener {
    private final IGoodsEsService goodsEsService;

    /**
     * 监听同步商品到 ES 的事件
     * 在事务提交后执行，防止事务回滚导致的数据不一致
     *
     * @param event 同步商品到 ES 事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSyncGoodsToEs(SyncGoodsToEsEvent event) {
        goodsEsService.saveGoodsIndex(GoodsIndex.convertToGoodsIndex(event.getGoods()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDelGoodsFromEs(DelGoodsFromEsEvent event) {
        goodsEsService.deleteGoodsIndex(event.getGoodsId());
    }
}