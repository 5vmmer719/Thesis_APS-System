package com.aps.entity.masterdata;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 换型矩阵实体类
 * 用于定义不同工艺下，从一个状态切换到另一个状态所需的时间和成本
 * 
 * @author APS System
 * @since 2024-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_setup_matrix")
public class SetupMatrix extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 工艺类型：1-冲压，2-焊装，3-涂装，4-总装
     */
    private Integer processType;

    /**
     * 源换型键（如：模具编码、颜色编码、夹具编码）
     */
    private String fromKey;

    /**
     * 目标换型键
     */
    private String toKey;

    /**
     * 换型时间（分钟）
     */
    private Integer setupMinutes;

    /**
     * 换型成本
     */
    private BigDecimal setupCost;

    /**
     * 状态：1-启用，0-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 工艺类型枚举
     */
    public enum ProcessType {
        STAMPING(1, "冲压"),
        WELDING(2, "焊装"),
        PAINTING(3, "涂装"),
        ASSEMBLY(4, "总装");

        private final Integer code;
        private final String desc;

        ProcessType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static String getDesc(Integer code) {
            if (code == null) {
                return "未知";
            }
            for (ProcessType type : values()) {
                if (type.code.equals(code)) {
                    return type.desc;
                }
            }
            return "未知";
        }
    }
}

