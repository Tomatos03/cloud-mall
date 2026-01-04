package com.onlineshop.framework.models.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/1
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long id;
    private String receiver;
    private Integer regionCode;
    private String fullAddress; // 完整地区地址
    private String detail;
    private String zipCode; // 邮政编码
    private String phone;
    private Boolean isDefault;
}
