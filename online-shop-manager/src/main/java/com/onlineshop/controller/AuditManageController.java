package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.audit.application.IAuditAppService;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
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
    private final IAuditAppService auditAppService;

    /**
     * 分页查询审核记录
     * 管理员权限：查询所有审核记录
     * 商家权限：查询自己店铺的审核记录
     * 来自 admin/AdminAuditController + merchant/MerchantAuditController
     *
     * @param auditQueryDTO 查询条件
     * @return 审核记录分页结果
     */
    @GetMapping("/page")
    public IPage<AuditVO> pageQuery(AuditParamsDTO auditQueryDTO) {
        return auditService.pageQuery(auditQueryDTO);
    }

    /**
     * 使用 POST 方式分页查询审核记录（兼容性）
     * 来自 admin/AdminAuditController
     */
    @PostMapping("/page")
    public IPage<AuditVO> pageQueryPost(@RequestBody AuditParamsDTO queryDTO) {
        return auditService.pageQuery(queryDTO);
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
     * 审核商品决定
     * 管理员专用：同意或拒绝商品审核
     *
     * @param decisionDTO 审核决定数据（包含auditLogId, approved, reason）
     */
    @PostMapping("/goods/decision")
    @PreAuthorize("hasAuthority('audit:edit')")
    public void auditGoodsDecision(@Valid @RequestBody AuditDecisionDTO decisionDTO) {
        auditAppService.handleAuditDecision(decisionDTO);
    }
}