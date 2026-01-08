package com.onlineshop.framework.models.unit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 单位服务接口
 *
 * @author Tomatos
 * @date 2026/01/05
 */
public interface IUnitService extends IService<Unit> {
    /**
     * 分页查询单位列表
     *
     * @param page 页码，从1开始
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<Unit> pageUnits(int page, int pageSize);

    /**
     * 更新单位状态
     *
     * @param id 单位ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}
