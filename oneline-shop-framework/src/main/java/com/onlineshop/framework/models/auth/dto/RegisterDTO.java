package com.onlineshop.framework.models.auth.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/31
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RegisterDTO extends LoginDTO {
}