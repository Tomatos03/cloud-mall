package com.onlineshop.framework.models.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditQueryDTO;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.vo.AuditVO;

/**
 * 审核日志服务接口
 */
public interface IAuditService extends IService<Audit> {

    /**
     * 提交审核申请
     * 创建一个新的审核记录，状态为待审核(1)
     *
     * @param submitDTO 审核提交数据
     * @throws com.onlineshop.framework.exception.BusinessException 当参数验证失败时
     */
    void submitAudit(AuditSubmitDTO submitDTO);

    /**
     * 审核员审核（通过或拒绝）
     * 更新审核记录的状态和审核人信息
     *
     * @param decisionDTO 审核决定数据
     * @throws com.onlineshop.framework.exception.BusinessException 当审核记录不存在或状态不符合要求时
     */
    void auditDecision(AuditDecisionDTO decisionDTO);

    /**
     * 分页查询审核记录（支持多条件筛选）
     * 通用分页查询方法，支持管理员和商家使用
     *
     * @param queryDTO 查询条件
     * @return 审核记录分页结果
     */
    IPage<AuditVO> pageQuery(AuditQueryDTO queryDTO);

    /**
     * 商家分页查询自己店铺的审核记录（自动限制为当前用户的店铺）
     * 需要查询的商品必须属于当前商家的店铺
     *
     * @param queryDTO 查询条件，注意storeId会被忽略，使用当前登录用户的店铺ID
     * @return 审核记录分页结果
     * @throws com.onlineshop.framework.exception.BusinessException 当用户未关联店铺时
     */
    IPage<AuditVO> pageQueryMerchant(AuditQueryDTO queryDTO);

    /**
     * 根据ID获取审核记录详情
     *
     * @param auditId 审核记录ID
     * @return 审核记录VO
     * @throws com.onlineshop.framework.exception.BusinessException 当审核记录不存在时
     */
    AuditVO getAuditById(Long auditId);

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