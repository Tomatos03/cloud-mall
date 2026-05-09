package com.cloudmall.framework.models.goods.spec.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/20
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationVO {
    private String name;

    private List<SpecValueVO> values;
}