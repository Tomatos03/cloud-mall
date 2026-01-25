package com.onlineshop.controller.merchant;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.models.unit.IUnitService;
import com.onlineshop.framework.models.unit.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/5
 */
@RestController
@RequestMapping("/manager/merchant/units")
public class MerchantUnitController {

    @Autowired
    private IUnitService unitService;

    /**
     * 分页查询单位列表
     *
     * @return 分页结果
     */
    @GetMapping
    public IPage<Unit> pageUnits(
            @RequestParam("page") int page,
            @RequestParam("pageSize") int size
    ) {
        IPage<Unit> pageObj = new Page<>(page, size);
        return unitService.page(pageObj);
    }

    @GetMapping("/list")
    public List<Unit> units() {
        return unitService.list();
    }
}