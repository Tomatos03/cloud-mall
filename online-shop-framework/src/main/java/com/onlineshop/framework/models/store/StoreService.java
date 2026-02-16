package com.onlineshop.framework.models.store;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.audit.application.IAuditDelegate;
import com.onlineshop.framework.models.audit.entity.Audit;
import com.onlineshop.framework.models.audit.enums.AuditStatus;
import com.onlineshop.framework.models.audit.enums.AuditType;
import com.onlineshop.framework.models.audit.service.IAuditService;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.goods.spu.vo.GoodsCardVO;
import com.onlineshop.framework.models.store.dto.StoreGoodsParamsDTO;
import com.onlineshop.framework.models.store.dto.StoreRegistrationForm;
import com.onlineshop.framework.models.store.dto.StoreUpdateDTO;
import com.onlineshop.framework.models.store.vo.StoreVO;
import com.onlineshop.framework.models.system.user.IUserService;
import com.onlineshop.framework.models.system.user.entity.User;
import com.onlineshop.framework.models.system.user.entity.UserQualification;
import com.onlineshop.framework.models.system.user.mapper.UserQualificationMapper;
import com.onlineshop.framework.support.JsonSupport;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.IDNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 店铺相关业务 Service 实现
 */
@Service
@RequiredArgsConstructor
public class StoreService extends ServiceImpl<StoreMapper, Store> implements IStoreService, IAuditDelegate {
    private final IGoodsService goodsService;
    private final IUserService userService;
    private final UserQualificationMapper userQualificationMapper;
    private final IAuditService auditService;

    @Override
    public StoreVO getStoreInfoById(Long storeId) {
        Store store = getById(storeId);
        return buildStoreItemVO(store);
    }

    @Override
    public IPage<GoodsCardVO> pageStoreGoods(StoreGoodsParamsDTO queryDTO) {
        // 构建查询条件
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("store_id", queryDTO.getStoreId())
               .eq("status", true);

        Page<Goods> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());

