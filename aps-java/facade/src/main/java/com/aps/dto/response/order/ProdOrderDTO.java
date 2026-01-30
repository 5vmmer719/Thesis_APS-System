package com.aps.dto.response.order;


import com.aps.entity.order.ProductionOrderAttr;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产订单响应DTO
 */
@Data
@Schema(description = "生产订单响应")
public class ProdOrderDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "生产订单号")
    private String prodNo;

    @Schema(description = "销售订单ID")
    private Long salesId;

    @Schema(description = "销售订单号")
    private String salesNo;

    @Schema(description = "订单类型: 1-常规, 2-紧急, 3-定制, 4-插单, 5-返工")
    private Integer orderKind;

    @Schema(description = "订单类型文本")
    private String orderKindText;

    @Schema(description = "车型ID")
    private Long modelId;

    @Schema(description = "车型编码")
    private String modelCode;

    @Schema(description = "车型名称")
    private String modelName;

    @Schema(description = "BOM ID")
    private Long bomId;

    @Schema(description = "BOM编码")
    private String bomCode;

    @Schema(description = "工艺路线版本")
    private String routeVersion;

    @Schema(description = "订单数量")
    private Integer qty;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "交期")
    private LocalDate dueDate;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "状态: 0-新建, 1-待审批, 2-已审批, 3-已排产, 4-执行中, 5-完成, 9-取消")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "订单属性列表")
    private List<ProductionOrderAttr> attrs;

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
