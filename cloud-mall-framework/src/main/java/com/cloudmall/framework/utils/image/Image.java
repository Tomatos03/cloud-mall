package com.cloudmall.framework.utils.image;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/16
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Image implements Serializable {
    private String uuid;
    private String name;
    private String url;

    public Image(String url) {
        this.url = url;
    }
}
