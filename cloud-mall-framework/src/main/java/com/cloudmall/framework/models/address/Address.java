package com.cloudmall.framework.models.address;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 地址管理实体类
 *
 * @author Tomatos
 * @date 2025/12/20
 */
@TableName("address")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String receiver;
    private Integer regionCode;
    private String fullAddress; // 完整地区地址
    private String detail;
    private String zipCode; // 邮政编码
    private String phone;
    private Boolean isDefault;
    private Date createdAt;
    private Date updatedAt;
}
