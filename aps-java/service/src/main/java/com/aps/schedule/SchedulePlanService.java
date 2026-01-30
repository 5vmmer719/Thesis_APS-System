package com.aps.schedule;


import com.aps.dto.request.schedule.PlanAdjustRequest;
import com.aps.dto.request.schedule.PublishPlanRequest;
import com.aps.dto.request.schedule.SchedulePlanQueryRequest;
import com.aps.dto.response.masterdata.GanttDataDTO;
import com.aps.dto.response.masterdata.PlanBucketDTO;
import com.aps.dto.response.masterdata.PlanConflictDTO;
import com.aps.dto.response.schedule.SchedulePlanDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 排产方案服务接口
 *
 * @author APS System
 * @since 2024-01-01
 */
public interface SchedulePlanService {

    /**
     * 分页查询排产方案
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<SchedulePlanDTO> listPlans(SchedulePlanQueryRequest request);

    /**
     * 获取排产方案详情
     *
     * @param planId 方案ID
     * @return 方案详情
     */
    SchedulePlanDTO getPlanById(Long planId);

    /**
     * 获取方案的班次桶列表
     *
     * @param planId 方案ID
     * @return 班次桶列表
     */
    List<PlanBucketDTO> getBucketsByPlanId(Long planId);

    /**
     * 获取方案的甘特图数据
     *
     * @param planId 方案ID
     * @return 甘特图数据
     */
    GanttDataDTO getGanttData(Long planId);

    /**
     * 获取方案的冲突列表
     *
     * @param planId 方案ID
     * @return 冲突列表
     */
    List<PlanConflictDTO> getConflictsByPlanId(Long planId);

    /**
     * 发布方案（生成工单）
     *
     * @param request 发布请求
     */
    void publishPlan(PublishPlanRequest request);

    /**
     * 手动调整方案
     *
     * @param request 调整请求
     */
    void adjustPlan(PlanAdjustRequest request);

    /**
     * 作废方案
     *
     * @param planId 方案ID
     */
    void discardPlan(Long planId);

    /**
     * 复制方案
     *
     * @param planId 方案ID
     * @return 新方案ID
     */
    Long copyPlan(Long planId);

    /**
     * 设置为最优方案
     *
     * @param planId 方案ID
     */
    void setBestPlan(Long planId);
}

