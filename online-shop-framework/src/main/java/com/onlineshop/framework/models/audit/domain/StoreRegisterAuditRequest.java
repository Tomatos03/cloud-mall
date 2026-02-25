package com.onlineshop.framework.models.audit.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 店铺注册审核请求
 * 包含店铺注册申请时需要的所有信息
 *
 * @author Tomatos
 * @date 2026/2/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreRegisterAuditRequest extends AuditRequest {
    
    /**
     * 主体类型
     */
    private String subjectType;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 身份证有效期开始
     */
    private LocalDate idCardValidStart;
    
    /**
     * 身份证有效期结束
     */
    private LocalDate idCardValidEnd;
    
    /**
     * 身份证正面照片URL
     */
    private String idCardFront;
    
    /**
     * 身份证反面照片URL
     */
    private String idCardBack;
    
    /**
     * 营业执照注册号
     */
    private String licenseNumber;
    
    /**
     * 营业执照名称
     */
    private String licenseName;
    
    /**
     * 成立日期
     */
    private LocalDate establishmentDate;
    
    /**
     * 注册地址
     */
    private String registeredAddress;
    
    /**
     * 营业执照照片URL
     */
    private String licensePhoto;
    
    /**
     * 店铺名称
     */
    private String storeName;
    
    /**
     * 经营类目
     */
    private List<Long> categories;
    
    /**
     * 发货地址
     */
    private String shippingAddress;
    
    /**
     * 银行账户名
     */
    private String bankAccountName;
    
    /**
     * 银行卡号
     */
    private String bankCardNumber;
    
    /**
     * 开户银行
     */
    private String bankName;
    
    /**
     * 开户支行
     */
    private String bankBranchName;
    
    /**
     * 银行预留手机号
     */
    private String bankMobile;
}
