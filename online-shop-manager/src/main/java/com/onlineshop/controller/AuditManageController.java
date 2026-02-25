package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.audit.application.AbstractAuditor;
import com.onlineshop.framework.models.audit.application.AuditAppService;
import com.onlineshop.framework.models.audit.application.impl.GoodsAuditor;
import com.onlineshop.framework.models.audit.application.impl.StoreRegisterAuditor;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.audit.vo.AuditVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 审核管理控制器
 * 合并自 admin/AdminAuditController + merchant/MerchantAuditController
 *
 * @author : Tomatos
 * @date : 2026/1/11
 */
@RestController
@RequestMapping("/manage/audit")
@PreAuthorize("hasAuthority('audit:view')")
@RequiredArgsConstructor
public class AuditManageController {
    private final IAuditService auditService;
    private final AuditAppService auditAppService;

    /**
     * 分页查询审核记录
     * 管理员权限：查询所有审核记录
     *
     * @param auditQueryDTO 查询条件
     * @return 审核记录分页结果
     */
    @GetMapping("/page")
    public IPage<AuditVO> pageQuery(AuditParamsDTO auditQueryDTO) {
        return auditService.pageQuery(auditQueryDTO);
    }

    /**
     * 获取审核记录详情
     * 来自 admin/AdminAuditController
     *
     * @param auditId 审核记录ID
     * @return 审核记录详情
     */
    @GetMapping("/{auditId}")
    public AuditVO getAuditDetail(@PathVariable @NotNull Long auditId) {
        return auditService.getAuditById(auditId);
    }

    /**
     * 审核决策（通过或拒绝）
     * 管理员专用：对任何类型的审核进行决策
     * <p>
     * 使用统一的决策接口处理所有审核类型的决策，根据审核类型自动路由到对应的 Auditor
     *
     * @param decisionDTO 审核决策数据（包含auditId、approved、reason）
     */
    @PostMapping("/decision/{type}")
    @PreAuthorize("hasAuthority('audit:edit')")
    public void auditDecision(
            @Valid @RequestBody AuditDecisionDTO decisionDTO,
            @PathVariable String type
    ) {
        auditAppService.handleAuditDecision(decisionDTO, type);
    }
}