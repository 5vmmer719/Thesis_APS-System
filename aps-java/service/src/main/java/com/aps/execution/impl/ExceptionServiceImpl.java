// service/src/main/java/com/aps/execution/impl/ExceptionServiceImpl.java
package com.aps.execution.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aps.constant.enums.ExceptionStatusEnum;
import com.aps.dto.request.execution.ExceptionCreateRequest;
import com.aps.dto.request.execution.ExceptionQueryRequest;
import com.aps.dto.response.execution.ExceptionDTO;
import com.aps.entity.execution.ExeException;
import com.aps.entity.execution.WorkOrder;
import com.aps.exception.BusinessException;
import com.aps.execution.ExceptionService;
import com.aps.mapper.execution.ExeExceptionMapper;
import com.aps.mapper.execution.WorkOrderMapper;
import com.aps.response.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 异常服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExceptionServiceImpl implements ExceptionService {

    private final ExeExceptionMapper exeExceptionMapper;
    private final WorkOrderMapper workOrderMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createException(ExceptionCreateRequest request) {
        log.info("创建异常, woId={}, type={}, level={}",
                request.getWoId(), request.getType(), request.getLevel());

        WorkOrder workOrder = workOrderMapper.selectById(request.getWoId());
        if (workOrder == null || workOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "工单不存在");
        }

        ExeException exception = new ExeException();
        exception.setWoId(request.getWoId());
        exception.setType(request.getType());
        exception.setLevel(request.getLevel());
        exception.setStatus(ExceptionStatusEnum.NEW.getCode());
        exception.setDesc(request.getDesc());

        if (request.getPayload() != null) {
            try {
                exception.setPayload(objectMapper.writeValueAsString(request.getPayload()));
            } catch (JsonProcessingException e) {
                throw new BusinessException(40000, "负载JSON格式错误: " + e.getMessage());
            }
        }

        exeExceptionMapper.insert(exception);

        log.info("创建异常成功, exceptionId={}, woId={}, woNo={}",
                exception.getId(), request.getWoId(), workOrder.getWoNo());

        return exception.getId();
    }

    @Override
    public PageResult<ExceptionDTO> queryExceptions(ExceptionQueryRequest request) {
        log.info("查询异常列表, request={}", request);

        LambdaQueryWrapper<ExeException> wrapper = new LambdaQueryWrapper<>();

        if (request.getStatus() != null) {
            wrapper.eq(ExeException::getStatus, request.getStatus());
        }
        if (request.getLevel() != null) {
            wrapper.eq(ExeException::getLevel, request.getLevel());
        }
        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.eq(ExeException::getType, request.getType());
        }
        if (request.getFromTime() != null) {
            wrapper.ge(ExeException::getCreatedTime, request.getFromTime());
        }
        if (request.getToTime() != null) {
            wrapper.le(ExeException::getCreatedTime, request.getToTime());
        }

        wrapper.orderByDesc(ExeException::getLevel, ExeException::getCreatedTime);

        Page<ExeException> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ExeException> result = exeExceptionMapper.selectPage(page, wrapper);

        List<ExceptionDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public ExceptionDTO getExceptionDetail(Long id) {
        log.info("获取异常详情, id={}", id);

        ExeException exception = exeExceptionMapper.selectById(id);
        if (exception == null || exception.getDeleted() == 1) {
            throw new BusinessException(40400, "异常记录不存在");
        }

        return convertToDTO(exception);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptException(Long id) {
        log.info("接单处理异常, id={}", id);

        ExeException exception = exeExceptionMapper.selectById(id);
        if (exception == null || exception.getDeleted() == 1) {
            throw new BusinessException(40400, "异常记录不存在");
        }

        if (!ExceptionStatusEnum.NEW.getCode().equals(exception.getStatus())) {
            throw new BusinessException(40002, "异常状态不允许接单操作，当前状态: "
                    + ExceptionStatusEnum.fromCode(exception.getStatus()).getDesc());
        }

        exception.setStatus(ExceptionStatusEnum.IN_PROGRESS.getCode());
        exeExceptionMapper.updateById(exception);

        log.info("接单处理异常成功, id={}, woId={}", id, exception.getWoId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeException(Long id, String comment) {
        log.info("关闭异常, id={}, comment={}", id, comment);

        ExeException exception = exeExceptionMapper.selectById(id);
        if (exception == null || exception.getDeleted() == 1) {
            throw new BusinessException(40400, "异常记录不存在");
        }

        if (!ExceptionStatusEnum.IN_PROGRESS.getCode().equals(exception.getStatus())) {
            throw new BusinessException(40002, "异常状态不允许关闭操作，当前状态: "
                    + ExceptionStatusEnum.fromCode(exception.getStatus()).getDesc());
        }

        exception.setStatus(ExceptionStatusEnum.CLOSED.getCode());

        if (comment != null && !comment.isEmpty()) {
            String newDesc = exception.getDesc() + "\n[关闭备注] " + comment;
            exception.setDesc(newDesc);
        }

        exeExceptionMapper.updateById(exception);

        log.info("关闭异常成功, id={}, woId={}", id, exception.getWoId());
    }

    /**
     * 转换为DTO (移除了不存在的 set 方法)
     */
    private ExceptionDTO convertToDTO(ExeException exception) {
        ExceptionDTO dto = new ExceptionDTO();
        BeanUtil.copyProperties(exception, dto);

        // 设置状态文本 (扩展字段)
        ExceptionStatusEnum statusEnum = ExceptionStatusEnum.fromCode(exception.getStatus());
        if (statusEnum != null) {
            dto.setStatusText(statusEnum.getDesc());
        }

        // 设置等级文本 (扩展字段)
        dto.setLevelText(getLevelText(exception.getLevel()));

        // 解析 payload JSON
        if (exception.getPayload() != null && !exception.getPayload().isEmpty()) {
            try {
                Map<String, Object> payload = objectMapper.readValue(
                        exception.getPayload(),
                        new TypeReference<Map<String, Object>>() {}
                );
                dto.setPayload(payload);
            } catch (JsonProcessingException e) {
                log.warn("解析payload JSON失败, exceptionId={}", exception.getId(), e);
            }
        }

        return dto;
    }

    /**
     * 获取等级文本
     */
    private String getLevelText(Integer level) {
        if (level == null) {
            return null;
        }
        switch (level) {
            case 1: return "提示";
            case 2: return "警告";
            case 3: return "严重";
            default: return "未知";
        }
    }
}
