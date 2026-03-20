package com.onlineshop.controller;

import com.onlineshop.framework.common.entity.PageParamsDTO;
import com.onlineshop.framework.models.goods.unit.IUnitService;
import com.onlineshop.framework.models.goods.unit.Unit;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/11
 */
@RestController
@RequestMapping("/merchant/units")
@RequiredArgsConstructor
public class UnitMerchantController {
    private final IUnitService unitService;

    @GetMapping("/list")
    public List<Unit> listUnits() {
        return unitService.getAllUnit();
    }
}
