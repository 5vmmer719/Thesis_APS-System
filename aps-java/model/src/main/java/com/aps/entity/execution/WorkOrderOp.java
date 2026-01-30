// model/src/main/java/com/aps/entity/execution/WorkOrderOp.java
package com.aps.entity.execution;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单工序执行实体
 * 对应表: exe_work_order_op
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exe_work_order_op")
@Schema(description = "工单工序执行")
public class WorkOrderOp extends BaseEntity {

    @Schema(description = "工序执行ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "工单ID")
    private Long woId;

    @Schema(description = "工序编码")
    private String opCode;

    @Schema(description = "顺序号")
    private Integer seqNo;

    @Schema(description = "工位ID")
    private Long stationId;

    @Schema(description = "完成数量")
    private Integer qtyDone;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
