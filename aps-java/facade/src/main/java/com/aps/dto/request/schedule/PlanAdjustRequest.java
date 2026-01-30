package com.aps.dto.request.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 方案手动调整请求
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@Schema(description = "方案手动调整请求")
public class PlanAdjustRequest {

    @NotNull(message = "方案ID不能为空")
    @Schema(description = "方案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long planId;

    @NotNull(message = "调整变更列表不能为空")
    @Schema(description = "调整变更列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PlanAdjustChange> changes;

    @Schema(description = "备注")
    private String remark;

    /**
     * 方案调整变更
     */
    @Data
    @Schema(description = "方案调整变更")
    public static class PlanAdjustChange {
        @Schema(description = "变更类型: MOVE-移动, SWAP-交换, DELETE-删除, INSERT-插入", example = "MOVE")
        private String changeType;

        @Schema(description = "源桶ID", example = "5001")
        private Long sourceBucketId;

        @Schema(description = "目标桶ID", example = "5002")
        private Long targetBucketId;

        @Schema(description = "变更详情")
        private Map<String, Object> details;
    }
}

