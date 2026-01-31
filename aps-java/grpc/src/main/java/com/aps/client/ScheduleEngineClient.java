package com.aps.client;

import com.aps.config.GrpcConfig;
import com.aps.grpc.proto.ApsServiceGrpc.ApsServiceStub;
import com.aps.grpc.proto.ApsServiceGrpc.ApsServiceBlockingStub;
import com.aps.grpc.proto.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 排产引擎 gRPC 客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleEngineClient {

    private final ApsServiceBlockingStub blockingStub;
    private final ApsServiceStub asyncStub;
    private final GrpcConfig grpcConfig;


    /**
     * 同步求解
     *
     * @param request 求解请求
     * @return 求解响应
     * @throws StatusRuntimeException gRPC 调用异常
     */
    public SolveResponse solve(SolveRequest request) {
        log.info("调用 gRPC Solve 接口, requestId={}, jobCount={}", 
                request.getRequestId(), request.getJobsCount());
        
        try {
            long startTime = System.currentTimeMillis();
            // 为每次调用动态设置 deadline
            SolveResponse response = blockingStub
                    .withDeadlineAfter(grpcConfig.getTimeoutSeconds(), TimeUnit.SECONDS)
                    .solve(request);
            long elapsed = System.currentTimeMillis() - startTime;
            
            log.info("gRPC Solve 调用成功, requestId={}, 耗时={}ms, cost={}", 
                    response.getRequestId(), elapsed, 
                    response.hasSummary() ? response.getSummary().getCost() : 0);
            
            return response;
        } catch (StatusRuntimeException e) {
            log.error("gRPC Solve 调用失败, requestId={}, status={}, message={}", 
                    request.getRequestId(), e.getStatus(), e.getMessage());
            throw e;
        }
    }

    /**
     * 提交异步任务
     *
     * @param request 求解请求
     * @return 任务提交响应
     * @throws StatusRuntimeException gRPC 调用异常
     */
    public SubmitJobResponse submitJob(SolveRequest request) {
        log.info("调用 gRPC SubmitJob 接口, requestId={}, jobCount={}", 
                request.getRequestId(), request.getJobsCount());
        
        try {
            SubmitJobRequest submitRequest = SubmitJobRequest.newBuilder()
                    .setRequest(request)
                    .build();
            
            // 为每次调用动态设置 deadline
            SubmitJobResponse response = blockingStub
                    .withDeadlineAfter(grpcConfig.getTimeoutSeconds(), TimeUnit.SECONDS)
                    .submitJob(submitRequest);
            
            log.info("gRPC SubmitJob 调用成功, jobId={}, message={}", 
                    response.getJobId(), response.getMessage());
            
            return response;
        } catch (StatusRuntimeException e) {
            log.error("gRPC SubmitJob 调用失败, requestId={}, status={}, message={}", 
                    request.getRequestId(), e.getStatus(), e.getMessage());
            throw e;
        }
    }

    /**
     * 查询任务状态
     *
     * @param jobId 任务ID
     * @return 任务状态响应,如果任务不存在返回 null
     * @throws StatusRuntimeException gRPC 调用异常(NOT_FOUND 除外)
     */
    public GetJobStatusResponse getJobStatus(String jobId) {
        log.debug("调用 gRPC GetJobStatus 接口, jobId={}", jobId);
        
        try {
            GetJobStatusRequest request = GetJobStatusRequest.newBuilder()
                    .setJobId(jobId)
                    .build();
            
            // 为每次调用动态设置 deadline
            GetJobStatusResponse response = blockingStub
                    .withDeadlineAfter(grpcConfig.getTimeoutSeconds(), TimeUnit.SECONDS)
                    .getJobStatus(request);
            
            log.debug("gRPC GetJobStatus 调用成功, jobId={}, status={}", 
                    jobId, response.getStatus());
            
            return response;
        } catch (StatusRuntimeException e) {
            // 如果是 NOT_FOUND,记录警告并返回 null,不抛出异常
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                log.warn("任务不存在或已被清理, jobId={}, message={}", jobId, e.getStatus().getDescription());
                return null;
            }
            
            // 其他错误继续抛出
            log.error("gRPC GetJobStatus 调用失败, jobId={}, status={}, message={}", 
                    jobId, e.getStatus(), e.getMessage());
            throw e;
        }
    }

    /**
     * 列出所有任务
     *
     * @param limit 返回数量限制(0表示不限制)
     * @return 任务列表响应
     * @throws StatusRuntimeException gRPC 调用异常
     */
    public ListJobsResponse listJobs(int limit) {
        log.debug("调用 gRPC ListJobs 接口, limit={}", limit);
        
        try {
            ListJobsRequest request = ListJobsRequest.newBuilder()
                    .setLimit(limit)
                    .build();
            
            // 为每次调用动态设置 deadline
            ListJobsResponse response = blockingStub
                    .withDeadlineAfter(grpcConfig.getTimeoutSeconds(), TimeUnit.SECONDS)
                    .listJobs(request);
            
            log.debug("gRPC ListJobs 调用成功, jobCount={}", response.getJobsCount());
            
            return response;
        } catch (StatusRuntimeException e) {
            log.error("gRPC ListJobs 调用失败, status={}, message={}", 
                    e.getStatus(), e.getMessage());
            throw e;
        }
    }

    /**
     * 健康检查（通过 ListJobs 接口）
     *
     * @return 是否健康
     */
    public boolean healthCheck() {
        try {
            listJobs(1);
            return true;
        } catch (Exception e) {
            log.warn("gRPC 健康检查失败: {}", e.getMessage());
            return false;
        }
    }
}