// facade/src/main/java/com/aps/dto/response/execution/WorkOrderDTO.java
package com.aps.dto.response.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 工单响应DTO
 * 严格对应 OpenAPI WorkOrderDTO 定义
 */
@Data
@Schema(description = "工单详情")
public class WorkOrderDTO {

    @Schema(description = "工单ID")
    private Long id;

    @Schema(description = "工单号")
    private String woNo;

    @Schema(description = "排产方案ID")
    private Long planId;

    @Schema(description = "排产桶ID")
    private Long planBucketId;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "生产订单ID")
    private Long prodOrderId;

    @Schema(description = "产线ID")
    private Long lineId;

    @Schema(description = "业务日期")
    private LocalDate bizDate;

    @Schema(description = "班次编码")
    private String shiftCode;

    @Schema(description = "顺序号")
    private Integer seqNo;

    @Schema(description = "计划数量")
    private Integer qtyPlanned;

    @Schema(description = "完成数量")
    private Integer qtyDone;

    @Schema(description = "状态：0-待下达，1-已下达，2-执行中，3-暂停，4-完成，9-作废")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    // ========== 以下是扩展字段（不在数据库表中） ==========

    @Schema(description = "状态文本 (扩展字段)")
    private String statusText;

    @Schema(description = "工艺类型文本 (扩展字段)")
    private String processTypeText;

    @Schema(description = "完成率 (扩展字段)")
    private Double completionRate;
}
