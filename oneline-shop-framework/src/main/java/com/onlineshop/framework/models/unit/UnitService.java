package com.onlineshop.framework.models.unit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @CacheEvict(value = "unit", allEntries = true)
    @Override
    public boolean updateStatus(Long id, Integer status) {
        Unit unit = this.getById(id);
        if (unit != null) {
            unit.setStatus(status);
            return this.updateById(unit);
        }
        return false;
    }

    @Cacheable(value = "unit", key = "'all'")
    @Override
    public List<Unit> getAllUnit() {
        return lambdaQuery().eq(Unit::getStatus, 1)
                            .list();
    }

    @CacheEvict(value = "unit", allEntries = true)
    @Override
    public void addUnit(Unit unit) {
        save(unit);
    }

    @CacheEvict(value = "unit", allEntries = true)
    @Override
    public void updateUnit(Unit unit) {
        updateById(unit);
    }

    @CacheEvict(value = "unit", allEntries = true)
    @Transactional(rollbackFor =  Exception.class)
    @Override
    public void batchRemoveUnit(List<Long> ids) {
        removeBatchByIds(ids);
    }
}
