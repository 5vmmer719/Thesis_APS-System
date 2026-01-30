package com.aps.dto.request.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建BOM请求
 */
@Data
@Schema(description = "创建BOM请求")
public class BomCreateRequest {

    @Schema(description = "BOM编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "BOM编码不能为空")
    private String bomCode;

    // bomName 字段不存在于数据库表中
    // @Schema(description = "BOM名称", requiredMode = Schema.RequiredMode.REQUIRED)
    // @NotBlank(message = "BOM名称不能为空")
    // private String bomName;

    @Schema(description = "车型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "车型ID不能为空")
    private Long modelId;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "版本号不能为空")
    private String version;

    @Schema(description = "生效开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生效开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveFrom;

    @Schema(description = "生效结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTo;

    @Schema(description = "状态", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "BOM明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "BOM明细不能为空")
    @Valid
    private List<BomItemCreateRequest> items;
}

