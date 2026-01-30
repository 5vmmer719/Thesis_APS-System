// facade/src/main/java/com/aps/dto/request/execution/WorkOrderQueryRequest.java
package com.aps.dto.request.execution;

import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 工单查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工单查询请求")
public class WorkOrderQueryRequest extends PageRequest {

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "产线ID")
    private Long lineId;

    @Schema(description = "业务日期")
    private LocalDate bizDate;

    @Schema(description = "班次编码")
    private String shiftCode;

    @Schema(description = "状态：0-待下达，1-已下达，2-执行中，3-暂停，4-完成，9-作废")
    private Integer status;
}
