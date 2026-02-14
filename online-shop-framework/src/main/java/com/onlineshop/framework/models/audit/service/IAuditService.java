package com.onlineshop.framework.models.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.vo.AuditVO;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 审核日志服务接口
 */
public interface IAuditService extends IService<Audit> {

    /**
     * 提交审核申请
     * 创建一个新的审核记录，状态为待审核(1)
     *
     * @param submitDTO 审核提交数据
     * @throws BizException 当参数验证失败时
     */
    void submitAudit(AuditSubmitDTO submitDTO);

    /**
     * 分页查询审核记录（支持多条件筛选）
     * 通用分页查询方法，支持管理员和商家使用
     *
     * @param queryDTO 查询条件
     * @return 审核记录分页结果
     */
    IPage<AuditVO> pageQuery(AuditParamsDTO queryDTO);

    /**
     * 根据ID获取审核记录详情
     *
     * @param auditId 审核记录ID
     * @return 审核记录VO
     * @throws BizException 当审核记录不存在时
     */
    AuditVO getAuditById(Long auditId);

    /**
     * 撤回审核申请（仅限待审核状态）
     * 将审核记录状态改为未提交(0)
     *
     * @param auditId 审核记录ID
     * @return 是否成功
     * @throws BizException 当审核记录不存在或状态不符合要求时
     */
    boolean withdrawAudit(Long auditId);

    /**
     * 获取指定目标的最新审核记录
     * 通过目标类型和目标ID查询该目标的最近一条审核记录（按审核记录创建时间降序）
     *
     * @param type 目标类型（如 GOODS、STORE 等）
     * @param targetId 目标ID
     * @return 最新的审核记录，如果不存在则返回null
     */
    Audit queryLatestAudit(AuditType type, Long targetId);

    /**
     * 批量获取指定目标的最新审核记录
     * 通过目标类型和多个目标ID，查询每个目标的最近一条审核记录
     * 返回的Map中：key为targetId，value为对应的最新审核记录，如果某个目标没有审核记录则不包含在结果中
     *
     * @param type 目标类型（如 GOODS、STORE 等）
     * @param targetIds 目标ID集合
     * @return 目标ID与最新审核记录的映射，不包含没有审核记录的目标
     */
    List<Audit> queryLatestAuditByTypeBatch(AuditType type, Collection<? extends Serializable> targetIds);
    /**
     * 更新审核记录的状态、目标ID和额外信息
     * 用于重新提交审核时，更新审核状态为待审核，同时更新序列化后的数据和目标ID
     *
     * @param auditId 审核记录ID
     * @param status 新的审核状态
     * @param targetId 被审核对象的ID
     * @param payload 被审核对象的数据（会被序列化为JSON存储到extraInfo）
     * @throws BizException 当审核记录不存在时
     */
    void updateAudit(Long auditId, AuditStatus status, Long targetId, Object payload);

    void updateAudit(Long auditId, AuditStatus status, Long targetId, String reason);

    void updateAudit(Long targetId, AuditStatus status, Object payload);
}
