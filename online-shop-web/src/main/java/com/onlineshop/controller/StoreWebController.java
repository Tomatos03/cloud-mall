package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.audit.application.IAuditAppService;
import com.onlineshop.framework.models.audit.dto.AuditStatusDTO;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.goods.spu.vo.GoodsCardVO;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.dto.StoreGoodsParamsDTO;
import com.onlineshop.framework.models.store.dto.StoreRegistrationForm;
import com.onlineshop.framework.models.store.vo.StoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺相关接口
 */
@RestController
@RequestMapping("/web/store")
@RequiredArgsConstructor
public class StoreWebController {
    private final IAuditAppService auditAppService;
    private final IStoreService storeService;

    /**
     * 获取店铺基本信息
     * GET /store/info/{storeId}
     */
    @GetMapping("/info/{storeId}")
    public StoreVO getStoreInfo(@PathVariable Long storeId) {
        return storeService.getStoreInfoById(storeId);
    }

    /**
     * 分页获取店铺商品列表
     */
    @GetMapping("/goods")
    public IPage<GoodsCardVO> pageStoreGoods(StoreGoodsParamsDTO queryDTO) {
        return storeService.pageStoreGoods(queryDTO);
    }

    /**
     * 查询当前用户的入驻申请状态
     *
     */
    @GetMapping("/application/status")
    public AuditStatusDTO queryApplicationStatus() {
        return auditAppService.queryUserCreateStoreAuditStatus();
    }

    /**
     * 提交入驻申请
     *
     * @param submitDTO 申请表单数据，包含所有必要的商家信息
     */
    @PostMapping("/create")
    public void submitApplication(@RequestBody StoreRegistrationForm submitDTO) {
        auditAppService.submitAudit(AuditType.STORE_REGISTER, submitDTO);
    }
}