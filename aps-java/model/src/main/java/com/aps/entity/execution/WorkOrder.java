// model/src/main/java/com/aps/entity/execution/WorkOrder.java
package com.aps.entity.execution;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 工单实体
 * 对应表: exe_work_order
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exe_work_order")
@Schema(description = "工单")
public class WorkOrder extends BaseEntity {

    @Schema(description = "工单ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "工单号")
    private String woNo;

    @Schema(description = "排产方案ID")
    private Long planId;

    @Schema(description = "排产桶ID")
    private Long planBucketId;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "生产订单ID")
    private Long prodOrderId;

    @Schema(description = "产线ID")
    private Long lineId;

    @Schema(description = "业务日期")
    private LocalDate bizDate;

    @Schema(description = "班次编码")
    private String shiftCode;

    @Schema(description = "顺序号")
    private Integer seqNo;

    @Schema(description = "计划数量")
    private Integer qtyPlanned;

    @Schema(description = "完成数量")
    private Integer qtyDone;

    @Schema(description = "状态：0-待下达，1-已下达，2-执行中，3-暂停，4-完成，9-作废")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
