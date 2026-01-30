// service/src/main/java/com/aps/execution/impl/WorkOrderServiceImpl.java
package com.aps.execution.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aps.constant.enums.WorkOrderStatusEnum;
import com.aps.dto.request.execution.WorkOrderActionRequest;
import com.aps.dto.request.execution.WorkOrderQueryRequest;
import com.aps.dto.request.execution.WorkOrderReportRequest;
import com.aps.dto.response.execution.WorkOrderDTO;
import com.aps.entity.execution.Report;
import com.aps.entity.execution.StatusHistory;
import com.aps.entity.execution.WorkOrder;
import com.aps.exception.BusinessException;
import com.aps.execution.WorkOrderService;
import com.aps.mapper.execution.ReportMapper;
import com.aps.mapper.execution.StatusHistoryMapper;
import com.aps.mapper.execution.WorkOrderMapper;
import com.aps.response.PageResult;
import com.aps.utils.RequestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final ReportMapper reportMapper;
    private final StatusHistoryMapper statusHistoryMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<WorkOrderDTO> queryWorkOrders(WorkOrderQueryRequest request) {
        log.info("查询工单列表, request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();

        if (request.getProcessType() != null) {
            wrapper.eq(WorkOrder::getProcessType, request.getProcessType());
        }
        if (request.getLineId() != null) {
            wrapper.eq(WorkOrder::getLineId, request.getLineId());
        }
        if (request.getBizDate() != null) {
            wrapper.eq(WorkOrder::getBizDate, request.getBizDate());
        }
        if (request.getShiftCode() != null && !request.getShiftCode().isEmpty()) {
            wrapper.eq(WorkOrder::getShiftCode, request.getShiftCode());
        }
        if (request.getStatus() != null) {
            wrapper.eq(WorkOrder::getStatus, request.getStatus());
        }

        // 排序：业务日期升序、班次升序、顺序号升序
        wrapper.orderByAsc(WorkOrder::getBizDate, WorkOrder::getShiftCode, WorkOrder::getSeqNo);

        // 分页查询
        Page<WorkOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<WorkOrder> result = workOrderMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<WorkOrderDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public WorkOrderDTO getWorkOrderDetail(Long id) {
        log.info("获取工单详情, id={}", id);

        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null || workOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "工单不存在");
        }

        return convertToDTO(workOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseWorkOrder(Long id, WorkOrderActionRequest request) {
        log.info("下达工单, id={}", id);

        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null || workOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "工单不存在");
        }

        if (!WorkOrderStatusEnum.PENDING.getCode().equals(workOrder.getStatus())) {
            throw new BusinessException(40002, "工单状态不允许下达操作，当前状态: "
                    + WorkOrderStatusEnum.fromCode(workOrder.getStatus()).getDesc());
        }

        Integer oldStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatusEnum.RELEASED.getCode());
        if (request != null && request.getRemark() != null) {
            workOrder.setRemark(request.getRemark());
        }

        workOrderMapper.updateById(workOrder);
        saveStatusHistory(id, oldStatus, workOrder.getStatus(),
                request != null ? request.getRemark() : null);

        log.info("下达工单成功, id={}, woNo={}", id, workOrder.getWoNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startWorkOrder(Long id, WorkOrderActionRequest request) {
        log.info("开始工单, id={}", id);

        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null || workOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "工单不存在");
        }

        if (!WorkOrderStatusEnum.RELEASED.getCode().equals(workOrder.getStatus())) {
            throw new BusinessException(40002, "工单状态不允许开始操作，当前状态: "
                    + WorkOrderStatusEnum.fromCode(workOrder.getStatus()).getDesc());
        }

        Integer oldStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatusEnum.IN_PROGRESS.getCode());
        if (request != null && request.getRemark() != null) {
            workOrder.setRemark(request.getRemark());
        }

        workOrderMapper.updateById(workOrder);
        saveStatusHistory(id, oldStatus, workOrder.getStatus(),
                request != null ? request.getRemark() : null);

        log.info("开始工单成功, id={}, woNo={}", id, workOrder.getWoNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseWorkOrder(Long id, WorkOrderActionRequest request) {
        log.info("暂停工单, id={}", id);

        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null || workOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "工单不存在");
        }

        if (!WorkOrderStatusEnum.IN_PROGRESS.getCode().equals(workOrder.getStatus())) {
            throw new BusinessException(40002, "工单状态不允许暂停操作，当前状态: "
                    + WorkOrderStatusEnum.fromCode(workOrder.getStatus()).getDesc());
        }

        Integer oldStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatusEnum.PAUSED.getCode());
        if (request != null && request.getRemark() != null) {
            workOrder.setRemark(request.getRemark());
        }

        workOrderMapper.updateById(workOrder);
        saveStatusHistory(id, oldStatus, workOrder.getStatus(),
                request != null ? request.getRemark() : null);

        log.info("暂停工单成功, id={}, woNo={}", id, workOrder.getWoNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeWorkOrder(Long id, WorkOrderActionRequest request) {
        log.info("恢复工单, id={}", id);

        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null || workOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "工单不存在");
        }

        if (!WorkOrderStatusEnum.PAUSED.getCode().equals(workOrder.getStatus())) {
            throw new BusinessException(40002, "工单状态不允许恢复操作，当前状态: "
                    + WorkOrderStatusEnum.fromCode(workOrder.getStatus()).getDesc());
        }

        Integer oldStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatusEnum.IN_PROGRESS.getCode());
        if (request != null && request.getRemark() != null) {
            workOrder.setRemark(request.getRemark());
        }

        workOrderMapper.updateById(workOrder);
        saveStatusHistory(id, oldStatus, workOrder.getStatus(),
                request != null ? request.getRemark() : null);

        log.info("恢复工单成功, id={}, woNo={}", id, workOrder.getWoNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeWorkOrder(Long id, WorkOrderActionRequest request) {
        log.info("完成工单, id={}", id);

        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null || workOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "工单不存在");
        }

        if (!WorkOrderStatusEnum.IN_PROGRESS.getCode().equals(workOrder.getStatus())) {
            throw new BusinessException(40002, "工单状态不允许完成操作，当前状态: "
                    + WorkOrderStatusEnum.fromCode(workOrder.getStatus()).getDesc());
        }

        if (workOrder.getQtyDone() < workOrder.getQtyPlanned()) {
            throw new BusinessException(40002, String.format(
                    "完成数量未达到计划数量，完成数=%d，计划数=%d",
                    workOrder.getQtyDone(), workOrder.getQtyPlanned()));
        }

        Integer oldStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatusEnum.COMPLETED.getCode());
        if (request != null && request.getRemark() != null) {
            workOrder.setRemark(request.getRemark());
        }

        workOrderMapper.updateById(workOrder);
        saveStatusHistory(id, oldStatus, workOrder.getStatus(),
                request != null ? request.getRemark() : null);

        log.info("完成工单成功, id={}, woNo={}, qtyDone={}/{}",
                id, workOrder.getWoNo(), workOrder.getQtyDone(), workOrder.getQtyPlanned());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidWorkOrder(Long id, String reason) {
        log.info("作废工单, id={}, reason={}", id, reason);

        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null || workOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "工单不存在");
        }

        if (WorkOrderStatusEnum.COMPLETED.getCode().equals(workOrder.getStatus())) {
            throw new BusinessException(40002, "已完成的工单不允许作废");
        }

        Integer oldStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatusEnum.VOIDED.getCode());
        workOrder.setRemark(reason);

        workOrderMapper.updateById(workOrder);
        saveStatusHistory(id, oldStatus, workOrder.getStatus(), reason);

        log.info("作废工单成功, id={}, woNo={}, reason={}", id, workOrder.getWoNo(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportWorkOrder(Long id, WorkOrderReportRequest request) {
        log.info("工单报工, id={}, qtyGood={}, qtyBad={}", id, request.getQtyGood(), request.getQtyBad());

        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null || workOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "工单不存在");
        }

        if (!WorkOrderStatusEnum.IN_PROGRESS.getCode().equals(workOrder.getStatus())) {
            throw new BusinessException(40002, "工单状态不允许报工，当前状态: "
                    + WorkOrderStatusEnum.fromCode(workOrder.getStatus()).getDesc());
        }

        if (request.getQtyGood() < 0 || request.getQtyBad() < 0) {
            throw new BusinessException(40000, "报工数量不能为负数");
        }

        Integer newQtyDone = workOrder.getQtyDone() + request.getQtyGood();

        if (newQtyDone > workOrder.getQtyPlanned()) {
            throw new BusinessException(40002, String.format(
                    "报工后完成数量超过计划数量，当前完成=%d，本次报工=%d，计划数量=%d",
                    workOrder.getQtyDone(), request.getQtyGood(), workOrder.getQtyPlanned()));
        }

        // 创建报工记录
        Report report = new Report();
        report.setWoId(id);
        report.setReportTime(request.getReportTime());
        report.setStationId(request.getStationId());
        report.setQtyGood(request.getQtyGood());
        report.setQtyBad(request.getQtyBad());

        if (request.getDetailJson() != null) {
            try {
                report.setDetailJson(objectMapper.writeValueAsString(request.getDetailJson()));
            } catch (JsonProcessingException e) {
                throw new BusinessException(40000, "详情JSON格式错误: " + e.getMessage());
            }
        }

        reportMapper.insert(report);

        // 更新工单完成数量
        workOrder.setQtyDone(newQtyDone);
        workOrderMapper.updateById(workOrder);

        log.info("工单报工成功, id={}, woNo={}, qtyGood={}, qtyBad={}, qtyDone={}/{}",
                id, workOrder.getWoNo(), request.getQtyGood(), request.getQtyBad(),
                newQtyDone, workOrder.getQtyPlanned());
    }

    /**
     * 保存状态变更历史
     */
    private void saveStatusHistory(Long woId, Integer fromStatus, Integer toStatus, String remark) {
        StatusHistory history = new StatusHistory();
        history.setWoId(woId);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setOperatorId(RequestUtil.getUsername());  // 改为 String 类型
        history.setRemark(remark);

        statusHistoryMapper.insert(history);
    }

    /**
     * 转换为DTO (移除了不存在的 set 方法)
     */
    private WorkOrderDTO convertToDTO(WorkOrder workOrder) {
        WorkOrderDTO dto = new WorkOrderDTO();
        BeanUtil.copyProperties(workOrder, dto);

        // 设置状态文本 (扩展字段)
        WorkOrderStatusEnum statusEnum = WorkOrderStatusEnum.fromCode(workOrder.getStatus());
        if (statusEnum != null) {
            dto.setStatusText(statusEnum.getDesc());
        }

        // 设置工艺类型文本 (扩展字段)
        dto.setProcessTypeText(getProcessTypeText(workOrder.getProcessType()));

        // 计算完成率 (扩展字段)
        if (workOrder.getQtyPlanned() != null && workOrder.getQtyPlanned() > 0) {
            double rate = (double) workOrder.getQtyDone() / workOrder.getQtyPlanned() * 100;
            dto.setCompletionRate(Math.round(rate * 100.0) / 100.0);
        }

        return dto;
    }

    /**
     * 获取工艺类型文本
     */
    private String getProcessTypeText(Integer processType) {
        if (processType == null) {
            return null;
        }
        switch (processType) {
            case 1: return "冲压";
            case 2: return "焊装";
            case 3: return "涂装";
            case 4: return "总装";
            default: return "未知";
        }
    }
}

