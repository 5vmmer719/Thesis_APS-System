package com.aps.dto.response.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * MRP响应DTO
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "MRP响应")
public class MrpDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "MRP计划编号")
    private String mrpNo;

    @Schema(description = "MPS主表ID")
    private Long mpsId;

    @Schema(description = "MPS编号")
    private String mpsNo;

    @Schema(description = "状态: 0-生成中, 1-完成, 2-失败")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "运算结果数据")
    private Map<String, Object> resultPayload;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @Schema(description = "更新人")
    private String updatedBy;

    @Schema(description = "明细列表")
    private List<MrpItemDTO> items;
}

