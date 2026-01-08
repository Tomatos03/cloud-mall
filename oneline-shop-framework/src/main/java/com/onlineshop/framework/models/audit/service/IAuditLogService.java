package com.onlineshop.framework.models.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditQueryDTO;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.entity.AuditLog;
import com.onlineshop.framework.models.audit.vo.AuditLogVO;

/**
 * 审核日志服务接口
 */
public interface IAuditLogService extends IService<AuditLog> {

    /**
     * 提交审核申请
     * 创建一个新的审核记录，状态为待审核(1)
     *
     * @param submitDTO 审核提交数据
     * @return 创建的审核记录ID
     * @throws com.onlineshop.framework.exception.BusinessException 当参数验证失败时
     */
    Long submitAudit(AuditSubmitDTO submitDTO);

    /**
     * 审核员审核（通过或拒绝）
     * 更新审核记录的状态和审核人信息
     *
     * @param decisionDTO 审核决定数据
     * @return 是否成功
     * @throws com.onlineshop.framework.exception.BusinessException 当审核记录不存在或状态不符合要求时
     */
    boolean auditDecision(AuditDecisionDTO decisionDTO);

    /**
     * 分页查询审核记录（支持多条件筛选）
     *
     * @param queryDTO 查询条件
     * @return 审核记录分页结果
     */
    IPage<AuditLogVO> pageQuery(AuditQueryDTO queryDTO);

    /**
     * 根据ID获取审核记录详情
     *
     * @param auditId 审核记录ID
     * @return 审核记录VO
     * @throws com.onlineshop.framework.exception.BusinessException 当审核记录不存在时
     */
    AuditLogVO getAuditById(Long auditId);

    /**
     * 根据被审核对象获取最新的审核记录
     * 用于查询商品或SKU的审核状态
     *
     * @param targetType 被审核对象类型
     * @param targetId 被审核对象ID
     * @return 最新的审核记录VO，如果不存在返回null
     */
    AuditLogVO getLatestAuditByTarget(String targetType, Long targetId);

    /**
     * 获取待审核的审核记录数量
     *
     * @return 待审核数量
     */
    long getPendingAuditCount();

    /**
     * 撤回审核申请（仅限待审核状态）
     * 将审核记录状态改为未提交(0)
     *
     * @param auditId 审核记录ID
     * @return 是否成功
     * @throws com.onlineshop.framework.exception.BusinessException 当审核记录不存在或状态不符合要求时
     */
    boolean withdrawAudit(Long auditId);
}