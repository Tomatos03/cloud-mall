package com.onlineshop.framework.models.audit.application.impl;

import com.alibaba.fastjson2.JSON;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.audit.application.AbstractAuditor;
import com.onlineshop.framework.models.audit.domain.StoreRegisterAuditRequest;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.auth.enums.AccountType;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.models.system.user.entity.UserQualification;
import com.onlineshop.framework.models.system.user.mapper.UserQualificationMapper;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.IDNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 店铺注册审核处理器
 * 继承泛型模板基类，实现店铺注册审核的完整流程
 * <p>
 * 审核流程（新设计）：
 * 1. 提交审核：验证 → 创建店铺（待审核状态）→ 保存审核记录
 * 2. 审核通过：激活店铺 + 更新资质审核状态为已通过
 * 3. 审核拒绝：修改资质审核状态为已拒绝
 * <p>
 * 职责（已解耦）：
 * 1. 验证店铺注册审核请求的合法性
 * 2. 创建待审核的店铺对象
 * 3. 生成审核快照用于持久化
 * 4. 处理审核通过时的业务逻辑（激活店铺、更新资质审核状态）
 * 5. 处理审核拒绝时的业务逻辑（更新资质审核状态）
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Component
@RequiredArgsConstructor
public class StoreRegisterAuditor extends AbstractAuditor<StoreRegisterAuditRequest> {
    private final IStoreService storeService;
    private final UserQualificationMapper userQualificationMapper;
    private final IUserService userService;

    @Override
    protected boolean support(AuditType type) {
        return AuditType.STORE_REGISTER == type;
    }

    @Override
    protected void validateRequest(StoreRegisterAuditRequest request) {
        // 验证基础信息
        AssertUtils.assertNotBlank(request.getRealName(), BizErrorCode.INVALID_PARAM);
        AssertUtils.assertNotBlank(request.getIdCard(), BizErrorCode.INVALID_PARAM);

        // 验证银行账户信息
        AssertUtils.assertNotBlank(request.getBankAccountName(), BizErrorCode.INVALID_PARAM);
        AssertUtils.assertNotBlank(request.getBankCardNumber(), BizErrorCode.INVALID_PARAM);
        AssertUtils.assertNotBlank(request.getBankName(), BizErrorCode.INVALID_PARAM);

        // 设置申请人名称
        request.setApplicantName(request.getRealName());
    }

    @Override
    protected Long onApproved(StoreRegisterAuditRequest request) {
        String finalStoreName = request.getStoreName() != null && !request.getStoreName()
                                                                          .trim()
                                                                          .isEmpty()
                ? request.getStoreName()
                : request.getApplicantName() + "的店铺";

        Store store = Store.builder()
                           .no(IDNumber.generateStoreNo())
                           .userId(request.getApplicantId())
                           .name(finalStoreName)
                           .build();

        storeService.save(store);
        saveUserQualification(request);
        addMerchantAccountType(request.getApplicantId());
        return store.getId();
    }

    private void addMerchantAccountType(Long userId) {
        User user = userService.getById(userId);
        String currentTypes = user.getTypes();
        
        Set<String> typeSet = new HashSet<>();
        if (currentTypes != null && !currentTypes.isEmpty()) {
            typeSet.addAll(Arrays.asList(currentTypes.split(",")));
        }
        typeSet.add(AccountType.MERCHANT.getCode());
        
        user.setTypes(String.join(",", typeSet));
        userService.updateById(user);
    }

    @Override
    protected String generateSnapshot(StoreRegisterAuditRequest request) {
        return JSON.toJSONString(request);
    }

    @Override
    protected StoreRegisterAuditRequest rebuildRequest(String snapshot) {
        return JSON.parseObject(snapshot, StoreRegisterAuditRequest.class);
    }

    /**
     * 保存用户资质认证信息
     *
     * @param request 店铺注册审核请求
     */
    private void saveUserQualification(StoreRegisterAuditRequest request) {
        UserQualification qualification = UserQualification.builder()
                                                           .userId(request.getApplicantId())
                                                           .subjectType(request.getSubjectType())
                                                           .realName(request.getRealName())
                                                           .idCard(request.getIdCard())
                                                           .idCardValidStart(request.getIdCardValidStart())
                                                           .idCardValidEnd(request.getIdCardValidEnd())
                                                           .idCardFront(request.getIdCardFront())
                                                           .idCardBack(request.getIdCardBack())
                                                           .licenseNumber(request.getLicenseNumber())
                                                           .licenseName(request.getLicenseName())
                                                           .establishmentDate(request.getEstablishmentDate())
                                                           .registeredAddress(request.getRegisteredAddress())
                                                           .licensePhoto(request.getLicensePhoto())
                                                           .accountName(request.getBankAccountName())
                                                           .cardNumber(request.getBankCardNumber())
                                                           .bankName(request.getBankName())
                                                           .branchName(request.getBankBranchName())
                                                           .mobile(request.getBankMobile())
                                                           .auditStatus(AuditStatus.APPROVED.getCode())
                                                           .createdAt(LocalDateTime.now())
                                                           .updatedAt(LocalDateTime.now())
                                                           .build();

        userQualificationMapper.insert(qualification);
    }
}
