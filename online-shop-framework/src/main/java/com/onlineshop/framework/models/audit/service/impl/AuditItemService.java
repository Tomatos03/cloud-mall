package com.onlineshop.framework.models.audit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.audit.entity.AuditItem;
import com.onlineshop.framework.models.audit.mapper.AuditItemMapper;
import com.onlineshop.framework.models.audit.service.IAuditItemService;
import com.onlineshop.framework.models.audit.vo.AuditItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 审核项目服务实现
 */
@Service
@Slf4j
public class AuditItemService extends ServiceImpl<AuditItemMapper, AuditItem> implements IAuditItemService {

    // ==================== 查询方法 ====================

    @Override
    public List<AuditItem> queryAuditItems(Long auditId) {
        log.info("查询审核项目列表，批次ID: {}", auditId);
        LambdaQueryWrapper<AuditItem> wrapper = new LambdaQueryWrapper<AuditItem>()
                .eq(AuditItem::getAuditId, auditId);
        return list(wrapper);
    }

    @Override
    public List<AuditItemVO> getAuditById(Long auditId) {
        log.info("查询审核项目VO列表，批次ID: {}", auditId);
        List<AuditItem> items = queryAuditItems(auditId);
        return items.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
    }

    @Override
    public void updateItemStatusByAuditId(Long auditId, String newStatus) {
        log.info("批量更新审核项目状态，批次ID: {}, 新状态: {}", auditId, newStatus);
        LambdaUpdateWrapper<AuditItem> wrapper = new LambdaUpdateWrapper<AuditItem>()
                .eq(AuditItem::getAuditId, auditId)
                .set(AuditItem::getStatus, newStatus);
        update(wrapper);
    }

    // ==================== 辅助方法 ====================

    /**
     * 将AuditItem转换为AuditItemVO
     */
    private AuditItemVO convertToVO(AuditItem item) {
        return BeanUtil.copyProperties(item, AuditItemVO.class);
    }
}
