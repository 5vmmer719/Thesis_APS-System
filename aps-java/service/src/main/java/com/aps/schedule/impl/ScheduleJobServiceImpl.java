package com.aps.schedule.impl;

import cn.hutool.core.bean.BeanUtil;

import com.aps.dto.request.schedule.ScheduleJobCreateRequest;
import com.aps.dto.request.schedule.ScheduleJobQueryRequest;
import com.aps.dto.response.schedule.ScheduleJobDTO;
import com.aps.dto.response.schedule.SchedulePlanDTO;
import com.aps.entity.order.ProductionOrder;
import com.aps.entity.schedule.SchJob;
import com.aps.entity.schedule.SchPlan;
import com.aps.exception.BusinessException;
import com.aps.grpc.proto.SolveResponse;
import com.aps.mapper.order.ProductionOrderMapper;
import com.aps.mapper.schedule.SchJobMapper;
import com.aps.mapper.schedule.SchPlanMapper;
import com.aps.response.PageResult;
import com.aps.schedule.ScheduleEngineService;
import com.aps.schedule.ScheduleJobService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排产任务服务实现
 *
 * @author APS System
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleJobServiceImpl implements ScheduleJobService {

    private final SchJobMapper schJobMapper;
    private final SchPlanMapper schPlanMapper;
    private final ProductionOrderMapper productionOrderMapper;
    private final ScheduleEngineService scheduleEngineService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createJob(ScheduleJobCreateRequest request) {
        log.info("创建排产任务, horizonStart={}, horizonEnd={}", request.getHorizonStart(), request.getHorizonEnd());

        // 1. 校验日期范围
        if (request.getHorizonStart().isAfter(request.getHorizonEnd())) {
            throw new BusinessException(40000, "排产起始日期不能晚于结束日期");
        }

        // 2. 校验订单状态（必须是已审批状态）
        if (request.getOrderIds() != null && !request.getOrderIds().isEmpty()) {
            List<ProductionOrder> orders = productionOrderMapper.selectBatchIds(request.getOrderIds());
            for (ProductionOrder order : orders) {
                if (!ProductionOrder.Status.APPROVED.getCode().equals(order.getStatus())) {
                    throw new BusinessException(40000, 
                            String.format("订单 %s 状态不是已审批，无法排产", order.getProdNo()));
                }
            }
        }

        // 3. 生成任务编号
        String jobNo = generateJobNo();

        // 4. 组装范围配置
        Map<String, Object> scopeJson = new HashMap<>();
        scopeJson.put("orderIds", request.getOrderIds());
        scopeJson.put("processTypes", request.getProcessTypes());
        scopeJson.put("lineScopes", request.getLineScopes());

        // 5. 组装目标权重配置
        Map<String, Object> objectiveJson = new HashMap<>();
        if (request.getObjective() != null) {
            objectiveJson.put("otdWeight", request.getObjective().getOtdWeight());
            objectiveJson.put("setupWeight", request.getObjective().getSetupWeight());
            objectiveJson.put("loadBalanceWeight", request.getObjective().getLoadBalanceWeight());
        } else {
            // 默认权重
            objectiveJson.put("otdWeight", 40);
            objectiveJson.put("setupWeight", 30);
            objectiveJson.put("loadBalanceWeight", 30);
        }

        // 6. 组装约束规则配置
        Map<String, Object> constraintJson = new HashMap<>();
        if (request.getConstraints() != null) {
            BeanUtil.copyProperties(request.getConstraints(), constraintJson);
        }

        // 7. 创建任务
        SchJob job = new SchJob();
        job.setJobNo(jobNo);
        job.setHorizonStart(request.getHorizonStart());
        job.setHorizonEnd(request.getHorizonEnd());
        job.setScopeJson(scopeJson);
        job.setObjectiveJson(objectiveJson);
        job.setConstraintJson(constraintJson);
        job.setStatus(0); // 待运行

        schJobMapper.insert(job);

        log.info("创建排产任务成功, jobId={}, jobNo={}", job.getId(), jobNo);
        return job.getId();
    }

    @Override
    public PageResult<ScheduleJobDTO> listJobs(ScheduleJobQueryRequest request) {
        log.info("查询排产任务列表, request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<SchJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchJob::getDeleted, 0);

        if (request.getJobNo() != null && !request.getJobNo().isEmpty()) {
            wrapper.like(SchJob::getJobNo, request.getJobNo());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SchJob::getStatus, request.getStatus());
        }
        if (request.getHorizonStartFrom() != null) {
            wrapper.ge(SchJob::getHorizonStart, request.getHorizonStartFrom());
        }
        if (request.getHorizonStartTo() != null) {
            wrapper.le(SchJob::getHorizonStart, request.getHorizonStartTo());
        }
        if (request.getCreatedBy() != null && !request.getCreatedBy().isEmpty()) {
            wrapper.eq(SchJob::getCreatedBy, request.getCreatedBy());
        }

        // 排序：创建时间降序
        wrapper.orderByDesc(SchJob::getCreatedTime);

        // 分页查询
        Page<SchJob> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<SchJob> result = schJobMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<ScheduleJobDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public ScheduleJobDTO getJobById(Long jobId) {
        log.info("获取排产任务详情, jobId={}", jobId);

        SchJob job = schJobMapper.selectById(jobId);
        if (job == null || job.getDeleted() == 1) {
            throw new BusinessException(40400, "排产任务不存在");
        }

        ScheduleJobDTO dto = convertToDTO(job);

        // 查询方案数量
        LambdaQueryWrapper<SchPlan> planWrapper = new LambdaQueryWrapper<>();
        planWrapper.eq(SchPlan::getJobId, jobId)
                .eq(SchPlan::getDeleted, 0);
        Long planCount = schPlanMapper.selectCount(planWrapper);
        dto.setPlanCount(planCount.intValue());

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void runJob(Long jobId) {
        log.info("运行排产任务, jobId={}", jobId);

        // 1. 查询任务
        SchJob job = schJobMapper.selectById(jobId);
        if (job == null || job.getDeleted() == 1) {
            throw new BusinessException(40400, "排产任务不存在");
        }

        // 2. 校验状态
        if (job.getStatus() != 0) {
            throw new BusinessException(40002, "任务状态不允许运行，当前状态: " + getStatusText(job.getStatus()));
        }

        // 3. 更新状态为运行中
        job.setStatus(1);
        schJobMapper.updateById(job);

        try {
            // 4. 调用排产引擎（同步模式，适合小规模任务）
            SolveResponse response = scheduleEngineService.solveSync(job);

            // 5. 处理结果
            Long planId = scheduleEngineService.processResult(job, response);

            // 6. 更新任务状态为成功
            job.setStatus(2);
            job.setErrorMsg(null);
            schJobMapper.updateById(job);

            log.info("排产任务执行成功, jobId={}, jobNo={}, planId={}", 
                    jobId, job.getJobNo(), planId);

        } catch (Exception e) {
            log.error("排产任务执行失败, jobId={}, error={}", jobId, e.getMessage(), e);

            // 更新任务状态为失败
            job.setStatus(3);
            job.setErrorMsg(e.getMessage());
            schJobMapper.updateById(job);

            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopJob(Long jobId) {
        log.info("停止排产任务, jobId={}", jobId);

        SchJob job = schJobMapper.selectById(jobId);
        if (job == null || job.getDeleted() == 1) {
            throw new BusinessException(40400, "排产任务不存在");
        }

        if (job.getStatus() != 1) {
            throw new BusinessException(40002, "任务未在运行中，无法停止");
        }

        // TODO: 通知引擎停止任务

        // 更新状态为失败
        job.setStatus(3);
        job.setErrorMsg("用户手动停止");
        schJobMapper.updateById(job);

        log.info("停止排产任务成功, jobId={}", jobId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Long jobId) {
        log.info("删除排产任务, jobId={}", jobId);

        SchJob job = schJobMapper.selectById(jobId);
        if (job == null || job.getDeleted() == 1) {
            throw new BusinessException(40400, "排产任务不存在");
        }

        // 校验是否有已发布的方案
        LambdaQueryWrapper<SchPlan> planWrapper = new LambdaQueryWrapper<>();
        planWrapper.eq(SchPlan::getJobId, jobId)
                .eq(SchPlan::getStatus, 1) // 已发布
                .eq(SchPlan::getDeleted, 0);
        Long publishedCount = schPlanMapper.selectCount(planWrapper);
        if (publishedCount > 0) {
            throw new BusinessException(40002, "存在已发布的方案，无法删除任务");
        }

        // 软删除
        job.setDeleted(1);
        schJobMapper.updateById(job);

        log.info("删除排产任务成功, jobId={}", jobId);
    }

    @Override
    public List<SchedulePlanDTO> getPlansByJobId(Long jobId) {
        log.info("获取任务的所有方案, jobId={}", jobId);

        LambdaQueryWrapper<SchPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchPlan::getJobId, jobId)
                .eq(SchPlan::getDeleted, 0)
                .orderByDesc(SchPlan::getIsBest)
                .orderByDesc(SchPlan::getCreatedTime);

        List<SchPlan> plans = schPlanMapper.selectList(wrapper);

        return plans.stream()
                .map(this::convertPlanToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 生成任务编号
     */
    private String generateJobNo() {
        return "JOB" + System.currentTimeMillis();
    }

    /**
     * 转换为DTO
     */
    private ScheduleJobDTO convertToDTO(SchJob job) {
        ScheduleJobDTO dto = new ScheduleJobDTO();
        BeanUtil.copyProperties(job, dto);
        dto.setStatusText(getStatusText(job.getStatus()));
        return dto;
    }

    /**
     * 转换方案为DTO
     */
    private SchedulePlanDTO convertPlanToDTO(SchPlan plan) {
        SchedulePlanDTO dto = new SchedulePlanDTO();
        BeanUtil.copyProperties(plan, dto);
        dto.setStatusText(getPlanStatusText(plan.getStatus()));
        return dto;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待运行";
            case 1: return "运行中";
            case 2: return "成功";
            case 3: return "失败";
            case 4: return "不可行";
            default: return "未知";
        }
    }

    /**
     * 获取方案状态文本
     */
    private String getPlanStatusText(Integer status) {
        switch (status) {
            case 0: return "草稿";
            case 1: return "已发布";
            case 2: return "已作废";
            default: return "未知";
        }
    }
}

