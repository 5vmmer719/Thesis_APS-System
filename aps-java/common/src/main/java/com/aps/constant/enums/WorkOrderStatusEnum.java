// common/src/main/java/com/aps/constant/enums/WorkOrderStatusEnum.java
package com.aps.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单状态枚举
 */
@Getter
@AllArgsConstructor
public enum WorkOrderStatusEnum {

    /**
     * 0-待下达
     */
    PENDING(0, "待下达"),

    /**
     * 1-已下达
     */
    RELEASED(1, "已下达"),

    /**
     * 2-执行中
     */
    IN_PROGRESS(2, "执行中"),

    /**
     * 3-暂停
     */
    PAUSED(3, "暂停"),

    /**
     * 4-完成
     */
    COMPLETED(4, "完成"),

    /**
     * 9-作废
     */
    VOIDED(9, "作废");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static WorkOrderStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WorkOrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}

