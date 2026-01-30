package com.aps.schedule;



import com.aps.dto.request.schedule.ScheduleJobCreateRequest;
import com.aps.dto.request.schedule.ScheduleJobQueryRequest;
import com.aps.dto.response.schedule.ScheduleJobDTO;
import com.aps.dto.response.schedule.SchedulePlanDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 排产任务服务接口
 *
 * @author APS System
 * @since 2024-01-01
 */
public interface ScheduleJobService {

    /**
     * 创建排产任务
     *
     * @param request 创建请求
     * @return 任务ID
     */
    Long createJob(ScheduleJobCreateRequest request);

    /**
     * 分页查询排产任务
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<ScheduleJobDTO> listJobs(ScheduleJobQueryRequest request);

    /**
     * 获取排产任务详情
     *
     * @param jobId 任务ID
     * @return 任务详情
     */
    ScheduleJobDTO getJobById(Long jobId);

    /**
     * 运行排产任务（调用排产引擎）
     *
     * @param jobId 任务ID
     */
    void runJob(Long jobId);

    /**
     * 停止排产任务
     *
     * @param jobId 任务ID
     */
    void stopJob(Long jobId);

    /**
     * 删除排产任务（软删除）
     *
     * @param jobId 任务ID
     */
    void deleteJob(Long jobId);

    /**
     * 获取任务的所有方案
     *
     * @param jobId 任务ID
     * @return 方案列表
     */
    List<SchedulePlanDTO> getPlansByJobId(Long jobId);
}

