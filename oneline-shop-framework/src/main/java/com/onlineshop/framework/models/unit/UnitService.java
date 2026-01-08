package com.onlineshop.framework.models.unit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 单位服务实现类
 *
 * @author Tomatos
 * @date 2026/01/05
 */
@Service
public class UnitService extends ServiceImpl<UnitMapper, Unit> implements IUnitService {

    @Override
    public IPage<Unit> pageUnits(int page, int pageSize) {
        Page<Unit> pageRequest = new Page<>(page, pageSize);
        return this.page(pageRequest);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Unit unit = this.getById(id);
        if (unit != null) {
            unit.setStatus(status);
            return this.updateById(unit);
        }
        return false;
    }
}
