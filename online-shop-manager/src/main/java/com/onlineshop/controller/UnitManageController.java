package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.common.entity.PageParamsDTO;
import com.onlineshop.framework.models.goods.unit.IUnitService;
import com.onlineshop.framework.models.goods.unit.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 单位管理控制器
 *
 * @author Tomatos
 * @date 2026/01/05
 */
@RestController
@RequestMapping("/units")
@PreAuthorize("hasAuthority('unit:view')")
public class UnitManageController {

    @Autowired
    private IUnitService unitService;

    /**
     * 分页查询单位列表
     *
     * @return 分页结果
     */
    @GetMapping
    public IPage<Unit> pageUnits(PageParamsDTO pageParamsDTO) {
        return unitService.pageUnits(pageParamsDTO);
    }

    /**
     * 获取单位列表（不分页）
     *
     * @return 单位列表
     */
    @GetMapping("/list")
    public List<Unit> units() {
        return unitService.list();
    }

    /**
     * 创建单位
     *
     * @param unit 单位信息
     */
    @PostMapping
    @PreAuthorize("hasAuthority('unit:add')")
    public void createUnit(@RequestBody Unit unit) {
        unitService.addUnit(unit);
    }

    /**
     * 获取单位详情
     *
     * @param id 单位ID
     * @return 单位信息
     */
    @GetMapping("/{id}")
    public Unit getUnit(@PathVariable Long id) {
        return unitService.getById(id);
    }

    /**
     * 更新单位信息
     *
     * @param id 单位ID
     * @param unit 单位信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('unit:edit')")
    public void updateUnit(@PathVariable Long id, @RequestBody Unit unit) {
        unit.setId(id);
        unitService.updateUnit(unit);
    }

    /**
     * 更新单位状态
     *
     * @param id 单位ID
     * @param unit 单位信息
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('unit:edit')")
    public void updateUnitStatus(@PathVariable Long id, @RequestBody Unit unit) {
        unitService.updateStatus(id, unit.getStatus());
    }

    /**
     * 删除单位
     *
     * @param id 单位ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('unit:delete')")
    public void deleteUnit(@PathVariable Long id) {
        unitService.batchRemoveUnit(Collections.singletonList(id));
    }

    /**
     * 批量删除单位
     *
     * @param ids 单位ID列表
     */
    @DeleteMapping
    @PreAuthorize("hasAuthority('unit:delete')")
    public void batchDeleteUnits(@RequestBody List<Long> ids) {
        unitService.batchRemoveUnit(ids);
    }
}