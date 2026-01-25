package com.onlineshop.controller.merchant;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.audit.dto.AuditQueryDTO;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.audit.vo.AuditVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/11
 */
@RestController
@RequestMapping("/manager/merchant/audit")
public class MerchantAuditController {
    @Autowired
    private IAuditService auditService;

    /**
     * 商家分页查询自己店铺的审核记录
     * 自动过滤为当前商家店铺下的商品审核
     *
     * @param auditQueryDTO 查询条件
     * @return 审核记录分页结果
     */
    @GetMapping("/page")
    public IPage<AuditVO> getAuditPage(AuditQueryDTO auditQueryDTO) {
        return auditService.pageQueryMerchant(auditQueryDTO);
    }

    /**
     * 白名单请求：获取指定ID的提现审核详情
     * @param id 提现审核ID
     * @return 审核详情
     */
    @PostMapping("/withdraw/{id}")
    public boolean withdrawAudit(@PathVariable Long id) {
        return auditService.withdrawAudit(id);
    }
}