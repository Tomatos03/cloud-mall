package com.onlineshop.framework.models.goods.application;

import com.onlineshop.framework.models.goods.application.vo.GoodsDetailVO;
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
     * 提交商品审核申请（新增或更新）
     * 将商品信息序列化为 JSON，保存到审核表等待审核
     * 审核通过后调用 publishGoodsAfterAudit 进行实际保存或更新
     *
     * @param payload 商品发布请求对象（新商品 goodsId=null，更新商品包含 goodsId）
     */
    void submitGoodsAudit(GoodsDTO payload);

    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    void deleteGoods(Long id);

    /**
     * 根据审核记录实际保存或更新商品
     * 从审核表恢复商品信息，进行实际的数据库保存或更新操作
     *
     * @param auditId 审核记录ID
     */
    void updateGoodsAfterAudit(Long auditId);

    /**
     * 获取商品详情（展示模式）
     *
     * @param id 商品ID
     * @return 商品详情
     */
    GoodsDetailVO getGoodsDetail(Long id);

    /**
     * 重新发布处于撤销状态的审核商品
     * 直接修改已有审核记录的扩展信息字段（extraInfo），将新的商品信息序列化为JSON后保存
     * 无需创建新的审核记录，复用原有记录ID
     *
     * @param auditId 被撤销的审核记录ID
     * @param payload 新的商品发布请求对象
     */
    void republishGoodsFromAudit(Long auditId, GoodsDTO payload);

    WebGoodsDetailVO getWebGoodsDetail(Long id);
}