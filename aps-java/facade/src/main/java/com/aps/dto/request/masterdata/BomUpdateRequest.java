package com.aps.dto.request.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新BOM请求
 */
@Data
@Schema(description = "更新BOM请求")
public class BomUpdateRequest {

    @Schema(description = "BOM ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "BOM ID不能为空")
    private Long id;

    // bomName 字段不存在于数据库表中
    // @Schema(description = "BOM名称")
    // private String bomName;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "生效开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveFrom;

    @Schema(description = "生效结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTo;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "BOM明细列表（如果提供则全量更新）")
    @Valid
    private List<BomItemCreateRequest> items;
}
