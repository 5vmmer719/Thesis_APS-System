// facade/src/main/java/com/aps/dto/request/execution/ExceptionQueryRequest.java
package com.aps.dto.request.execution;

import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 异常查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "异常查询请求")
public class ExceptionQueryRequest extends PageRequest {

    @Schema(description = "状态：0-新建，1-处理中，2-已关闭")
    private Integer status;

    @Schema(description = "异常等级")
    private Integer level;

    @Schema(description = "异常类型")
    private String type;

    @Schema(description = "开始时间")
    private LocalDateTime fromTime;

    @Schema(description = "结束时间")
    private LocalDateTime toTime;
}
