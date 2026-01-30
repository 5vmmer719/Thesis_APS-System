package com.aps.dto.request.order;


import com.aps.request.PageRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 生产订单查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "生产订单查询请求")
public class ProdOrderQueryRequest extends PageRequest {

    @Schema(description = "状态: 0-新建, 1-待审批, 2-已审批, 3-已排产, 4-执行中, 5-完成, 9-取消")
    private Integer status;

    @Schema(description = "订单类型: 1-常规, 2-紧急, 3-定制, 4-插单, 5-返工")
    private Integer orderKind;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "交期开始日期", example = "2026-01-01")
    private LocalDate fromDueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "交期结束日期", example = "2026-12-31")
    private LocalDate toDueDate;

    @Schema(description = "关键字(订单号)")
    private String keyword;
}
