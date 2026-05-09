package com.cloudmall.framework.models.search.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.framework.models.goods.spu.dto.GoodsSearchDTO;
import com.cloudmall.framework.models.goods.spu.vo.GoodsCardVO;
import com.cloudmall.framework.models.search.service.IGoodsEsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 商品搜索应用服务实现
 * 通过ES提供商品搜索功能
 *
 * @author : Tomatos
 * @date : 2025/1/1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchAppService implements ISearchAppService {
    private final IGoodsEsService searchService;

    @Override
    public IPage<GoodsCardVO> searchGoods(GoodsSearchDTO searchDTO) {
        return searchService.searchGoods(searchDTO);
    }
}