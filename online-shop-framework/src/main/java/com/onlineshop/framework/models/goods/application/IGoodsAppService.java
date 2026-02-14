package com.onlineshop.framework.models.goods.application;

import com.onlineshop.framework.models.goods.application.vo.GoodsDetailVO;
import com.onlineshop.framework.models.goods.application.vo.GoodsDetailWithAuditVO;
import com.onlineshop.framework.models.goods.application.vo.WebGoodsDetailVO;

/**
 * 商品应用服务接口
 * 定义商品聚合相关的应用服务操作
 *
 * @author Tomatos
 * @date 2026/1/6
 */
public interface IGoodsAppService {
    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    void deleteGoods(Long id);

    /**
     * 获取商品详情（展示模式）
     *
     * @param id 商品ID
     * @return 商品详情
     */
    GoodsDetailVO queryGoodsDetail(Long id);

    /**
     * 获取商品详情（包含审核信息）
     * 用于商家端查看商品详情及其关联的审核信息
     *
     * @param id 商品ID
     * @return 商品详情和审核信息
     */
    GoodsDetailWithAuditVO getGoodsDetailWithAudit(Long id);

    /**
     * 重新发布处于撤销状态的审核商品
     *
     * @param auditId 被撤销的审核记录ID
     * @param payload 新的商品发布请求对象
     */
    void republishGoodsFromAudit(Long auditId, GoodsDTO payload);

    WebGoodsDetailVO getWebGoodsDetail(Long id);
}