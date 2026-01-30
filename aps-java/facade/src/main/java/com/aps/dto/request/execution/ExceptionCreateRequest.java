// facade/src/main/java/com/aps/dto/request/execution/ExceptionCreateRequest.java
package com.aps.dto.request.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 异常创建请求
 * 对应OpenAPI ExceptionCreate
 */
@Data
@Schema(description = "异常创建请求")
public class ExceptionCreateRequest {

    @NotNull(message = "工单ID不能为空")
    @Schema(description = "工单ID", required = true)
    private Long woId;

    @NotBlank(message = "异常类型不能为空")
    @Schema(description = "异常类型", required = true)
    private String type;

    @NotNull(message = "异常等级不能为空")
    @Schema(description = "异常等级", required = true)
    private Integer level;

    @NotBlank(message = "异常描述不能为空")
    @Schema(description = "异常描述", required = true)
    private String desc;

    @Schema(description = "负载JSON")
    private Map<String, Object> payload;
}
