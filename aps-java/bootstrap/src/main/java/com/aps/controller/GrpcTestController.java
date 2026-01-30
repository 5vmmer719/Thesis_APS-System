package com.aps.controller;


import com.aps.client.ScheduleEngineClient;
import com.aps.grpc.proto.*;
import com.aps.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * gRPC 测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/grpc-test")
@RequiredArgsConstructor
@Tag(name = "gRPC 测试", description = "测试 gRPC 连接和接口")
public class GrpcTestController {

    private final ScheduleEngineClient scheduleEngineClient;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查 gRPC 引擎连接状态")
    public ApiResponse<Map<String, Object>> healthCheck() {
        log.info("执行 gRPC 健康检查");
        
        Map<String, Object> result = new HashMap<>();
        result.put("connected", scheduleEngineClient.healthCheck());
        result.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success(result);
    }

    /**
     * 测试同步求解（简单示例）
     */
    @PostMapping("/test-solve")
    @Operation(summary = "测试同步求解", description = "使用示例数据测试同步求解接口")
    public ApiResponse<Map<String, Object>> testSolve() {
        log.info("测试 gRPC Solve 接口");
        
        try {
            // 构造测试请求
            SolveRequest request = SolveRequest.newBuilder()
                    .setRequestId("test-" + System.currentTimeMillis())
                    .setPlanStartEpochMs(System.currentTimeMillis())
                    .addJobs(Job.newBuilder()
                            .setVin("TEST_VIN_001")
                            .setDueEpochMs(System.currentTimeMillis() + 3600000)
                            .setStampingMinutes(15)
                            .setWeldingMinutes(20)
                            .setPaintingMinutes(30)
                            .setAssembleMinutes(60)
                            .setMoldCode("MOLD_A")
                            .setWeldingFixture("FIX_B")
                            .setColor("RED")
                            .setConfig("BASE")
                            .setEnergyScore(100.0)
                            .setEmissionScore(50.0)
                            .build())
                    .addJobs(Job.newBuilder()
                            .setVin("TEST_VIN_002")
                            .setDueEpochMs(System.currentTimeMillis() + 7200000)
                            .setStampingMinutes(15)
                            .setWeldingMinutes(20)
                            .setPaintingMinutes(30)
                            .setAssembleMinutes(60)
                            .setMoldCode("MOLD_A")
                            .setWeldingFixture("FIX_B")
                            .setColor("BLUE")
                            .setConfig("PREMIUM")
                            .setEnergyScore(120.0)
                            .setEmissionScore(60.0)
                            .build())
                    .setParams(SolveParams.newBuilder()
                            .setAlgorithm("baseline")
                            .setTimeBudgetSec(5)
                            .setSeed(42)
                            .setWeights(Weights.newBuilder()
                                    .setTardiness(10.0)
                                    .setColorChange(50.0)
                                    .setConfigChange(30.0)
                                    .setEnergyExcess(2.0)
                                    .setEmissionExcess(3.0)
                                    .setMaterialShortage(0.0)
                                    .build())
                            .setLimits(Limits.newBuilder()
                                    .setMaxEnergyPerShift(5000.0)
                                    .setMaxEmissionPerShift(2500.0)
                                    .build())
                            .build())
                    .build();
            
            // 调用 gRPC
            SolveResponse response = scheduleEngineClient.solve(request);
            
            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("requestId", response.getRequestId());
            result.put("engineVersion", response.getEngineVersion());
            
            if (response.hasSummary()) {
                KpiSummary summary = response.getSummary();
                Map<String, Object> kpi = new HashMap<>();
                kpi.put("cost", summary.getCost());
                kpi.put("totalTardinessMin", summary.getTotalTardinessMin());
                kpi.put("colorChanges", summary.getColorChanges());
                kpi.put("configChanges", summary.getConfigChanges());
                kpi.put("elapsedMs", summary.getElapsedMs());
                result.put("kpi", kpi);
            }
            
            result.put("orderCount", response.getOrderCount());
            result.put("scheduleCount", response.getScheduleCount());
            result.put("violationCount", response.getViolationsCount());
            result.put("warningCount", response.getWarningsCount());
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("gRPC Solve 测试失败", e);
            return ApiResponse.error(50002, "gRPC 调用失败: " + e.getMessage());
        }
    }

