package com.onlineshop.framework.models.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditBizType;
import com.onlineshop.framework.models.audit.vo.AuditItemVO;
import com.onlineshop.framework.models.audit.vo.AuditListItemVO;

import java.util.List;

/**
 * 审核批次服务接口
 */
public interface IAuditService extends IService<Audit> {

    /**
     * 分页查询审核批次（支持多条件筛选）
     * 通用分页查询方法，支持管理员查看所有审核批次
     *
     * @param queryDTO 查询条件
     * @return 审核批次分页结果
     */
    IPage<AuditListItemVO> pageQuery(AuditParamsDTO queryDTO);

    /**
     * 根据ID获取审核批次详情
     *
     * @param auditId 审核批次ID
     * @return 审核批次VO
     * @throws BizException 当审核批次不存在时
     */
    List<AuditItemVO> getAuditById(Long auditId);

    /**
     * 根据批次编号获取审核批次详情
     *
     * @param auditNo 审核批次编号
     * @return 审核项目列表
     * @throws BizException 当审核批次不存在时
     */
    List<AuditItemVO> getAuditByNo(String auditNo);

    /**
     * 创建审核批次
     *
     * @param bizType 业务类型
     * @param bizPid 业务父ID（如秒杀活动ID）
     * @param itemCount 项数量
     * @return 创建的Audit对象
     */
    Audit createAuditBatch(String bizType, Long bizPid, int itemCount);

    /**
     * 重新推算批次状态
     * 在所有audit_item都审批完成后调用，根据统计结果自动推算批次状态
     *
     * 状态流转规则：
     * - 所有项都通过 → APPROVED
     * - 所有项都拒绝 → REJECTED
     * - 部分通过部分拒绝 → PARTIAL
     * - 如果还有未审批的项 → 保持 PENDING
     *
     * @param auditId 审核批次ID
     */
    void recalculateAuditStatus(Long auditId);

    /**
     * 获取指定目标的最新审核批次
     * 通过目标类型和目标ID查询该目标的最近一条审核批次（按创建时间降序）
     *
     * @param type 目标类型（如 GOODS、STORE 等）
     * @param targetId 目标ID
     * @return 最新的审核批次，如果不存在则返回null
     */
    Audit queryLatestAudit(AuditBizType type, Long targetId);
}
