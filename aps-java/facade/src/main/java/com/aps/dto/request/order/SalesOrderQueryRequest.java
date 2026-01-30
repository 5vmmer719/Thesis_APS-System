package com.aps.dto.request.order;


import com.aps.request.PageRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 销售订单查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "销售订单查询请求")
public class SalesOrderQueryRequest extends PageRequest {

    @Schema(description = "车型ID")
    private Long modelId;

    @Schema(description = "状态: 0-新建, 1-已审核, 2-已转生产, 9-取消")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "交期开始日期", example = "2026-01-01")
    private LocalDate fromDueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "交期结束日期", example = "2026-12-31")
    private LocalDate toDueDate;

    @Schema(description = "关键字(订单号/客户编码)")
    private String keyword;
}
