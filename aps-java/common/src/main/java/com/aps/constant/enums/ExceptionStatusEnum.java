// common/src/main/java/com/aps/constant/enums/ExceptionStatusEnum.java
package com.aps.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 异常状态枚举
 */
@Getter
@AllArgsConstructor
public enum ExceptionStatusEnum {

    /**
     * 0-新建
     */
    NEW(0, "新建"),

    /**
     * 1-处理中
     */
    IN_PROGRESS(1, "处理中"),

    /**
     * 2-已关闭
     */
    CLOSED(2, "已关闭");

    private final Integer code;
    private final String desc;

    public static ExceptionStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ExceptionStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
