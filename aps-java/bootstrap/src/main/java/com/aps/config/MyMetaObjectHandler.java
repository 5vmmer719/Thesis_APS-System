package com.aps.config;


import com.aps.utils.RequestUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");

        // 填充创建时间
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());

        // 填充创建人
        String username = RequestUtil.getUsername();
        if (username != null) {
            this.strictInsertFill(metaObject, "createdBy", String.class, username);
        }

        // 填充更新时间
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());

        // 填充更新人
        if (username != null) {
            this.strictInsertFill(metaObject, "updatedBy", String.class, username);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");

        // 填充更新时间
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());

        // 填充更新人
        String username = RequestUtil.getUsername();
        if (username != null) {
            this.strictUpdateFill(metaObject, "updatedBy", String.class, username);
        }
    }
}

