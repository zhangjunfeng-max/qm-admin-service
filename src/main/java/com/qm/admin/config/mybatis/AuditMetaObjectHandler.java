package com.qm.admin.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.qm.admin.common.constants.Constants;
import com.qm.admin.system.util.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_TIME = "updateTime";
    private static final String CREATE_BY = "createBy";
    private static final String UPDATE_BY = "updateBy";
    private static final String DELETED = "deleted";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);
        strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);
        strictInsertFill(metaObject, CREATE_BY, Long.class, getCurrentUserId());
        strictInsertFill(metaObject, UPDATE_BY, Long.class, getCurrentUserId());
        strictInsertFill(metaObject, DELETED, Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, UPDATE_BY, Long.class, getCurrentUserId());
    }

    private Long getCurrentUserId() {
        // 登录失效后的退出、刷新令牌撤销和定时清理仍会更新审计字段；这些系统内部操作不能写入 NULL。
        return java.util.Optional.ofNullable(SecurityUtils.getCurrentUserId())
                .orElse(Constants.SYSTEM_USER_ID);
    }
}
