package com.onlineshop.framework.models.audit.dto;

import lombok.*;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditStatusDTO {
    private String status;
    private String storeNo;
}
