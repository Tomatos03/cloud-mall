package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.framework.application.audit.IAuditAppService;
import com.cloudmall.framework.models.audit.dto.AuditParamsDTO;
import com.cloudmall.framework.models.audit.dto.AuditDecisionDTO;
import com.cloudmall.framework.models.audit.service.IAuditService;
import com.cloudmall.framework.models.audit.vo.AuditItemVO;
import com.cloudmall.framework.models.audit.vo.AuditListItemVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审核管理控制器
 * 合并自 admin/AdminAuditController + merchant/MerchantAuditController
 *
 * @author : Tomatos
 * @date : 2026/1/11
 */
@RestController
@RequestMapping("/manager/audit")
@PreAuthorize("hasAuthority('audit:view')")
@RequiredArgsConstructor
@Slf4j
public class AuditManageController {
    private final IAuditService auditService;
    private final IAuditAppService auditAppService;

    /**
     * 分页查询审核记录
     * 管理员权限：查询所有审核记录
     *
     * @param auditQueryDTO 查询条件
     * @return 审核记录分页结果
     */
    @GetMapping("/page")
    public IPage<AuditListItemVO> pageQuery(AuditParamsDTO auditQueryDTO) {
        log.info("分页查询审核记录，页码: {}, 每页数量: {}", auditQueryDTO.getPage(), auditQueryDTO.getPageSize());
        return auditService.pageQuery(auditQueryDTO);
    }

    /**
     * 获取审核详情
     * GET /merchant/audit/{auditNo}/detail
     * 按审核批次编号查询审核项目列表
     *
     * @param auditNo 审核批次编号
     * @return 审核项目列表（包含reason、snapshot、status等字段）
     */
    @GetMapping("/{auditNo}/detail")
    public List<AuditItemVO> getDetailByAuditNo(@PathVariable @NotBlank String auditNo) {
        log.debug("管理员按审核编号查询审核详情，审核批次编号: {}", auditNo);
        return auditService.getAuditByNo(auditNo);
    }

    /**
     * 获取审核记录详情
     * 来自 admin/AdminAuditController
     *
     * @param auditId 审核记录ID
     * @return 审核记录详情
     */
    @GetMapping("/{auditId}")
    public List<AuditItemVO> getAuditDetail(@PathVariable @NotNull Long auditId) {
        log.info("查询审核详情，审核批次ID: {}", auditId);
        return auditService.getAuditById(auditId);
    }

    /**
     * 审核决策（通过或拒绝）
     * 管理员专用：对任何类型的审核进行决策
     * <p>
     * 使用统一的决策接口处理所有审核类型的决策，根据审核类型自动路由到对应的 Auditor
     *
     * @param decisionDTO 审核决策数据（包含auditId、decisions列表）
     * @param type 审核业务类型
     */
    @PostMapping("/decision/{type}")
    @PreAuthorize("hasAuthority('audit:edit')")
    public void auditDecision(
            @Valid @RequestBody AuditDecisionDTO decisionDTO,
            @PathVariable String type
    ) {
        log.info("提交审核决策，审核批次ID: {}, 业务类型: {}, 决策数量: {}",
                 decisionDTO.getAuditNo(), type, decisionDTO.getDecisions().size());
        auditAppService.submitAuditDecisions(decisionDTO, type);
        log.info("审核决策提交完成，审核批次ID: {}", decisionDTO.getAuditNo());
    }
}