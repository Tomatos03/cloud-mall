package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.audit.vo.AuditVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商家审核管理 Controller
 * 商家查询自己提交的审核记录
 *
 * @author : Tomatos
 * @date : 2026/3/3
 */
@RequestMapping("/audit")
@RestController
@RequiredArgsConstructor
public class AuditMerchantController {
    private final IAuditService auditService;

    /**
     * 查询审核列表
     * POST /merchant/audit/page
     *
     * @param queryDTO 查询条件
     * @return 审核记录分页结果
     */
    @PostMapping("/page")
    public IPage<AuditVO> pageQuery(@Valid @RequestBody AuditParamsDTO queryDTO) {
        Long merchantId = AuthUserUtils.getUserId();
        queryDTO.setApplicantId(merchantId);
        return auditService.pageQuery(queryDTO);
    }

    /**
     * 获取审核详情
     * GET /merchant/audit/{auditId}
     *
     * @param auditId 审核记录ID
     * @return 审核记录详情
     */
    @GetMapping("/{auditId}")
    public AuditVO getDetail(@PathVariable Long auditId) {
        return auditService.getAuditById(auditId);
    }

    /**
     * 撤销审核申请
     * DELETE /merchant/audit/{auditId}
     *
     * @param auditId 审核记录ID
     * @return 是否撤销成功
     */
    @DeleteMapping("/{auditId}")
    public boolean withdraw(@PathVariable Long auditId) {
        return auditService.withdrawAudit(auditId);
    }
}