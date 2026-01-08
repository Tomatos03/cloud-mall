package com.onlineshop.framework.models.goods.application;

import com.onlineshop.framework.models.goods.spu.dto.GoodsPublishPayload;
import com.onlineshop.framework.models.goods.spu.vo.GoodsItemVO;

/**
 * 商品应用服务接口
 * 定义商品聚合相关的应用服务操作
 *
 * @author Tomatos
 * @date 2026/1/6
 */
public interface IGoodsAppService {

    /**
     * 新增商品
     * @param payload 商品发布请求对象
     */
    void publishGoods(GoodsPublishPayload payload);

    /**
     * 删除商品
     * @param id 商品ID
     */
    void deleteGoods(Long id);

    /**
     * 更新商品
     * @param payload 商品发布请求对象（包含id）
     */
    void updateGoods(GoodsPublishPayload payload);

    /**
     * 获取商品详情（编辑模式）
     * @param id 商品ID
     * @return 商品详情
     */
    GoodsItemVO getGoodsItem(Long id);
}
