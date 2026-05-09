package com.cloudmall.framework.models.search.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.framework.models.goods.spu.dto.GoodsSearchDTO;
import com.cloudmall.framework.models.goods.spu.vo.GoodsCardVO;

/**
 * 商品搜索应用服务接口
 * 提供商品搜索功能
 *
 * @author : Tomatos
 * @date : 2025/1/1
 */
public interface ISearchAppService {

    /**
     * 搜索商品
     *
     * @param searchDTO 搜索条件DTO
     * @return 分页的商品卡片VO列表
     */
    IPage<GoodsCardVO> searchGoods(GoodsSearchDTO searchDTO);
}