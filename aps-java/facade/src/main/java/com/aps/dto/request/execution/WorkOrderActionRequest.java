// facade/src/main/java/com/aps/dto/request/execution/WorkOrderActionRequest.java
package com.aps.dto.request.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工单操作请求
 * 对应OpenAPI WorkOrderActionRequest
 */
@Data
@Schema(description = "工单操作请求")
public class WorkOrderActionRequest {

    @Schema(description = "备注")
    private String remark;
}
