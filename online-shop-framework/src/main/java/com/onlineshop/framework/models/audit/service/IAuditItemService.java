package com.onlineshop.framework.models.audit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.audit.entity.AuditItem;
import com.onlineshop.framework.models.audit.vo.AuditItemVO;

import java.util.List;

/**
 * 审核项目服务接口
 */
public interface IAuditItemService extends IService<AuditItem> {

    /**
     * 根据批次ID查询所有项
     *
     * @param auditId 批次ID
     * @return 项列表
     */
    List<AuditItem> queryByAuditId(Long auditId);

    /**
     * 根据批次ID查询所有项的VO
     *
     * @param auditId 批次ID
     * @return 项VO列表
     */
    List<AuditItemVO> getAuditById(Long auditId);

    /**
     * 批量更新审核批次下所有项的状态
     *
     * @param auditId 审核批次ID
     * @param newStatus 新状态
     */
    void updateItemStatusByAuditId(Long auditId, String newStatus);
}
