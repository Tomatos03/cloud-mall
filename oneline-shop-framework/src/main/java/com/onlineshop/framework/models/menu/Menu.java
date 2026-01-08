package com.onlineshop.framework.models.menu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Menu {
    private String name; // vue-router 路由名称
    private String routePath; // vue-router 路由路径
    private String path; // 前端视图或布局文件路径
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

        public Meta(String title) {
            this.title = title;
        }
    }
}