package com.onlineshop.framework.models.goods.spec.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class SpecValueVO {
    private Long id;
    private String name;
}