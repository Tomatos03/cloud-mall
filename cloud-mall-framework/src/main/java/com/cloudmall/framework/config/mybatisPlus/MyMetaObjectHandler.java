package com.cloudmall.framework.config.mybatisPlus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cloudmall.framework.utils.AuthUserUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/11
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    private static final String SYSTEM_USER = "system";

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时自动填充 createTime / updateTime
        if (metaObject.hasSetter("createTime")) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("updateTime")) {
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }

        // 插入时自动填充 createUser / updateUser，如果有登录用户可动态获取
        if (metaObject.hasSetter("createUser")) {
            this.strictInsertFill(metaObject, "createUser", String.class, getOperateUser());
        }
        if (metaObject.hasSetter("updateUser")) {
            this.strictInsertFill(metaObject, "updateUser", String.class, getOperateUser());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 检查字段是否存在，再进行填充
        if (metaObject.hasSetter("updateTime")) {
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }

        if (metaObject.hasSetter("updateUser")) {
            this.strictUpdateFill(metaObject, "updateUser", String.class, getOperateUser());
        }
    }

    private String getOperateUser() {
        if (AuthUserUtils.getAuthentication() == null) {
            return SYSTEM_USER;
        }
        return AuthUserUtils.getUsername();
    }
}
