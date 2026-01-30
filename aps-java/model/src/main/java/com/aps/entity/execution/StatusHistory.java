// model/src/main/java/com/aps/entity/execution/StatusHistory.java
package com.aps.entity.execution;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exe_status_history")
@Schema(description = "工单状态历史")
public class StatusHistory extends BaseEntity {

    @TableId
    @Schema(description = "历史记录ID")
    private Long id;

    @Schema(description = "工单ID")
    private Long woId;

    @Schema(description = "原状态")
    private Integer fromStatus;

    @Schema(description = "新状态")
    private Integer toStatus;

    @Schema(description = "操作人ID")
    private String operatorId;  // 改为 VARCHAR(50)，与 created_by 一致

    @Schema(description = "备注")
    private String remark;
}
