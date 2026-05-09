package com.cloudmall.framework.models.system.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资质认证信息表（包含银行卡信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_qualification")
public class UserQualification implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 主体类型: personal/individual/enterprise
     */
    private String subjectType;

    private String auditStatus;
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 身份证有效期起
     */
    private LocalDate idCardValidStart;
    
    /**
     * 身份证有效期止
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
     * 营业执照编号
     */
    private String licenseNumber;
    
    /**
     * 营业执照名称
     */
    private String licenseName;
    
    /**
     * 营业执照成立日期
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
     * 可经营类目（逗号分隔或JSON）
     */
    private String categories;
    
    /**
     * 开户人姓名
     */
    private String accountName;
    
    /**
     * 银行卡号
     */
    private String cardNumber;
    
    /**
     * 开户银行
     */
    private String bankName;
    
    /**
     * 开户支行
     */
    private String branchName;
    
    /**
     * 银行预留手机号
     */
    private String mobile;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}