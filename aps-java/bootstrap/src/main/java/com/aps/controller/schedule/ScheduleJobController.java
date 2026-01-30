package com.aps.controller.schedule;


import com.aps.dto.request.schedule.ScheduleJobCreateRequest;
import com.aps.dto.request.schedule.ScheduleJobQueryRequest;
import com.aps.dto.response.schedule.ScheduleJobDTO;
import com.aps.dto.response.schedule.SchedulePlanDTO;
import com.aps.response.ApiResponse;
import com.aps.response.PageResult;
import com.aps.schedule.ScheduleJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排产任务控制器
 *
 * @author APS System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/schedule/jobs")
@RequiredArgsConstructor
@Tag(name = "排产任务管理", description = "排产任务的创建、运行、查询等操作")
public class ScheduleJobController {

    private final ScheduleJobService scheduleJobService;

    /**
     * 创建排产任务
     */
    @PostMapping
    @Operation(summary = "创建排产任务", description = "创建新的排产任务，配置排产范围、目标权重和约束规则")
    public ApiResponse<Long> createJob(@Valid @RequestBody ScheduleJobCreateRequest request) {
        log.info("接收创建排产任务请求, horizonStart={}, horizonEnd={}", 
                request.getHorizonStart(), request.getHorizonEnd());
        Long jobId = scheduleJobService.createJob(request);
        return ApiResponse.success(jobId);
    }

    /**
     * 分页查询排产任务
     */
    @GetMapping
    @Operation(summary = "分页查询排产任务", description = "根据条件分页查询排产任务列表")
    public ApiResponse<PageResult<ScheduleJobDTO>> listJobs(@Valid ScheduleJobQueryRequest request) {
        log.info("接收查询排产任务请求, request={}", request);
        PageResult<ScheduleJobDTO> result = scheduleJobService.listJobs(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取排产任务详情
     */
    @GetMapping("/{jobId}")
    @Operation(summary = "获取排产任务详情", description = "根据ID获取排产任务详细信息")
    public ApiResponse<ScheduleJobDTO> getJobById(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long jobId) {
        log.info("接收获取排产任务详情请求, jobId={}", jobId);
        ScheduleJobDTO dto = scheduleJobService.getJobById(jobId);
        return ApiResponse.success(dto);
    }

    /**
     * 运行排产任务
     */
    @PostMapping("/{jobId}/run")
    @Operation(summary = "运行排产任务", description = "启动排产任务，调用排产引擎进行求解")
    public ApiResponse<Void> runJob(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long jobId) {
        log.info("接收运行排产任务请求, jobId={}", jobId);
        scheduleJobService.runJob(jobId);
        return ApiResponse.success();
    }

    /**
     * 停止排产任务
     */
    @PostMapping("/{jobId}/stop")
    @Operation(summary = "停止排产任务", description = "停止正在运行的排产任务")
    public ApiResponse<Void> stopJob(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long jobId) {
        log.info("接收停止排产任务请求, jobId={}", jobId);
        scheduleJobService.stopJob(jobId);
        return ApiResponse.success();
    }

    /**
     * 删除排产任务
     */
    @DeleteMapping("/{jobId}")
    @Operation(summary = "删除排产任务", description = "删除排产任务（软删除）")
    public ApiResponse<Void> deleteJob(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long jobId) {
        log.info("接收删除排产任务请求, jobId={}", jobId);
        scheduleJobService.deleteJob(jobId);
        return ApiResponse.success();
    }

    /**
     * 获取任务的所有方案
     */
    @GetMapping("/{jobId}/plans")
    @Operation(summary = "获取任务的所有方案", description = "获取指定任务下的所有排产方案")
    public ApiResponse<List<SchedulePlanDTO>> getPlansByJobId(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long jobId) {
        log.info("接收获取任务方案列表请求, jobId={}", jobId);
        List<SchedulePlanDTO> plans = scheduleJobService.getPlansByJobId(jobId);
        return ApiResponse.success(plans);
    }
}

