package com.cloudmall.framework.models.file;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/21
 */
@Data
@AllArgsConstructor
public class FileMeta implements Serializable {
    String url;
}