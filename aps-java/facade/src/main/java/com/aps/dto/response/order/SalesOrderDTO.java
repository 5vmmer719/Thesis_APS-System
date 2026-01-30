package com.aps.dto.response.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售订单响应DTO
 */
@Data
@Schema(description = "销售订单响应")
public class SalesOrderDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "销售订单号")
    private String salesNo;

    @Schema(description = "客户编码")
    private String customerCode;

    @Schema(description = "车型ID")
    private Long modelId;

    @Schema(description = "车型编码")
    private String modelCode;

    @Schema(description = "车型名称")
    private String modelName;

    @Schema(description = "订单数量")
    private Integer qty;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "交期")
    private LocalDate dueDate;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "状态: 0-新建, 1-已审核, 2-已转生产, 9-取消")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "来源数据")
    private String sourcePayload;

    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    @Schema(description = "更新人")
    private String updatedBy;
}
