package com.onlineshop.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditQueryDTO;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.audit.vo.AuditVO;
import com.onlineshop.framework.models.goods.application.IGoodsAppService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员审核管理 Controller
 * 处理商品审核、审核决定等相关操作
 */
@RestController
@RequestMapping("/manager/admin/audit")
public class AdminAuditController {
    
    @Autowired
    private IAuditService auditLogService;
    
    @Autowired
    private IGoodsAppService goodsAppService;

    /**
     * 分页查询审核记录
     *
     * @param queryDTO 查询条件
     * @return 审核记录分页结果
     */
    @PostMapping("/page")
    public IPage<AuditVO> pageQuery(@RequestBody AuditQueryDTO queryDTO) {
        return auditLogService.pageQuery(queryDTO);
    }

    /**
     * 获取审核记录详情
     *
     * @param auditId 审核记录ID
     * @return 审核记录详情
     */
    @GetMapping("/{auditId}")
    public AuditVO getAuditDetail(@PathVariable @NotNull Long auditId) {
        return auditLogService.getAuditById(auditId);
    }

    /**
     * 审核商品
     * 管理员同意或拒绝商品审核，如果同意则进行实际的商品保存或更新操作
     *
     * @param decisionDTO 审核决定数据（包含auditLogId, approved, reason）
     */
    @PostMapping("/goods/decision")
    public void auditGoodsDecision(@Valid @RequestBody AuditDecisionDTO decisionDTO) {
        // 先更新审核记录状态
        auditLogService.auditDecision(decisionDTO);
        
        // 如果审核通过，则进行实际的商品保存操作
        if (decisionDTO.getApproved()) {
            goodsAppService.updateGoodsAfterAudit(decisionDTO.getAuditId());
        }
    }
}