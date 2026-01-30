package com.aps.entity.masterdata;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 工序实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_operation", autoResultMap = true)
@Schema(description = "工序定义")
public class Operation extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "工艺路线ID")
    private Long routeId;

    @Schema(description = "工序编码")
    private String opCode;

    @Schema(description = "工序名称")
    private String opName;

    @Schema(description = "序号")
    private Integer seqNo;

    @Schema(description = "标准工时（分钟/辆）")
    private Integer stdMinutesPerUnit;

    @Schema(description = "准备/换型时间（分钟）")
    private Integer setupMinutes;

    @Schema(description = "可选工位组")
    private String stationGroup;

    @Schema(description = "工艺约束参数（JSON）")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> constraintJson;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
