package com.onlineshop.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.unit.IUnitService;
import com.onlineshop.framework.models.unit.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员单位管理 Controller
 *
 * @author Tomatos
 * @date 2026/01/05
 */
@RestController
@RequestMapping("/manager/admin/units")
public class AdminUnitManagerController {

    @Autowired
    private IUnitService unitService;

    /**
     * 分页查询单位列表
     *
     * @param page 页码，从1开始
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping
    public IPage<Unit> listUnits(@RequestParam int page, @RequestParam int pageSize) {
        return unitService.pageUnits(page, pageSize);
    }

    /**
     * 创建单位
     *
     * @param unit 单位信息
     * @return 创建的单位
     */
    @PostMapping
    public Unit createUnit(@RequestBody Unit unit) {
        unitService.save(unit);
        return unit;
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
     * @return 更新后的单位
     */
    @PutMapping("/{id}")
    public Unit updateUnit(@PathVariable Long id, @RequestBody Unit unit) {
        unit.setId(id);
        unitService.updateById(unit);
        return unit;
    }

    /**
     * 更新单位状态
     *
     * @param id 单位ID
     * @return 更新后的单位
     */
    @PatchMapping("/{id}/status")
    public Unit updateUnitStatus(@PathVariable Long id, @RequestBody Unit unit) {
        unitService.updateStatus(id, unit.getStatus());
        return unitService.getById(id);
    }

    /**
     * 删除单位
     *
     * @param id 单位ID
     */
    @DeleteMapping("/{id}")
    public void deleteUnit(@PathVariable Long id) {
        unitService.removeById(id);
    }

    /**
     * 批量删除单位
     *
     * @param body 请求体，包含ids数组
     */
    @DeleteMapping
    public void batchDeleteUnits(@RequestBody Map<String, List<String>> body) {
        List<String> idsStr = body.get("ids");
        List<Long> ids = idsStr.stream().map(Long::valueOf).toList();
        unitService.removeByIds(ids);
    }
}
