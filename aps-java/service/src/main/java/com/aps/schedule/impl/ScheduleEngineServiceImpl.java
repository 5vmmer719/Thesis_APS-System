package com.aps.schedule.impl;

import cn.hutool.core.bean.BeanUtil;

import com.aps.client.ScheduleEngineClient;
import com.aps.entity.masterdata.Model;
import com.aps.entity.order.ProductionOrder;
import com.aps.entity.order.ProductionOrderAttr;
import com.aps.entity.schedule.*;
import com.aps.exception.BusinessException;
import com.aps.grpc.proto.*;
import com.aps.mapper.masterdata.LineMapper;
import com.aps.mapper.masterdata.ModelMapper;
import com.aps.mapper.order.ProductionOrderAttrMapper;
import com.aps.mapper.order.ProductionOrderMapper;
import com.aps.mapper.schedule.*;
import com.aps.schedule.ScheduleEngineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排产引擎服务实现
 *
 * @author APS System
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleEngineServiceImpl implements ScheduleEngineService {

    private final ScheduleEngineClient engineClient;
    private final SchJobMapper schJobMapper;
    private final SchPlanMapper schPlanMapper;
    private final SchPlanBucketMapper schPlanBucketMapper;
    private final SchPlanConflictMapper schPlanConflictMapper;
    private final SchPlanStatMapper schPlanStatMapper;
    private final SchEngineResultRawMapper schEngineResultRawMapper;
    private final ProductionOrderMapper productionOrderMapper;
    private final ProductionOrderAttrMapper productionOrderAttrMapper;
    private final ModelMapper modelMapper;
    private final LineMapper lineMapper;

    @Override
    public SolveRequest buildSolveRequest(SchJob job) {
        log.info("构建排产请求, jobId={}, jobNo={}", job.getId(), job.getJobNo());

        // 1. 生成请求ID
        String requestId = "REQ-" + job.getJobNo() + "-" + System.currentTimeMillis();

        // 2. 计算排产起始时间（毫秒时间戳）
        long planStartEpochMs = job.getHorizonStart()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        // 3. 查询待排产订单
        List<Long> orderIds = getOrderIdsFromScope(job);
        if (orderIds.isEmpty()) {
            throw new BusinessException(40000, "没有找到符合条件的待排产订单");
        }

        List<ProductionOrder> orders = productionOrderMapper.selectBatchIds(orderIds);
        if (orders.isEmpty()) {
            throw new BusinessException(40000, "订单数据不存在");
        }

        // 4. 转换为 gRPC Job 对象
        List<Job> grpcJobs = orders.stream()
                .map(order -> convertToGrpcJob(order, true))
                .collect(Collectors.toList());

        // 5. 构建求解参数
        SolveParams params = buildSolveParams(job);

        // 6. 构建请求
        return SolveRequest.newBuilder()
                .setRequestId(requestId)
                .setPlanStartEpochMs(planStartEpochMs)
                .addAllJobs(grpcJobs)
                .setParams(params)
                .build();
    }

    @Override
    public SolveResponse solveSync(SchJob job) {
        log.info("同步执行排产, jobId={}, jobNo={}", job.getId(), job.getJobNo());

        try {
            SolveRequest request = buildSolveRequest(job);
            SolveResponse response = engineClient.solve(request);

            log.info("排产完成, jobId={}, cost={}, elapsed={}ms",
                    job.getId(),
                    response.hasSummary() ? response.getSummary().getCost() : 0,
                    response.hasSummary() ? response.getSummary().getElapsedMs() : 0);

            return response;
        } catch (Exception e) {
            log.error("同步排产失败, jobId={}, error={}", job.getId(), e.getMessage(), e);
            throw new BusinessException(50000, "排产引擎调用失败: " + e.getMessage());
        }
    }

    @Override
    public String submitJobAsync(SchJob job) {
        log.info("异步提交排产任务, jobId={}, jobNo={}", job.getId(), job.getJobNo());

        try {
            SolveRequest request = buildSolveRequest(job);
            SubmitJobResponse response = engineClient.submitJob(request);

            log.info("异步任务提交成功, jobId={}, engineJobId={}", 
                    job.getId(), response.getJobId());

            return response.getJobId();
        } catch (Exception e) {
            log.error("异步任务提交失败, jobId={}, error={}", job.getId(), e.getMessage(), e);
            throw new BusinessException(50000, "排产引擎调用失败: " + e.getMessage());
        }
    }

    @Override
    public String getJobStatus(String engineJobId) {
        try {
            GetJobStatusResponse response = engineClient.getJobStatus(engineJobId);
            
            // 任务不存在
            if (response == null) {
                log.warn("任务不存在或已被清理, engineJobId={}", engineJobId);
                throw new BusinessException(40404, "任务不存在或已被清理，可能已完成并超过保留期限");
            }
            
            return response.getStatus();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询任务状态失败, engineJobId={}, error={}", engineJobId, e.getMessage());
            throw new BusinessException(50000, "查询任务状态失败: " + e.getMessage());
        }
    }

    @Override
    public SolveResponse getJobResult(String engineJobId) {
        try {
            GetJobStatusResponse response = engineClient.getJobStatus(engineJobId);
            
            // 任务不存在
            if (response == null) {
                log.warn("任务不存在或已被清理, engineJobId={}", engineJobId);
                throw new BusinessException(40404, "任务不存在或已被清理，可能已完成并超过保留期限");
            }
            
            if (!"COMPLETED".equals(response.getStatus())) {
                throw new BusinessException(40002, "任务未完成，当前状态: " + response.getStatus());
            }

            if (!response.hasResult()) {
                throw new BusinessException(50000, "任务结果为空");
            }

            return response.getResult();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取任务结果失败, engineJobId={}, error={}", engineJobId, e.getMessage());
            throw new BusinessException(50000, "获取任务结果失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long processResult(SchJob job, SolveResponse response) {
        log.info("处理排产结果, jobId={}, requestId={}", job.getId(), response.getRequestId());

        try {
            // 1. 保存原始结果
            saveRawResult(job, response);

            // 2. 构建 VIN 到订单ID的映射
            Map<String, Long> vinToOrderIdMap = buildVinToOrderIdMap(job);

            // 3. 创建方案
            Long planId = createPlan(job, response);

            // 4. 创建班次桶
            createBuckets(planId, response, vinToOrderIdMap);

            // 5. 创建冲突记录
            createConflicts(planId, response);

            // 6. 创建统计记录
            createStats(planId, response);

            log.info("排产结果处理完成, jobId={}, planId={}", job.getId(), planId);
            return planId;

        } catch (Exception e) {
            log.error("处理排产结果失败, jobId={}, error={}", job.getId(), e.getMessage(), e);
            throw new BusinessException(50000, "处理排产结果失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 从范围配置中获取订单ID列表
     */
    @SuppressWarnings("unchecked")
    private List<Long> getOrderIdsFromScope(SchJob job) {
        Map<String, Object> scopeJson = job.getScopeJson();
        if (scopeJson == null || scopeJson.isEmpty()) {
            // 如果没有指定范围，查询所有已审批订单
            LambdaQueryWrapper<ProductionOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductionOrder::getStatus, ProductionOrder.Status.APPROVED.getCode())
                    .eq(ProductionOrder::getDeleted, 0);
            return productionOrderMapper.selectList(wrapper).stream()
                    .map(ProductionOrder::getId)
                    .collect(Collectors.toList());
        }

        Object orderIdsObj = scopeJson.get("orderIds");
        if (orderIdsObj instanceof List) {
            return ((List<?>) orderIdsObj).stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * 构建 VIN (prodNo) 到订单ID的映射
     */
    private Map<String, Long> buildVinToOrderIdMap(SchJob job) {
        List<Long> orderIds = getOrderIdsFromScope(job);
        if (orderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ProductionOrder> orders = productionOrderMapper.selectBatchIds(orderIds);
        return orders.stream()
                .filter(order -> order.getProdNo() != null && !order.getProdNo().isEmpty())
                .collect(Collectors.toMap(
                        ProductionOrder::getProdNo,
                        ProductionOrder::getId,
                        (existing, replacement) -> existing // 如果有重复的 prodNo，保留第一个
                ));
    }

    /**
     * 转换订单为 gRPC Job 对象
     * 
     * @param order 生产订单
     * @param loadAttrs 是否加载订单属性（如颜色、模具等）
     * @return gRPC Job 对象
     */
    private Job convertToGrpcJob(ProductionOrder order, boolean loadAttrs) {
        // 查询车型信息
        Model model = modelMapper.selectById(order.getModelId());
        if (model == null) {
            throw new BusinessException(40000, "车型不存在: " + order.getModelId());
        }

        // 计算交期时间戳
        long dueEpochMs = order.getDueDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        // 构建 Job
        Job.Builder builder = Job.newBuilder()
                .setVin(order.getProdNo() != null ? order.getProdNo() : "") // 使用生产单号作为VIN
                .setDueEpochMs(dueEpochMs);

        // 从订单属性中获取颜色、配置、换型键等信息
        String color = "DEFAULT";
        String moldCode = "";
        String fixture = "";
        
        if (loadAttrs) {
            // 加载订单属性
            LambdaQueryWrapper<ProductionOrderAttr> attrWrapper = new LambdaQueryWrapper<>();
            attrWrapper.eq(ProductionOrderAttr::getOrdId, order.getId())
                       .eq(ProductionOrderAttr::getDeleted, 0);
            List<ProductionOrderAttr> attrs = productionOrderAttrMapper.selectList(attrWrapper);
            order.setAttrs(attrs);
            
            // 获取颜色属性，确保返回字符串类型
            String colorAttr = getOrderAttrValue(order, ProductionOrderAttr.AttrKey.COLOR);
            if (colorAttr != null && !colorAttr.trim().isEmpty()) {
                color = colorAttr.trim();
            }
            
            // 获取模具编码，确保返回字符串类型
            String moldCodeAttr = getOrderAttrValue(order, ProductionOrderAttr.AttrKey.MOLD_CODE);
            if (moldCodeAttr != null && !moldCodeAttr.trim().isEmpty()) {
                moldCode = moldCodeAttr.trim();
            }
            
            // 获取夹具属性，确保返回字符串类型
            String fixtureAttr = getOrderAttrValue(order, ProductionOrderAttr.AttrKey.FIXTURE);
            if (fixtureAttr != null && !fixtureAttr.trim().isEmpty()) {
                fixture = fixtureAttr.trim();
            }
        }
        
        // 设置颜色（必须是非空字符串，不能为 null）
        builder.setColor(color);
        
        // 设置配置（使用车型编码，必须是非空字符串）
        String configCode = "DEFAULT_CONFIG";
        if (model.getModelCode() != null && !model.getModelCode().trim().isEmpty()) {
            configCode = model.getModelCode().trim();
        }
        builder.setConfig(configCode);

        // 设置换型键（必须是非空字符串，不能为 null）
        builder.setMoldCode(moldCode);
        builder.setWeldingFixture(fixture);

        // TODO: 从工艺路线和工序中获取工时
        // 暂时使用默认工时（分钟）
        builder.setStampingMinutes(15);
        builder.setWeldingMinutes(20);
        builder.setPaintingMinutes(30);
        builder.setAssembleMinutes(60);

        // 设置能耗和排放分值（可从订单或车型中获取）
        builder.setEnergyScore(100.0);
        builder.setEmissionScore(50.0);

        return builder.build();
    }

    /**
     * 从订单属性列表中获取属性值
     */
    private String getOrderAttrValue(ProductionOrder order, String attrKey) {
        if (order.getAttrs() == null || order.getAttrs().isEmpty()) {
            return null;
        }
        
        return order.getAttrs().stream()
                .filter(attr -> attrKey.equals(attr.getAttrKey()))
                .map(ProductionOrderAttr::getAttrValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * 构建求解参数
     */
    @SuppressWarnings("unchecked")
    private SolveParams buildSolveParams(SchJob job) {
        SolveParams.Builder builder = SolveParams.newBuilder();

        // 默认算法和时间预算
        builder.setAlgorithm("sa"); // 模拟退火
        builder.setTimeBudgetSec(10); // 10秒
        builder.setSeed(42);

        // 从任务配置中获取目标权重
        Map<String, Object> objectiveJson = job.getObjectiveJson();
        if (objectiveJson != null && !objectiveJson.isEmpty()) {
            Weights.Builder weightsBuilder = Weights.newBuilder();
            
            // OTD权重 -> tardiness
            Object otdWeight = objectiveJson.get("otdWeight");
            if (otdWeight != null) {
                weightsBuilder.setTardiness(Double.parseDouble(otdWeight.toString()) / 10.0);
            }
            
            // 换型权重 -> color_change + config_change
            Object setupWeight = objectiveJson.get("setupWeight");
            if (setupWeight != null) {
                double weight = Double.parseDouble(setupWeight.toString()) / 10.0;
                weightsBuilder.setColorChange(weight * 1.5);
                weightsBuilder.setConfigChange(weight);
            }

            builder.setWeights(weightsBuilder.build());
        } else {
            // 默认权重
            builder.setWeights(Weights.newBuilder()
                    .setTardiness(10.0)
                    .setColorChange(50.0)
                    .setConfigChange(30.0)
                    .setEnergyExcess(2.0)
                    .setEmissionExcess(3.0)
                    .setMaterialShortage(0.0)
                    .build());
        }

        // 资源限制
        builder.setLimits(Limits.newBuilder()
                .setMaxEnergyPerShift(5000.0)
                .setMaxEmissionPerShift(2500.0)
                .build());

        return builder.build();
    }

    /**
     * 保存原始结果
     */
    private void saveRawResult(SchJob job, SolveResponse response) {
        SchEngineResultRaw raw = new SchEngineResultRaw();
        raw.setJobId(job.getId());
        raw.setRawJson(responseToJson(response));
        schEngineResultRawMapper.insert(raw);
    }

    /**
     * 创建方案
     */
    private Long createPlan(SchJob job, SolveResponse response) {
        SchPlan plan = new SchPlan();
        plan.setJobId(job.getId());
        plan.setPlanNo(generatePlanNo(job));
        plan.setIsBest(1); // 默认为最优
        plan.setStatus(0); // 草稿

        // 保存KPI数据
        if (response.hasSummary()) {
            KpiSummary summary = response.getSummary();
            Map<String, Object> kpiMap = new HashMap<>();
            kpiMap.put("cost", summary.getCost());
            kpiMap.put("totalTardinessMin", summary.getTotalTardinessMin());
            kpiMap.put("maxTardinessMin", summary.getMaxTardinessMin());
            kpiMap.put("colorChanges", summary.getColorChanges());
            kpiMap.put("configChanges", summary.getConfigChanges());
            kpiMap.put("elapsedMs", summary.getElapsedMs());
            plan.setKpiJson(kpiMap);
        }

        schPlanMapper.insert(plan);
        return plan.getId();
    }

    /**
     * 创建班次桶
     */
    private void createBuckets(Long planId, SolveResponse response, Map<String, Long> vinToOrderIdMap) {
        List<ScheduleItem> items = response.getDetailedScheduleList();
        if (items.isEmpty()) {
            items = response.getScheduleList(); // 如果没有详细调度表，使用简化版
        }

        for (ScheduleItem item : items) {
            SchPlanBucket bucket = new SchPlanBucket();
            bucket.setPlanId(planId);
            bucket.setProcessType(item.getProcessType());
            bucket.setLineId(item.getLineId());
            bucket.setShiftCode(item.getShiftId()); // shiftId 对应 shiftCode
            bucket.setSeqNo(item.getSeqInShift());
            
            // 转换时间戳为 LocalDate (业务日期)
            LocalDate bizDate = Instant.ofEpochMilli(item.getStartEpochMs())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            bucket.setBizDate(bizDate);
            
            // 通过 VIN (prodNo) 查找订单ID
            String vin = item.getVin();
            Long orderId = vinToOrderIdMap.get(vin);
            if (orderId == null) {
                log.warn("未找到VIN对应的订单ID, vin={}, 跳过该调度项", vin);
                continue; // 跳过无法关联订单的调度项
            }
            bucket.setProdOrderId(orderId);
            
            bucket.setQty(1); // 数量为整数
            bucket.setSetupMinutes(0); // TODO: 计算换型时间
            
            schPlanBucketMapper.insert(bucket);
        }
    }

    /**
     * 创建冲突记录
     */
    private void createConflicts(Long planId, SolveResponse response) {
        for (ShiftViolation violation : response.getViolationsList()) {
            SchPlanConflict conflict = new SchPlanConflict();
            conflict.setPlanId(planId);
            conflict.setConflictType(violation.getVtype());
            conflict.setLevel(2); // 警告
            conflict.setMessage("班次 " + violation.getShiftId() + " 超限: " + violation.getExcess());
            
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("shiftId", violation.getShiftId());
            payloadMap.put("excess", violation.getExcess());
            conflict.setPayload(payloadMap);
            
            schPlanConflictMapper.insert(conflict);
        }
    }

    /**
     * 创建统计记录
     */
    private void createStats(Long planId, SolveResponse response) {
        if (!response.hasSummary()) {
            return;
        }

        KpiSummary summary = response.getSummary();
        
        SchPlanStat stat = new SchPlanStat();
        stat.setPlanId(planId);
        
        // 计算 OTD 准时交付率（假设总延迟为0表示100%准时）
        if (summary.getTotalTardinessMin() == 0) {
            stat.setOtdRate(BigDecimal.valueOf(100.0));
        } else {
            stat.setOtdRate(BigDecimal.ZERO); // TODO: 根据实际逻辑计算
        }
        
        // 换型次数
        stat.setSetupTimes(summary.getColorChanges() + summary.getConfigChanges());
        
        // 平均产线负荷率（TODO: 需要从详细数据计算）
        stat.setAvgLineLoad(BigDecimal.valueOf(80.0));
        
        schPlanStatMapper.insert(stat);
    }

    /**
     * 生成方案编号
     */
    private String generatePlanNo(SchJob job) {
        return job.getJobNo().replace("JOB", "PLAN") + "-" + System.currentTimeMillis() % 1000;
    }

    /**
     * 转换响应为JSON
     */
    private Map<String, Object> responseToJson(SolveResponse response) {
        Map<String, Object> json = new HashMap<>();
        json.put("requestId", response.getRequestId());
        json.put("engineVersion", response.getEngineVersion());
        json.put("scheduleCount", response.getScheduleCount());
        json.put("violationsCount", response.getViolationsCount());
        // 可以添加更多字段...
        return json;
    }
}