    /**
     * 测试异步任务提交
     */
    @PostMapping("/test-submit-job")
    @Operation(summary = "测试异步任务提交", description = "测试异步任务提交接口")
    public ApiResponse<Map<String, Object>> testSubmitJob() {
        log.info("测试 gRPC SubmitJob 接口");
        
        try {
            // 构造测试请求（同上）
            SolveRequest request = SolveRequest.newBuilder()
                    .setRequestId("async-test-" + System.currentTimeMillis())
                    .setPlanStartEpochMs(System.currentTimeMillis())
                    .addJobs(Job.newBuilder()
                            .setVin("ASYNC_VIN_001")
                            .setDueEpochMs(System.currentTimeMillis() + 3600000)
                            .setAssembleMinutes(60)
                            .setColor("RED")
                            .setConfig("BASE")
                            .build())
                    .setParams(SolveParams.newBuilder()
                            .setAlgorithm("sa")
                            .setTimeBudgetSec(10)
                            .build())
                    .build();
            
            // 提交任务
            SubmitJobResponse response = scheduleEngineClient.submitJob(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("jobId", response.getJobId());
            result.put("message", response.getMessage());
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("gRPC SubmitJob 测试失败", e);
            return ApiResponse.error(50002, "gRPC 调用失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务状态
     */
    @GetMapping("/job-status/{jobId}")
    @Operation(summary = "查询任务状态", description = "查询异步任务的执行状态")
    public ApiResponse<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        log.info("查询任务状态, jobId={}", jobId);
        
        try {
            GetJobStatusResponse response = scheduleEngineClient.getJobStatus(jobId);
            
            // 任务不存在
            if (response == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("jobId", jobId);
                result.put("exists", false);
                result.put("message", "任务不存在或已被清理，可能原因：1) 任务已完成并超过保留期限 2) 服务端重启导致内存数据丢失 3) 任务ID错误");
                return ApiResponse.success(result);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("jobId", response.getJobId());
            result.put("exists", true);
            result.put("status", response.getStatus());
            result.put("createdAt", response.getCreatedAt());
            result.put("updatedAt", response.getUpdatedAt());
            result.put("engineVersion", response.getEngineVersion());
            
            if (response.hasResult()) {
                result.put("hasResult", true);
                KpiSummary summary = response.getResult().getSummary();
                Map<String, Object> kpi = new HashMap<>();
                kpi.put("cost", summary.getCost());
                kpi.put("elapsedMs", summary.getElapsedMs());
                result.put("kpi", kpi);
            } else if (!response.getErrorMessage().isEmpty()) {
                result.put("errorMessage", response.getErrorMessage());
            }
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("查询任务状态失败, jobId={}", jobId, e);
            return ApiResponse.error(50002, "gRPC 调用失败: " + e.getMessage());
        }
    }

    /**
     * 列出所有任务
     */
    @GetMapping("/list-jobs")
    @Operation(summary = "列出所有任务", description = "列出所有异步任务")
    public ApiResponse<Map<String, Object>> listJobs(@RequestParam(defaultValue = "10") int limit) {
        log.info("列出任务列表, limit={}", limit);
        
        try {
            ListJobsResponse response = scheduleEngineClient.listJobs(limit);
            
            Map<String, Object> result = new HashMap<>();
            result.put("jobCount", response.getJobsCount());
            result.put("jobs", response.getJobsList());
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("列出任务失败", e);
            return ApiResponse.error(50002, "gRPC 调用失败: " + e.getMessage());
        }
    }
}

