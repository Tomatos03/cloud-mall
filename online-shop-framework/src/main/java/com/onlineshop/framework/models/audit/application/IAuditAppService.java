package com.onlineshop.framework.models.audit.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.audit.domain.AuditRequest;
import com.onlineshop.framework.models.audit.domain.SeckillGoodsItem;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditRequestDTO;
import com.onlineshop.framework.models.audit.dto.AuditStatusDTO;
import com.onlineshop.framework.models.audit.dto.SeckillActivityGoodsParamsDTO;

/**
 * 审核应用服务接口
 * 定义审核业务操作的统一API
 * 
 * 包含两大类操作：
 * 1. 审核提交操作 - submitAudit
 * 2. 审核决策操作 - handleAuditDecision
 * 3. 审核查询操作 - queryUserCreateStoreAuditStatus
 *
 * @author Tomatos
 * @date 2026/1/12
 */
public interface IAuditAppService {
    
    /**
     * 统一的审核提交API
     * 支持所有业务类型的审核申请
     * 
     * 使用示例：
     * AuditRequestDTO<GoodsAuditRequest> dto = new AuditRequestDTO<>();
     * dto.setBusinessType("GOODS");
     * dto.setData(goodsAuditRequest);
     * auditAppService.submitAudit(dto);
     * 
     * @param requestDTO 审核请求DTO，包含businessType和具体的审核数据
     * @param <T> 审核请求类型，必须继承AuditRequest
     * @throws com.onlineshop.framework.exception.BizException 当业务异常时抛出
     */
    <T extends AuditRequest> void submitAudit(T request);

    /**
     * 统一的审核决策处理API
     * 处理审核的批准或拒绝，支持所有业务类型
     * 
     * 使用示例：
     * AuditDecisionDTO decision = AuditDecisionDTO.builder()
     *     .auditId(123L)
     *     .approved(true)
     *     .reason("审核通过")
     *     .build();
     * auditAppService.handleAuditDecision(decision);
     * 
     * @param decision 审核决策DTO，包含auditId、approved标志和拒绝原因
     * @throws com.onlineshop.framework.exception.BizException 当审核记录不存在或业务异常时抛出
     */
    void handleAuditDecision(AuditDecisionDTO decision, String type);
    
    /**
     * 查询当前用户的创建店铺审核状态
     */
    AuditStatusDTO queryUserCreateStoreAuditStatus();

    /**
     * 获取秒杀活动中的商品（审核通过和待审核）
     *
     * 分页查询指定秒杀活动中审核通过或待审核的商品列表。
     * 从审核记录的快照中提取商品信息并进行合并。
     *
     * @param params 查询参数DTO，包含activityId、page、pageSize
     * @return 商品分页数据
     */
    IPage<SeckillGoodsItem> getSeckillActivityGoods(SeckillActivityGoodsParamsDTO params);
}