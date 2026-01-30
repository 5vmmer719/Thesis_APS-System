// model/src/main/java/com/aps/entity/execution/Report.java
package com.aps.entity.execution;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报工记录实体
 * 对应表: exe_report
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exe_report")
@Schema(description = "报工记录")
public class Report extends BaseEntity {

    @Schema(description = "报工ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "工单ID")
    private Long woId;

    @Schema(description = "报工时间")
    private LocalDateTime reportTime;

    @Schema(description = "工位ID")
    private Long stationId;

    @Schema(description = "良品数量")
    private Integer qtyGood;

    @Schema(description = "不良品数量")
    private Integer qtyBad;

    @Schema(description = "详情JSON")
    private String detailJson;
}
