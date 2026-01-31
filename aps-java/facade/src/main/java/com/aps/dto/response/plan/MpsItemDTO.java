package com.aps.dto.response.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MPS明细响应DTO
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "MPS明细响应")
public class MpsItemDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "MPS主表ID")
    private Long mpsId;

    @Schema(description = "计划日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bizDate;

    @Schema(description = "车型ID")
    private Long modelId;

    @Schema(description = "车型编码")
    private String modelCode;

    @Schema(description = "车型名称")
    private String modelName;

    @Schema(description = "计划数量")
    private Integer qty;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private String createdBy;
}

