package com.onlineshop.framework.models.audit.application;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.audit.domain.AuditRequest;
import com.onlineshop.framework.models.audit.domain.SeckillGoodsAuditRequest;
import com.onlineshop.framework.models.audit.domain.SeckillGoodsItem;
import com.onlineshop.framework.models.audit.dto.AuditDecisionDTO;
import com.onlineshop.framework.models.audit.dto.AuditParamsDTO;
import com.onlineshop.framework.models.audit.dto.AuditStatusDTO;
import com.onlineshop.framework.models.audit.dto.SeckillActivityGoodsParamsDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.audit.vo.AuditVO;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 审核应用服务实现
 * 提供审核操作的统一API入口
 * 
 * 核心职责：
 * 1. submitAudit - 统一的审核提交API，支持所有业务类型
 * 2. handleAuditDecision - 统一的审核决策API，支持所有业务类型
 * 3. queryUserCreateStoreAuditStatus - 店铺注册状态查询
 * 
 * 设计要点：
 * - 使用工厂模式（AuditorFactory）根据businessType动态获取对应的Auditor
 * - 利用模板方法模式（AbstractAuditor）保证审核流程的一致性
 * - 所有审核类型共享同一套API，降低复杂度
 *
 * @author Tomatos
 * @date 2026/1/12
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AuditAppService implements IAuditAppService {
    private final IAuditService auditService;
    private final AuditorFactory auditorFactory;

    @Override
    public <T extends AuditRequest> void submitAudit(T request) {
        AbstractAuditor<T> auditor = (AbstractAuditor<T>) auditorFactory.getAuditor(request.getType());
        auditor.submitAudit(request);
    }

    /**
     * 统一的审核决策处理API
     * 处理审核的批准或拒绝，支持所有业务类型
     * 
     * 流程：
     * 1. 根据auditId查询审核记录
     * 2. 从审核记录中获取targetType（即businessType）
     * 3. 通过AuditorFactory获取对应的Auditor实例
     * 4. 调用Auditor的handleDecision()方法，进行批准或拒绝处理
     * 
     * @param decision 审核决策DTO，包含auditId、approved标志和拒绝原因
     * @throws BizException 当审核记录不存在或业务类型未知时抛出
     */
    @Override
    public void handleAuditDecision(AuditDecisionDTO decision, String type) {
        AbstractAuditor<?> auditor = auditorFactory.getAuditor(type);
        auditor.handleDecision(decision);
    }

    /**
     * 查询当前用户的创建店铺审核状态
     */
    @Override
    public AuditStatusDTO queryUserCreateStoreAuditStatus() {
        Audit audit = auditService.lambdaQuery()
                                  .eq(Audit::getTargetType, AuditType.STORE_REGISTER.getCode())
                                  .eq(Audit::getApplicantId, AuthUserUtils.getUserId())
                                  .one();
        if (Objects.isNull(audit)) {
            return new AuditStatusDTO();
        }

        return AuditStatusDTO.builder()
                             .status(audit.getStatus())
                             .build();
    }

    /**
     * 获取秒杀活动中的商品（审核通过和待审核）
     * 
     * 分页查询指定秒杀活动中审核通过或待审核的商品列表。
     * 从审核记录的快照中提取商品信息，支持分页返回。
     * 
     * 实现逻辑：
     * 1. 验证参数有效性（activityId 不能为空）
     * 2. 查询该活动的所有 APPROVED 和 PENDING 状态的审核记录
     * 3. 从快照中反序列化商品列表
     * 4. 合并所有商品并分页返回
     * 
     * @param params 查询参数DTO，包含 activityId、page、pageSize
     * @return 商品分页数据
     */
    @Override
    public IPage<SeckillGoodsItem> getSeckillActivityGoods(SeckillActivityGoodsParamsDTO params) {
        AssertUtils.notNull(params, BizErrorCode.INVALID_PARAM);
        AssertUtils.notNull(params.getActivityId(), BizErrorCode.ACTIVITY_ID_REQUIRED);
        
        Long activityId = params.getActivityId();
        Integer page = params.getPage();
        Integer pageSize = params.getPageSize();
        
        log.info("查询秒杀活动商品，活动ID: {}, 页码: {}, 每页数量: {}", activityId, page, pageSize);
        
        // ==================== 查询审核通过和待审核的商品 ====================
        List<SeckillGoodsItem> allGoods = new ArrayList<>();
        
        // 查询APPROVED状态
        AuditParamsDTO approvedParams = new AuditParamsDTO();
        approvedParams.setTargetType(AuditType.SECKILL_ACTIVITY.getCode());
        approvedParams.setStatus(AuditStatus.APPROVED.getCode());
        approvedParams.setPage(1);
        approvedParams.setPageSize(1000);  // 一次性取所有
        
        IPage<?> approvedAudits = auditService.pageQuery(approvedParams);
        extractGoodsFromAudits(approvedAudits.getRecords(), activityId, allGoods);
        
        // 查询PENDING状态
        AuditParamsDTO pendingParams = new AuditParamsDTO();
        pendingParams.setTargetType(AuditType.SECKILL_ACTIVITY.getCode());
        pendingParams.setStatus(AuditStatus.PENDING.getCode());
        pendingParams.setPage(1);
        pendingParams.setPageSize(1000);  // 一次性取所有
        
        IPage<?> pendingAudits = auditService.pageQuery(pendingParams);
        extractGoodsFromAudits(pendingAudits.getRecords(), activityId, allGoods);
        
        // ==================== 分页处理 ====================
        long total = allGoods.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allGoods.size());
        
        List<SeckillGoodsItem> pageGoods = new ArrayList<>();
        if (startIndex < allGoods.size()) {
            pageGoods = allGoods.subList(startIndex, endIndex);
        }
        
        // ==================== 构建分页结果 ====================
        Page<SeckillGoodsItem> result = new Page<>(page, pageSize);
        result.setRecords(pageGoods);
        result.setTotal(total);
        
        log.info("查询完成，共 {} 个商品，当前页返回 {} 个", total, pageGoods.size());
        return result;
    }

    /**
     * 从审核记录列表中提取指定活动的商品
     * 
     * @param audits 审核记录对象列表
     * @param activityId 秒杀活动ID
     * @param resultGoods 结果商品列表（会被追加）
     */
    private void extractGoodsFromAudits(List<?> audits, Long activityId, List<SeckillGoodsItem> resultGoods) {
        if (audits == null || audits.isEmpty()) {
            return;
        }
        
        for (Object auditObj : audits) {
            try {
                // 将对象转为JSON后再解析为AuditVO，获取快照
                String json = JSON.toJSONString(auditObj);
                AuditVO auditVO = JSON.parseObject(json, AuditVO.class);
                if (auditVO != null && auditVO.getSnapshot() != null) {
                    // 从快照反序列化出秒杀商品审核请求
                    SeckillGoodsAuditRequest request = JSON.parseObject(
                        auditVO.getSnapshot(), 
                        SeckillGoodsAuditRequest.class
                    );
                    
                    // 只提取指定活动的商品
                    if (request != null && request.getActivityId().equals(activityId) && request.getItems() != null) {
                        resultGoods.addAll(request.getItems());
                    }
                }
            } catch (Exception e) {
                log.warn("提取商品信息失败，跳过此审核记录", e);
            }
        }
    }
}