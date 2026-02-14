package com.onlineshop.framework.config.mybatisPlus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.onlineshop.framework.utils.AuthUserUtils;
import org.apache.ibatis.reflection.MetaObject;
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
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 插入时自动填充 createUser / updateUser，如果有登录用户可动态获取
        this.strictInsertFill(metaObject, "createUser", String.class, getOperateUser());
        this.strictInsertFill(metaObject, "updateUser", String.class, getOperateUser());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时自动填充 updateTime
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 更新人
        this.strictUpdateFill(metaObject, "updateUser", String.class, getOperateUser());
    }

    private String getOperateUser() {
        String username = AuthUserUtils.getUsername();
        return StringUtils.hasText(username)
                ? username
                : SYSTEM_USER;
    }
}
