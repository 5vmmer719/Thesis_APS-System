package com.aps.schedule.impl;

import cn.hutool.core.bean.BeanUtil;

import com.aps.dto.request.schedule.PlanAdjustRequest;
import com.aps.dto.request.schedule.PublishPlanRequest;
import com.aps.dto.request.schedule.SchedulePlanQueryRequest;
import com.aps.dto.response.masterdata.GanttDataDTO;
import com.aps.dto.response.masterdata.PlanBucketDTO;
import com.aps.dto.response.masterdata.PlanConflictDTO;
import com.aps.dto.response.schedule.SchedulePlanDTO;
import com.aps.entity.schedule.*;
import com.aps.exception.BusinessException;
import com.aps.mapper.schedule.*;
import com.aps.response.PageResult;
import com.aps.schedule.SchedulePlanService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排产方案服务实现
 *
 * @author APS System
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulePlanServiceImpl implements SchedulePlanService {

    private final SchPlanMapper schPlanMapper;
    private final SchPlanBucketMapper schPlanBucketMapper;
    private final SchPlanConflictMapper schPlanConflictMapper;
    private final SchPlanStatMapper schPlanStatMapper;
    private final SchManualAdjustLogMapper schManualAdjustLogMapper;
    private final SchJobMapper schJobMapper;

    @Override
    public PageResult<SchedulePlanDTO> listPlans(SchedulePlanQueryRequest request) {
        log.info("查询排产方案列表, request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<SchPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchPlan::getDeleted, 0);

        if (request.getJobId() != null) {
            wrapper.eq(SchPlan::getJobId, request.getJobId());
        }
        if (request.getPlanNo() != null && !request.getPlanNo().isEmpty()) {
            wrapper.like(SchPlan::getPlanNo, request.getPlanNo());
        }
        if (request.getIsBest() != null) {
            wrapper.eq(SchPlan::getIsBest, request.getIsBest());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SchPlan::getStatus, request.getStatus());
        }

        // 排序：最优方案优先，创建时间降序
        wrapper.orderByDesc(SchPlan::getIsBest)
                .orderByDesc(SchPlan::getCreatedTime);

        // 分页查询
        Page<SchPlan> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<SchPlan> result = schPlanMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<SchedulePlanDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public SchedulePlanDTO getPlanById(Long planId) {
        log.info("获取排产方案详情, planId={}", planId);

        SchPlan plan = schPlanMapper.selectById(planId);
        if (plan == null || plan.getDeleted() == 1) {
            throw new BusinessException(40400, "排产方案不存在");
        }

        SchedulePlanDTO dto = convertToDTO(plan);

        // 查询统计信息
        SchPlanStat stat = schPlanStatMapper.selectOne(
                new LambdaQueryWrapper<SchPlanStat>()
                        .eq(SchPlanStat::getPlanId, planId)
        );
        if (stat != null) {
            SchedulePlanDTO.PlanStatDTO statDTO = new SchedulePlanDTO.PlanStatDTO();
            statDTO.setOtdRate(stat.getOtdRate().doubleValue());
            statDTO.setSetupTimes(stat.getSetupTimes());
            statDTO.setAvgLineLoad(stat.getAvgLineLoad().doubleValue());
            dto.setStat(statDTO);
        }

        // 查询冲突列表
        List<SchPlanConflict> conflicts = schPlanConflictMapper.selectList(
                new LambdaQueryWrapper<SchPlanConflict>()
                        .eq(SchPlanConflict::getPlanId, planId)
                        .orderByDesc(SchPlanConflict::getLevel)
        );
        if (!conflicts.isEmpty()) {
            List<SchedulePlanDTO.PlanConflictDTO> conflictDTOs = conflicts.stream()
                    .map(this::convertConflictToInnerDTO)
                    .collect(Collectors.toList());
            dto.setConflicts(conflictDTOs);
        }

        // 查询桶数量
        Long bucketCount = schPlanBucketMapper.selectCount(
                new LambdaQueryWrapper<SchPlanBucket>()
                        .eq(SchPlanBucket::getPlanId, planId)
        );
        dto.setBucketCount(bucketCount.intValue());

        return dto;
    }

    @Override
    public List<PlanBucketDTO> getBucketsByPlanId(Long planId) {
        log.info("获取方案的班次桶列表, planId={}", planId);

        LambdaQueryWrapper<SchPlanBucket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchPlanBucket::getPlanId, planId)
                .orderByAsc(SchPlanBucket::getBizDate)
                .orderByAsc(SchPlanBucket::getShiftCode)
                .orderByAsc(SchPlanBucket::getSeqNo);

        List<SchPlanBucket> buckets = schPlanBucketMapper.selectList(wrapper);

        return buckets.stream()
                .map(this::convertBucketToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GanttDataDTO getGanttData(Long planId) {
        log.info("获取方案的甘特图数据, planId={}", planId);

        // 查询方案
        SchPlan plan = schPlanMapper.selectById(planId);
        if (plan == null || plan.getDeleted() == 1) {
            throw new BusinessException(40400, "排产方案不存在");
        }

        // 查询所有桶
        List<SchPlanBucket> buckets = schPlanBucketMapper.selectList(
                new LambdaQueryWrapper<SchPlanBucket>()
                        .eq(SchPlanBucket::getPlanId, planId)
                        .orderByAsc(SchPlanBucket::getBizDate)
                        .orderByAsc(SchPlanBucket::getShiftCode)
                        .orderByAsc(SchPlanBucket::getSeqNo)
        );

        // 构建甘特图数据
        GanttDataDTO ganttData = new GanttDataDTO();

        // 1. 提取产线列表（去重）
        Map<Long, GanttDataDTO.LineInfo> lineMap = new LinkedHashMap<>();
        for (SchPlanBucket bucket : buckets) {
            if (!lineMap.containsKey(bucket.getLineId())) {
                GanttDataDTO.LineInfo lineInfo = new GanttDataDTO.LineInfo();
                lineInfo.setLineId(bucket.getLineId());
                lineInfo.setLineCode("LINE-" + bucket.getLineId()); // TODO: 从产线表查询
                lineInfo.setLineName("产线" + bucket.getLineId());
                lineInfo.setProcessType(bucket.getProcessType());
                lineMap.put(bucket.getLineId(), lineInfo);
            }
        }
        ganttData.setLines(new ArrayList<>(lineMap.values()));

        // 2. 提取时间轴（日期列表，去重排序）
        Set<LocalDate> dateSet = buckets.stream()
                .map(SchPlanBucket::getBizDate)
                .collect(Collectors.toCollection(TreeSet::new));
        ganttData.setTimeline(new ArrayList<>(dateSet));

        // 3. 构建甘特图行数据
        Map<String, GanttDataDTO.GanttRow> rowMap = new LinkedHashMap<>();
        for (SchPlanBucket bucket : buckets) {
            String rowKey = bucket.getLineId() + "_" + bucket.getBizDate() + "_" + bucket.getShiftCode();
            
            GanttDataDTO.GanttRow row = rowMap.computeIfAbsent(rowKey, k -> {
                GanttDataDTO.GanttRow newRow = new GanttDataDTO.GanttRow();
                newRow.setLineId(bucket.getLineId());
                newRow.setDate(bucket.getBizDate());
                newRow.setShiftCode(bucket.getShiftCode());
                newRow.setTasks(new ArrayList<>());
                return newRow;
            });

            // 添加任务
            GanttDataDTO.GanttTask task = new GanttDataDTO.GanttTask();
            task.setBucketId(bucket.getId());
            task.setProdOrderId(bucket.getProdOrderId());
            // TODO: 从订单表关联查询
            task.setProdNo("ORDER-" + bucket.getProdOrderId());
            task.setModelCode("MODEL-" + bucket.getProdOrderId());
            task.setModelName("车型" + bucket.getProdOrderId());
            task.setQty(bucket.getQty());
            task.setSeqNo(bucket.getSeqNo());
            task.setHasSetup(bucket.getSetupMinutes() != null && bucket.getSetupMinutes() > 0);
            task.setSetupMinutes(bucket.getSetupMinutes());

            row.getTasks().add(task);
        }
        ganttData.setRows(new ArrayList<>(rowMap.values()));

        return ganttData;
    }

    @Override
    public List<PlanConflictDTO> getConflictsByPlanId(Long planId) {
        log.info("获取方案的冲突列表, planId={}", planId);

        List<SchPlanConflict> conflicts = schPlanConflictMapper.selectList(
                new LambdaQueryWrapper<SchPlanConflict>()
                        .eq(SchPlanConflict::getPlanId, planId)
                        .orderByDesc(SchPlanConflict::getLevel)
                        .orderByDesc(SchPlanConflict::getCreatedTime)
        );

        return conflicts.stream()
                .map(this::convertConflictToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishPlan(PublishPlanRequest request) {
        log.info("发布排产方案, planId={}", request.getPlanId());

        // 1. 查询方案
        SchPlan plan = schPlanMapper.selectById(request.getPlanId());
        if (plan == null || plan.getDeleted() == 1) {
            throw new BusinessException(40400, "排产方案不存在");
        }

        // 2. 校验状态
        if (plan.getStatus() != 0) {
            throw new BusinessException(40002, "方案状态不允许发布，当前状态: " + getPlanStatusText(plan.getStatus()));
        }

        // 3. 检查是否有致命冲突
        Long fatalConflictCount = schPlanConflictMapper.selectCount(
                new LambdaQueryWrapper<SchPlanConflict>()
                        .eq(SchPlanConflict::getPlanId, request.getPlanId())
                        .eq(SchPlanConflict::getLevel, 3) // 致命冲突
        );
        if (fatalConflictCount > 0) {
            throw new BusinessException(40002, "存在致命冲突，无法发布方案");
        }

        // 4. 更新方案状态
        plan.setStatus(1); // 已发布
        if (request.getRemark() != null) {
            plan.setRemark(request.getRemark());
        }
        schPlanMapper.updateById(plan);

        // TODO: 5. 生成工单（如果 generateWorkOrder = true）
        if (Boolean.TRUE.equals(request.getGenerateWorkOrder())) {
            generateWorkOrders(request.getPlanId());
        }

        // TODO: 6. 更新订单状态为"已排产"

        log.info("发布排产方案成功, planId={}", request.getPlanId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustPlan(PlanAdjustRequest request) {
        log.info("手动调整排产方案, planId={}, changeCount={}", request.getPlanId(), request.getChanges().size());

        // 1. 查询方案
        SchPlan plan = schPlanMapper.selectById(request.getPlanId());
        if (plan == null || plan.getDeleted() == 1) {
            throw new BusinessException(40400, "排产方案不存在");
        }

        // 2. 校验状态（草稿状态才能调整）
        if (plan.getStatus() != 0) {
            throw new BusinessException(40002, "方案状态不允许调整，当前状态: " + getPlanStatusText(plan.getStatus()));
        }

        // 3. 执行调整变更
        for (PlanAdjustRequest.PlanAdjustChange change : request.getChanges()) {
            applyChange(change);
        }

        // 4. 记录调整日志
        SchManualAdjustLog log = new SchManualAdjustLog();
        log.setPlanId(request.getPlanId());
        log.setUserId(1L); // TODO: 从上下文获取当前用户ID
        Map<String, Object> changeJson = new HashMap<>();
        changeJson.put("changes", request.getChanges());
        log.setChangeJson(changeJson);
        log.setRemark(request.getRemark());
        schManualAdjustLogMapper.insert(log);

        // TODO: 5. 重新计算KPI和冲突

        this.log.info("手动调整排产方案成功, planId={}", request.getPlanId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void discardPlan(Long planId) {
        log.info("作废排产方案, planId={}", planId);

        SchPlan plan = schPlanMapper.selectById(planId);
        if (plan == null || plan.getDeleted() == 1) {
            throw new BusinessException(40400, "排产方案不存在");
        }

        // 更新状态
        plan.setStatus(2); // 已作废
        schPlanMapper.updateById(plan);

        log.info("作废排产方案成功, planId={}", planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyPlan(Long planId) {
        log.info("复制排产方案, planId={}", planId);

        // 1. 查询原方案
        SchPlan sourcePlan = schPlanMapper.selectById(planId);
        if (sourcePlan == null || sourcePlan.getDeleted() == 1) {
            throw new BusinessException(40400, "排产方案不存在");
        }

        // 2. 创建新方案
        SchPlan newPlan = new SchPlan();
        BeanUtil.copyProperties(sourcePlan, newPlan);
        newPlan.setId(null);
        newPlan.setPlanNo(generatePlanNo());
        newPlan.setIsBest(0); // 新方案不是最优
        newPlan.setStatus(0); // 草稿
        newPlan.setRemark("复制自: " + sourcePlan.getPlanNo());
        schPlanMapper.insert(newPlan);

        // 3. 复制桶
        List<SchPlanBucket> sourceBuckets = schPlanBucketMapper.selectList(
                new LambdaQueryWrapper<SchPlanBucket>()
                        .eq(SchPlanBucket::getPlanId, planId)
        );
        for (SchPlanBucket sourceBucket : sourceBuckets) {
            SchPlanBucket newBucket = new SchPlanBucket();
            BeanUtil.copyProperties(sourceBucket, newBucket);
            newBucket.setId(null);
            newBucket.setPlanId(newPlan.getId());
            schPlanBucketMapper.insert(newBucket);
        }

        // 4. 复制统计（如果有）
        SchPlanStat sourceStat = schPlanStatMapper.selectOne(
                new LambdaQueryWrapper<SchPlanStat>()
                        .eq(SchPlanStat::getPlanId, planId)
        );
        if (sourceStat != null) {
            SchPlanStat newStat = new SchPlanStat();
            BeanUtil.copyProperties(sourceStat, newStat);
            newStat.setId(null);
            newStat.setPlanId(newPlan.getId());
            schPlanStatMapper.insert(newStat);
        }

        log.info("复制排产方案成功, sourcePlanId={}, newPlanId={}", planId, newPlan.getId());
        return newPlan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setBestPlan(Long planId) {
        log.info("设置最优方案, planId={}", planId);

        // 1. 查询方案
        SchPlan plan = schPlanMapper.selectById(planId);
        if (plan == null || plan.getDeleted() == 1) {
            throw new BusinessException(40400, "排产方案不存在");
        }

        // 2. 取消同任务下其他方案的最优标记
        SchPlan updatePlan = new SchPlan();
        updatePlan.setIsBest(0);
        schPlanMapper.update(updatePlan,
                new LambdaQueryWrapper<SchPlan>()
                        .eq(SchPlan::getJobId, plan.getJobId())
                        .eq(SchPlan::getDeleted, 0)
        );

        // 3. 设置当前方案为最优
        plan.setIsBest(1);
        schPlanMapper.updateById(plan);

        log.info("设置最优方案成功, planId={}", planId);
    }

    /**
     * 生成方案编号
     */
    private String generatePlanNo() {
        return "PLAN" + System.currentTimeMillis();
    }

    /**
     * 生成工单
     */
    private void generateWorkOrders(Long planId) {
        log.info("生成工单, planId={}", planId);
        // TODO: 根据桶生成四大工艺工单
        // 1. 查询所有桶
        // 2. 按工艺类型分组
        // 3. 为每个桶创建工单记录
    }

    /**
     * 应用调整变更
     */
    private void applyChange(PlanAdjustRequest.PlanAdjustChange change) {
        switch (change.getChangeType()) {
            case "MOVE":
                // TODO: 移动桶（改变日期/班次/顺序）
                break;
            case "SWAP":
                // TODO: 交换两个桶的位置
                break;
            case "DELETE":
                // TODO: 删除桶
                break;
            case "INSERT":
                // TODO: 插入新桶
                break;
            default:
                throw new BusinessException(40000, "不支持的变更类型: " + change.getChangeType());
        }
    }

    /**
     * 转换为DTO
     */
    private SchedulePlanDTO convertToDTO(SchPlan plan) {
        SchedulePlanDTO dto = new SchedulePlanDTO();
        BeanUtil.copyProperties(plan, dto);
        dto.setStatusText(getPlanStatusText(plan.getStatus()));
        return dto;
    }

    /**
     * 转换桶为DTO
     */
    private PlanBucketDTO convertBucketToDTO(SchPlanBucket bucket) {
        PlanBucketDTO dto = new PlanBucketDTO();
        BeanUtil.copyProperties(bucket, dto);
        dto.setProcessTypeText(getProcessTypeText(bucket.getProcessType()));
        // TODO: 关联查询产线、订单、车型信息
        return dto;
    }

    /**
     * 转换冲突为DTO
     */
    private PlanConflictDTO convertConflictToDTO(SchPlanConflict conflict) {
        PlanConflictDTO dto = new PlanConflictDTO();
        BeanUtil.copyProperties(conflict, dto);
        dto.setLevelText(getConflictLevelText(conflict.getLevel()));
        dto.setConflictTypeText(conflict.getConflictType());
        return dto;
    }

    /**
     * 转换冲突为内部DTO
     */
    private SchedulePlanDTO.PlanConflictDTO convertConflictToInnerDTO(SchPlanConflict conflict) {
        SchedulePlanDTO.PlanConflictDTO dto = new SchedulePlanDTO.PlanConflictDTO();
        dto.setConflictType(conflict.getConflictType());
        dto.setLevel(conflict.getLevel());
        dto.setLevelText(getConflictLevelText(conflict.getLevel()));
        dto.setMessage(conflict.getMessage());
        dto.setObjectType(conflict.getObjectType());
        dto.setObjectId(conflict.getObjectId());
        return dto;
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

    /**
     * 获取工艺类型文本
     */
    private String getProcessTypeText(Integer processType) {
        switch (processType) {
            case 1: return "冲压";
            case 2: return "焊装";
            case 3: return "涂装";
            case 4: return "总装";
            default: return "未知";
        }
    }

    /**
     * 获取冲突级别文本
     */
    private String getConflictLevelText(Integer level) {
        switch (level) {
            case 1: return "提示";
            case 2: return "警告";
            case 3: return "致命";
            default: return "未知";
        }
    }
}

