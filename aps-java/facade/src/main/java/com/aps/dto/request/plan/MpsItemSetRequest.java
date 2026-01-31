package com.aps.dto.request.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * MPS明细设置请求
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "MPS明细设置请求")
public class MpsItemSetRequest {

    @Schema(description = "操作模式: REPLACE-替换, APPEND-追加", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作模式不能为空")
    private ModeEnum mode;

    @Schema(description = "明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "明细列表不能为空")
    @Valid
    private List<MpsItemRequest> items;

    public enum ModeEnum {
        REPLACE,
        APPEND
    }
}

