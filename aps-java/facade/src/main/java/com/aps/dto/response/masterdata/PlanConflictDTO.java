package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 方案冲突响应DTO
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@Schema(description = "方案冲突响应")
public class PlanConflictDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "方案ID")
    private Long planId;

    @Schema(description = "冲突类型")
    private String conflictType;

    @Schema(description = "冲突类型文本")
    private String conflictTypeText;

    @Schema(description = "级别: 1-提示, 2-警告, 3-致命")
    private Integer level;

    @Schema(description = "级别文本")
    private String levelText;

    @Schema(description = "对象类型")
    private String objectType;

    @Schema(description = "对象ID")
    private Long objectId;

    @Schema(description = "冲突消息")
    private String message;

    @Schema(description = "详细数据")
    private Map<String, Object> payload;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}

