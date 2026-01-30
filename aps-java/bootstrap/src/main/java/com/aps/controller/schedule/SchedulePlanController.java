package com.aps.controller.schedule;


import com.aps.dto.request.schedule.PlanAdjustRequest;
import com.aps.dto.request.schedule.PublishPlanRequest;
import com.aps.dto.request.schedule.SchedulePlanQueryRequest;
import com.aps.dto.response.masterdata.GanttDataDTO;
import com.aps.dto.response.masterdata.PlanBucketDTO;
import com.aps.dto.response.masterdata.PlanConflictDTO;
import com.aps.dto.response.schedule.SchedulePlanDTO;
import com.aps.response.ApiResponse;
import com.aps.response.PageResult;
import com.aps.schedule.SchedulePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排产方案控制器
 *
 * @author APS System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/schedule/plans")
@RequiredArgsConstructor
@Tag(name = "排产方案管理", description = "排产方案的查询、发布、调整等操作")
public class SchedulePlanController {

    private final SchedulePlanService schedulePlanService;

    /**
     * 分页查询排产方案
     */
    @GetMapping
    @Operation(summary = "分页查询排产方案", description = "根据条件分页查询排产方案列表")
    public ApiResponse<PageResult<SchedulePlanDTO>> listPlans(@Valid SchedulePlanQueryRequest request) {
        log.info("接收查询排产方案请求, request={}", request);
        PageResult<SchedulePlanDTO> result = schedulePlanService.listPlans(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取排产方案详情
     */
    @GetMapping("/{planId}")
    @Operation(summary = "获取排产方案详情", description = "根据ID获取排产方案详细信息（含统计和冲突）")
    public ApiResponse<SchedulePlanDTO> getPlanById(
            @Parameter(description = "方案ID", required = true)
            @PathVariable Long planId) {
        log.info("接收获取排产方案详情请求, planId={}", planId);
        SchedulePlanDTO dto = schedulePlanService.getPlanById(planId);
        return ApiResponse.success(dto);
    }

    /**
     * 获取方案的班次桶列表
     */
    @GetMapping("/{planId}/buckets")
    @Operation(summary = "获取方案的班次桶列表", description = "获取排产方案的所有班次桶明细")
    public ApiResponse<List<PlanBucketDTO>> getBucketsByPlanId(
            @Parameter(description = "方案ID", required = true)
            @PathVariable Long planId) {
        log.info("接收获取方案桶列表请求, planId={}", planId);
        List<PlanBucketDTO> buckets = schedulePlanService.getBucketsByPlanId(planId);
        return ApiResponse.success(buckets);
    }

    /**
     * 获取方案的甘特图数据
     */
    @GetMapping("/{planId}/gantt")
    @Operation(summary = "获取方案的甘特图数据", description = "获取排产方案的甘特图展示数据")
    public ApiResponse<GanttDataDTO> getGanttData(
            @Parameter(description = "方案ID", required = true)
            @PathVariable Long planId) {
        log.info("接收获取甘特图数据请求, planId={}", planId);
        GanttDataDTO ganttData = schedulePlanService.getGanttData(planId);
        return ApiResponse.success(ganttData);
    }

    /**
     * 获取方案的冲突列表
     */
    @GetMapping("/{planId}/conflicts")
    @Operation(summary = "获取方案的冲突列表", description = "获取排产方案的所有冲突信息")
    public ApiResponse<List<PlanConflictDTO>> getConflictsByPlanId(
            @Parameter(description = "方案ID", required = true)
            @PathVariable Long planId) {
        log.info("接收获取方案冲突列表请求, planId={}", planId);
        List<PlanConflictDTO> conflicts = schedulePlanService.getConflictsByPlanId(planId);
        return ApiResponse.success(conflicts);
    }

    /**
     * 发布方案
     */
    @PostMapping("/publish")
    @Operation(summary = "发布方案", description = "发布排产方案，生成工单并更新订单状态")
    public ApiResponse<Void> publishPlan(@Valid @RequestBody PublishPlanRequest request) {
        log.info("接收发布方案请求, planId={}", request.getPlanId());
        schedulePlanService.publishPlan(request);
        return ApiResponse.success();
    }

    /**
     * 手动调整方案
     */
    @PostMapping("/adjust")
    @Operation(summary = "手动调整方案", description = "手动调整排产方案（移动、交换、删除、插入桶）")
    public ApiResponse<Void> adjustPlan(@Valid @RequestBody PlanAdjustRequest request) {
        log.info("接收手动调整方案请求, planId={}, changeCount={}", 
                request.getPlanId(), request.getChanges().size());
        schedulePlanService.adjustPlan(request);
        return ApiResponse.success();
    }

    /**
     * 作废方案
     */
    @PostMapping("/{planId}/discard")
    @Operation(summary = "作废方案", description = "作废排产方案")
    public ApiResponse<Void> discardPlan(
            @Parameter(description = "方案ID", required = true)
            @PathVariable Long planId) {
        log.info("接收作废方案请求, planId={}", planId);
        schedulePlanService.discardPlan(planId);
        return ApiResponse.success();
    }

    /**
     * 复制方案
     */
    @PostMapping("/{planId}/copy")
    @Operation(summary = "复制方案", description = "复制排产方案（包含所有桶和统计）")
    public ApiResponse<Long> copyPlan(
            @Parameter(description = "方案ID", required = true)
            @PathVariable Long planId) {
        log.info("接收复制方案请求, planId={}", planId);
        Long newPlanId = schedulePlanService.copyPlan(planId);
        return ApiResponse.success(newPlanId);
    }

    /**
     * 设置为最优方案
     */
    @PostMapping("/{planId}/set-best")
    @Operation(summary = "设置为最优方案", description = "将指定方案设置为最优方案")
    public ApiResponse<Void> setBestPlan(
            @Parameter(description = "方案ID", required = true)
            @PathVariable Long planId) {
        log.info("接收设置最优方案请求, planId={}", planId);
        schedulePlanService.setBestPlan(planId);
        return ApiResponse.success();
    }
}