        return goodsService.page(page, wrapper)
                           .convert(GoodsCardVO::convertGoodsCardVO);
    }

    @Override
    public StoreVO getMyStoreInfo() {
        Store store = queryStoreByUserId(AuthUserUtils.getUserId());
        return buildStoreItemVO(store);
    }

    @Override
    public void updateStore(StoreUpdateDTO updateDTO) {
        LambdaUpdateWrapper<Store> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Store::getId, AuthUserUtils.getStoreId())
                     .eq(Store::getUserId, AuthUserUtils.getUserId());

        updateWrapper.set(Objects.nonNull(updateDTO.getName()), Store::getName, updateDTO.getName());
        updateWrapper.set(Objects.nonNull(updateDTO.getInfo()), Store::getInfo, updateDTO.getInfo());
        updateWrapper.set(Objects.nonNull(updateDTO.getAvatarUrl()), Store::getAvatarUrl, updateDTO.getAvatarUrl());
        updateWrapper.set(Objects.nonNull(updateDTO.getBanner()), Store::getBanner, updateDTO.getBanner());

        update(updateWrapper);
    }

    @Override
    public Store queryStoreByUserId(Long userId) {
        return lambdaQuery().eq(Store::getUserId, userId)
                            .one();
    }

    @Override
    public StoreInfoDTO getMerchantInfo() {
        Store store = queryStoreByUserId(AuthUserUtils.getUserId());
        User user = userService.getById(AuthUserUtils.getUserId());
        return StoreInfoDTO.builder()
                           .uid(user.getId()
                                       .toString())
                           .storeId(store.getId())
                           .storeName(store.getName())
                           .nickname(user.getNickname())
                           .username(user.getUsername())
                           .avatarUrl(user.getAvatarUrl())
                           .build();
    }

    /**
     * 构建 StoreItemVO
     */
    private StoreVO buildStoreItemVO(Store store) {
        Objects.requireNonNull(store);

        return StoreVO.builder()
                      .id(String.valueOf(store.getId()))
                      .name(store.getName())
                      .description(store.getInfo())
                      .avatarUrl(store.getAvatarUrl())
                      .banner(store.getBanner())
                      .build();
    }

    @Override
    public AuditType getSupportAuditType() {
        return AuditType.STORE_REGISTER;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAudit(Object payload) {
        if (payload instanceof StoreRegistrationForm form) {
            UserQualification userQualification = convertFormToQualification(form);
            userQualification.setUserId(AuthUserUtils.getUserId());
            userQualificationMapper.insert(userQualification);
            auditService.save(buildAudit(form, userQualification.getId()));
        }
    }

    /**
     * 将StoreRegistrationForm转换为UserQualification
     */
    private UserQualification convertFormToQualification(StoreRegistrationForm form) {
        Long userId = AuthUserUtils.getUserId();

        return UserQualification.builder()
                                .userId(userId)
                                .auditStatus(AuditStatus.PENDING.getCode())
                                .subjectType(form.getSubjectType())
                                .realName(form.getRealName())
                                .idCard(form.getIdCard())
                                .idCardValidStart(form.getIdCardValidStart())
                                .idCardValidEnd(form.getIdCardValidEnd())
                                .idCardFront(form.getIdCardFront())
                                .idCardBack(form.getIdCardBack())
                                .licenseNumber(form.getLicenseNumber())
                                .licenseName(form.getLicenseName())
                                .establishmentDate(form.getEstablishmentDate())
                                .registeredAddress(form.getRegisteredAddress())
                                .licensePhoto(form.getLicensePhoto())
                                .categories(form.getCategories() != null ? String.join(",", form.getCategories()) : "")
                                .accountName(form.getBankAccountName())
                                .cardNumber(form.getBankCardNumber())
                                .bankName(form.getBankName())
                                .branchName(form.getBankBranchName())
                                .mobile(form.getBankMobile())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
    }

    private static Audit buildAudit(StoreRegistrationForm form, Long id) {
        return Audit.builder()
                    .targetId(id)
                    .targetType(AuditType.STORE_REGISTER.getCode())
                    .status(AuditStatus.PENDING.getCode())
                    .applicantId(AuthUserUtils.getUserId())
                    .applicantName(AuthUserUtils.getUsername())
                    .snapshot(JsonSupport.toJson(form))
                    .createTime(LocalDateTime.now())
                    .build();
    }

    @Override
    public void onAuditApproved(Audit validatedAudit) {
        auditService.updateAudit(buildAuditDecision(
                validatedAudit.getId(),
                AuditStatus.APPROVED,
                validatedAudit.getTargetId(),
                null,
                null
        ));
        save(buildNewStore(validatedAudit.getApplicantId()));
    }

    private Store buildNewStore(Long userId) {
        final String DEFAULT_STORE_NAME = AuthUserUtils.getUsername() +  "的店铺";
        return Store.builder()
                    .no(IDNumber.generateStoreNo())
                    .userId(userId)
                    .name(DEFAULT_STORE_NAME)
                    .build();
    }

    @Override
    public void onAuditRejected(Audit audit, String reason) {
        auditService.updateAudit(buildAuditDecision(
                audit.getId(),
                AuditStatus.REJECTED,
                null,
                null,
                reason
        ));
    }

    /**
     * 构建审核决策对象（审核通过/拒绝）
     */
    private Audit buildAuditDecision(Long auditId, AuditStatus status, Long targetId, String snapshot, String reason) {
        return Audit.builder()
                    .id(auditId)
                    .status(status.getCode())
                    .targetId(targetId)
                    .snapshot(snapshot)
                    .reason(reason)
                    .auditorId(AuthUserUtils.getUserId())
                    .auditorName(AuthUserUtils.getUsername())
                    .auditTime(LocalDateTime.now())
                    .build();
    }
}