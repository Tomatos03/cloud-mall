package com.onlineshop.framework.models.menu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Menu {
    private String name;
    private String path;
    private String component;
    private String redirect;
    @JsonProperty("type")
    private String routeRecordRawType;
    private Meta meta;
    private List<Menu> children;
    
    @Data
    public static class Meta {
        private String title;
        private String icon;

        public Meta(String title, String icon) {
            this.title = title;
            this.icon = icon;
        }
    }
}