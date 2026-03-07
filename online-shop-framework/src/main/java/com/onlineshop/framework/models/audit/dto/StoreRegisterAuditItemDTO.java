package com.onlineshop.framework.models.audit.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 店铺注册审核项目DTO
 * 代表一个审核批次中的单个店铺注册申请项目
 * 
 * 设计说明：
 * - 对应 AuditItem 表，存储在 snapshot 字段中
 * - 包含店铺注册申请时的所有信息
 * - 审核员基于此信息做出批准或拒绝决策
 *
 * @author Tomatos
 * @date 2026/3/7
 */
@Data
public class StoreRegisterAuditItemDTO {
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
