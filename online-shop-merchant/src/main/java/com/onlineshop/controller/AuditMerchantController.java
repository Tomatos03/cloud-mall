package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.audit.application.IAuditAppService;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.audit.vo.AuditItemVO;
import com.onlineshop.framework.models.audit.vo.AuditListItemVO;
import com.onlineshop.framework.utils.AuthUserUtils;
import jakarta.validation.constraints.NotBlank;
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
        log.debug("商家查询审核列表，商家ID: {}, 页码: {}, 每页数量: {}", 
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
        log.debug("商家查询审核详情，审核批次ID: {}", auditId);
        return auditService.getAuditById(auditId);
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
        log.debug("商家按审核编号查询审核详情，审核批次编号: {}", auditNo);
        return auditService.getAuditByNo(auditNo);
    }

    /**
     * 撤销审核申请
     * DELETE /merchant/audit/{auditNo}
     * 商家可以撤销自己提交的待审核申请
     *
     * @param auditNo 审核批次编号
     */
    @DeleteMapping("/{auditNo}")
    public void withdraw(@PathVariable @NotBlank String auditNo) {
        log.debug("商家撤销审核申请，审核批次编号: {}", auditNo);
        auditAppService.withdrawAudit(auditNo);
        log.debug("审核申请撤销成功，审核批次编号: {}", auditNo);
    }
}
