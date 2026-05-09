package com.cloudmall.framework.models.goods.application;

import com.cloudmall.framework.exception.BizException;
import com.cloudmall.framework.models.category.vo.CategoryGoodsSectionVO;
import com.cloudmall.framework.models.goods.application.vo.GoodsDetailVO;
import com.cloudmall.framework.models.goods.application.vo.WebGoodsDetailVO;
import com.cloudmall.framework.models.goods.spu.Goods;

import java.util.List;

/**
 * 商品应用服务接口
 * 定义商品聚合相关的应用服务操作
 *
 * @author Tomatos
 * @date 2026/1/6
 */
public interface IGoodsAppService {
    /**
     * 发布商品（创建或更新）
     * <p>
     * 用于审核通过后或直接发布商品到平台
     * <p>
     * 处理流程：
     * 1. 根据 goodsId 判断是新增还是更新
     * 2. 构建 Goods 实体（计算最低/最高价格）
     * 3. 保存 Goods 到数据库
     * 4. 保存规格、规格值和 SKU 到数据库
     * <p>
     * 规格和规格值的处理：
     * - 自动查询或创建不存在的规格
     * - 自动查询或创建不存在的规格值
     * - 创建 SKU 与规格值的多对多关联
     *
     * @param command 商品发布命令，包含发布所需的所有数据
     * @return 发布后的商品对象
     * @throws BizException 如果发布失败
     */
    Goods publishGoods(GoodsPublishCommand command);

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
     * 按分类ID查询商品（包含子分类）
     * 递归查询指定分类及其所有子分类下的商品
     *
     * @param categoryId 分类ID
     * @param limit      查询结果数量限制
     * @return 商品列表
     */
    List<Goods> queryGoodsByCategoryId(Long categoryId, int limit);

    /**
     * 获取分类及其商品信息
     * 查询所有一级分类及其下的二级分类，并为每个二级分类获取对应的商品列表
     * 这是分类模块和商品模块的跨模块交互操作
     *
     * @return 分类商品区域列表，包含分类和商品映射
     */
    List<CategoryGoodsSectionVO> getCategoryGoodsSections();

    WebGoodsDetailVO getWebGoodsDetail(Long id);
}