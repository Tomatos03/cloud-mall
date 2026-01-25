package com.onlineshop.framework.models.goods.application.vo;

import com.onlineshop.framework.models.goods.sku.SelectedSkuDTO;
import com.onlineshop.framework.models.goods.spec.vo.SpecificationVO;
import com.onlineshop.framework.models.goods.spu.vo.WebSpuVO;
import com.onlineshop.framework.models.store.vo.StoreInfoVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/23
 */
@Data
@AllArgsConstructor
@Builder
public class WebGoodsDetailVO {
    private WebSpuVO spu;
    private StoreInfoVO storeInfo;
    private List<SpecificationVO> specifications;
    private List<SelectedSkuDTO> skus;
}