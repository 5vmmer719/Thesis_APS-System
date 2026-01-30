package com.aps.schedule;


import com.aps.entity.schedule.SchJob;
import com.aps.grpc.proto.SolveRequest;
import com.aps.grpc.proto.SolveResponse;

/**
 * 排产引擎服务接口
 * 负责与 gRPC 排产引擎交互
 *
 * @author APS System
 * @since 2024-01-01
 */
public interface ScheduleEngineService {

    /**
     * 构建排产请求
     *
     * @param job 排产任务
     * @return gRPC 请求对象
     */
    SolveRequest buildSolveRequest(SchJob job);

    /**
     * 同步执行排产
     *
     * @param job 排产任务
     * @return 排产结果
     */
    SolveResponse solveSync(SchJob job);

    /**
     * 异步提交排产任务
     *
     * @param job 排产任务
     * @return 引擎任务ID
     */
    String submitJobAsync(SchJob job);

    /**
     * 查询异步任务状态
     *
     * @param engineJobId 引擎任务ID
     * @return 任务状态: QUEUED/RUNNING/COMPLETED/FAILED
     */
    String getJobStatus(String engineJobId);

    /**
     * 获取异步任务结果
     *
     * @param engineJobId 引擎任务ID
     * @return 排产结果（仅COMPLETED状态可用）
     */
    SolveResponse getJobResult(String engineJobId);

    /**
     * 处理排产结果，保存到数据库
     *
     * @param job 排产任务
     * @param response 排产结果
     * @return 生成的方案ID
     */
    Long processResult(SchJob job, SolveResponse response);
}

