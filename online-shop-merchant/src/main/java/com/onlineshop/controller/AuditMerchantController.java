package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.audit.application.IAuditAppService;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.audit.vo.AuditItemVO;
import com.onlineshop.framework.models.audit.vo.AuditListItemVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@Slf4j
public class AuditMerchantController {
    private final IAuditService auditService;
    private final IAuditAppService auditAppService;

    /**
     * 查询审核列表
     * GET /merchant/audit/page
     * 商家只能查询自己提交的审核记录
     *
     * @param queryDTO 查询条件
     * @return 审核记录分页结果
     */
    @GetMapping("/page")
    public IPage<AuditListItemVO> pageQuery(AuditParamsDTO queryDTO) {
        Long merchantId = AuthUserUtils.getUserId();
        log.info("商家查询审核列表，商家ID: {}, 页码: {}, 每页数量: {}", 
                 merchantId, queryDTO.getPage(), queryDTO.getPageSize());
        queryDTO.setApplicantId(merchantId);
        return auditService.pageQuery(queryDTO);
    }

    /**
     * 获取审核详情
     * GET /merchant/audit/{auditId}
     * 商家查询自己提交的审核批次的所有项目
     *
     * @param auditId 审核记录ID
     * @return 审核项目列表
     */
    @GetMapping("/{auditId}")
    public List<AuditItemVO> getDetail(@PathVariable @NotNull Long auditId) {
        log.info("商家查询审核详情，审核批次ID: {}", auditId);
        return auditService.getAuditById(auditId);
    }

    /**
     * 撤销审核申请
     * DELETE /merchant/audit/{auditId}
     * 商家可以撤销自己提交的待审核申请
     *
     * @param auditId 审核记录ID
     */
    @DeleteMapping("/{auditId}")
    public void withdraw(@PathVariable @NotNull Long auditId) {
        log.info("商家撤销审核申请，审核批次ID: {}", auditId);
        auditAppService.withdrawAudit(auditId);
        log.info("审核申请撤销成功，审核批次ID: {}", auditId);
    }
}