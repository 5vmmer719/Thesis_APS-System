// facade/src/main/java/com/aps/dto/response/execution/ExceptionDTO.java
package com.aps.dto.response.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 异常响应DTO
 * 对应 OpenAPI ExceptionDTO
 */
@Data
@Schema(description = "异常详情")
public class ExceptionDTO {

    @Schema(description = "异常ID")
    private Long id;

    @Schema(description = "工单ID")
    private Long woId;

    @Schema(description = "异常类型")
    private String type;

    @Schema(description = "异常等级")
    private Integer level;

    @Schema(description = "状态：0-新建，1-处理中，2-已关闭")
    private Integer status;

    @Schema(description = "异常描述")
    private String desc;

    @Schema(description = "负载JSON")
    private Map<String, Object> payload;

    // ========== 以下是扩展字段（不在数据库表中） ==========

    @Schema(description = "状态文本 (扩展字段)")
    private String statusText;

    @Schema(description = "等级文本 (扩展字段)")
    private String levelText;
}
