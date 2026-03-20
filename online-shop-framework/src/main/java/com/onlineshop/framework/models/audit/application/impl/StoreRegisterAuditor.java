package com.onlineshop.framework.models.audit.application.impl;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.models.audit.application.AbstractAuditor;
import com.onlineshop.framework.models.audit.dto.AuditSubmitDTO;
import com.onlineshop.framework.models.audit.dto.StoreRegisterAuditItemDTO;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.entity.AuditItem;
import com.onlineshop.framework.models.audit.enums.AuditBizType;
import com.onlineshop.framework.models.audit.enums.AuditItemStatus;
import com.onlineshop.framework.models.auth.enums.AccountType;
import com.onlineshop.framework.models.store.IStoreService;
import com.onlineshop.framework.models.store.Store;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.models.system.user.entity.UserQualification;
import com.onlineshop.framework.models.system.user.mapper.UserQualificationMapper;
import com.onlineshop.framework.utils.AssertUtils;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.IDNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 店铺注册审核处理器
 * 继承泛型模板基类，实现店铺注册审核的完整流程
 * <p>
 * 审核流程：
 * 1. 提交审核：验证 → 创建待审核记录
 * 2. 审核通过：创建店铺 + 更新资质审核状态
 * 3. 审核拒绝：记录拒绝原因
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreRegisterAuditor extends AbstractAuditor<StoreRegisterAuditItemDTO> {
    private final IStoreService storeService;
    private final UserQualificationMapper userQualificationMapper;
    private final IUserService userService;

    @Override
    protected boolean support(AuditBizType auditBizType) {
        return AuditBizType.STORE_REGISTER == auditBizType;
    }

    @Override
    protected void validateAndFill(AuditSubmitDTO<StoreRegisterAuditItemDTO> submitDTO) {
        AssertUtils.isFalse(AuthUserUtils.isMerchantAccount(), BizErrorCode.NO_PERMISSION);
        Long userId = AuthUserUtils.getUserId();
        User user = userService.getById(userId);
        AssertUtils.notNull(user, BizErrorCode.USER_NOT_EXISTS);
        Collection<StoreRegisterAuditItemDTO> items = submitDTO.getItems();

        for (StoreRegisterAuditItemDTO item : items) {
            item.setUserId(userId);
        }
    }

    /**
     * 批量处理店铺注册审核决策
     * <p>
     * 逻辑：
     * 1. 遍历所有审核项
     * 2. 对于通过的项：创建店铺、保存资质、添加商家账户类型
     * 3. 对于拒绝的项：仅记录拒绝原因
     *
     * @param audit 审核批次
     * @param items   批次中的所有项（已按审核决策更新状态）
     */
    @Override
    protected void onProcessed(Audit audit, List<AuditItem> items) {
        Long auditId = audit.getId();
        log.info("处理店铺注册审核结果，批次ID: {}，项数: {}", auditId, items.size());

        for (AuditItem item : items) {
            if (AuditItemStatus.APPROVED.getCode().equals(item.getStatus())) {
                // 通过：创建店铺
                try {
                    StoreRegisterAuditItemDTO storeItem = parseSnapshot(item.getSnapshot(), StoreRegisterAuditItemDTO.class);
                    Long userId = resolveApplicantUserId(audit, storeItem);

                    // 创建店铺
                    String finalStoreName = storeItem.getStoreName() != null && !storeItem.getStoreName().trim().isEmpty()
                            ? storeItem.getStoreName()
                            : storeItem.getRealName() + "的店铺";

                    Store store = Store.builder()
                                       .no(IDNumber.generateStoreNo())
                                       .userId(userId)
                                       .name(finalStoreName)
                                       .build();

                    storeService.save(store);

                    // 保存用户资质认证信息
                    saveUserQualificationForItem(storeItem, userId);

                    // 添加商家账户类型
                    addMerchantAccountType(userId);

                    log.info("店铺注册审核通过，AuditItem ID: {}，用户ID: {}，店铺ID: {}",
                             item.getId(), userId, store.getId());
                } catch (Exception e) {
                    log.error("店铺注册审核通过处理失败，AuditItem ID: {}", item.getId(), e);
                    throw e;
                }
            } else if (AuditItemStatus.REJECTED.getCode().equals(item.getStatus())) {
                // 拒绝：记录拒绝原因
                log.info("店铺注册被拒绝，AuditItem ID: {}，原因: {}", item.getId(), item.getReason());
            }
        }

        log.info("店铺注册审核结果处理完成，批次ID: {}", auditId);
    }

    private Long resolveApplicantUserId(Audit audit, StoreRegisterAuditItemDTO storeItem) {
        Long userId = storeItem.getUserId();
        if (userId == null) {
            userId = audit.getApplicantId();
        }
        AssertUtils.notNull(userId, BizErrorCode.USER_NOT_AUTHENTICATED);
        return userId;
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

    /**
     * 保存用户资质认证信息（从DTO）
     *
     * @param item   店铺注册审核项目DTO
     * @param userId 用户ID
     */
    private void saveUserQualificationForItem(StoreRegisterAuditItemDTO item, Long userId) {
        UserQualification qualification = UserQualification.builder()
                                                           .userId(userId)
                                                           .subjectType(item.getSubjectType())
                                                           .realName(item.getRealName())
                                                           .idCard(item.getIdCard())
                                                           .idCardValidStart(item.getIdCardValidStart())
                                                           .idCardValidEnd(item.getIdCardValidEnd())
                                                           .idCardFront(item.getIdCardFront())
                                                           .idCardBack(item.getIdCardBack())
                                                           .licenseNumber(item.getLicenseNumber())
                                                           .licenseName(item.getLicenseName())
                                                           .establishmentDate(item.getEstablishmentDate())
                                                           .registeredAddress(item.getRegisteredAddress())
                                                           .licensePhoto(item.getLicensePhoto())
                                                           .accountName(item.getBankAccountName())
                                                           .cardNumber(item.getBankCardNumber())
                                                           .bankName(item.getBankName())
                                                           .branchName(item.getBankBranchName())
                                                           .mobile(item.getBankMobile())
                                                           .createdAt(LocalDateTime.now())
                                                           .updatedAt(LocalDateTime.now())
                                                           .build();

        userQualificationMapper.insert(qualification);
    }
}
