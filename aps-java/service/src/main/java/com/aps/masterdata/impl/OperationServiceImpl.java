package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.OperationCreateRequest;
import com.aps.dto.request.masterdata.OperationQueryRequest;
import com.aps.dto.response.masterdata.OperationDTO;
import com.aps.entity.masterdata.Operation;
import com.aps.entity.masterdata.Route;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.OperationMapper;
import com.aps.mapper.masterdata.RouteMapper;
import com.aps.masterdata.OperationService;
import com.aps.response.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工序服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    private final OperationMapper operationMapper;
    private final RouteMapper routeMapper;

    @Override
    public PageResult<OperationDTO> listOperations(OperationQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Operation> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<Operation> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Operation> resultPage = operationMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<OperationDTO> operationDTOS = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("分页查询工序结果: total={}, records={}", resultPage.getTotal(), operationDTOS.size());

        return new PageResult<>(
                operationDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public OperationDTO getOperationById(Long id) {
        Operation operation = operationMapper.selectById(id);
        if (operation == null) {
            throw new BusinessException("工序不存在");
        }

        return convertToDTO(operation);
    }

    @Override
    public List<OperationDTO> listOperationsByRouteId(Long routeId) {
        LambdaQueryWrapper<Operation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Operation::getRouteId, routeId)
                .eq(Operation::getStatus, 1)
                .orderByAsc(Operation::getSeqNo);

        List<Operation> operations = operationMapper.selectList(wrapper);

        return operations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOperation(Long routeId, OperationCreateRequest request) {
        // 校验工艺路线是否存在
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new BusinessException("工艺路线不存在");
        }

        // 校验工序编码在该路线下是否重复
        Long count = operationMapper.selectCount(
                new LambdaQueryWrapper<Operation>()
                        .eq(Operation::getRouteId, routeId)
                        .eq(Operation::getOpCode, request.getOpCode())
        );
        if (count > 0) {
            throw new BusinessException("该路线下工序编码已存在");
        }

        // 创建工序
        Operation operation = new Operation();
        operation.setRouteId(routeId);
        operation.setOpCode(request.getOpCode());
        operation.setOpName(request.getOpName());
        operation.setSeqNo(request.getSeqNo());
        operation.setStdMinutesPerUnit(request.getStdMinutesPerUnit());
        operation.setSetupMinutes(request.getSetupMinutes());
        operation.setStationGroup(request.getStationGroup());
        operation.setConstraintJson(request.getConstraintJson());
        operation.setStatus(request.getStatus());
        operation.setRemark(request.getRemark());

        operationMapper.insert(operation);
        log.info("创建工序成功: id={}, opCode={}, routeId={}", operation.getId(), operation.getOpCode(), routeId);

        return operation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOperation(Long id, OperationCreateRequest request) {
        // 查询工序是否存在
        Operation operation = operationMapper.selectById(id);
        if (operation == null) {
            throw new BusinessException("工序不存在");
        }

        // 如果修改了工序编码，校验是否重复
        if (!operation.getOpCode().equals(request.getOpCode())) {
            Long count = operationMapper.selectCount(
                    new LambdaQueryWrapper<Operation>()
                            .eq(Operation::getRouteId, operation.getRouteId())
                            .eq(Operation::getOpCode, request.getOpCode())
                            .ne(Operation::getId, id)
            );
            if (count > 0) {
                throw new BusinessException("该路线下工序编码已存在");
            }
        }

        // 更新工序
        operation.setOpCode(request.getOpCode());
        operation.setOpName(request.getOpName());
        operation.setSeqNo(request.getSeqNo());
        operation.setStdMinutesPerUnit(request.getStdMinutesPerUnit());
        operation.setSetupMinutes(request.getSetupMinutes());
        operation.setStationGroup(request.getStationGroup());
        operation.setConstraintJson(request.getConstraintJson());
        operation.setStatus(request.getStatus());
        operation.setRemark(request.getRemark());

        operationMapper.updateById(operation);
        log.info("更新工序成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOperation(Long id) {
        // 查询工序是否存在
        Operation operation = operationMapper.selectById(id);
        if (operation == null) {
            throw new BusinessException("工序不存在");
        }

        // 逻辑删除工序
        operationMapper.deleteById(id);
        log.info("删除工序成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteOperations(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 批量逻辑删除工序
        operationMapper.deleteBatchIds(ids);
        log.info("批量删除工序成功: count={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Operation operation = operationMapper.selectById(id);
        if (operation == null) {
            throw new BusinessException("工序不存在");
        }

        operation.setStatus(status);
        operationMapper.updateById(operation);

        log.info("更新工序状态成功: id={}, status={}", id, status);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Operation> buildQueryWrapper(OperationQueryRequest request) {
        LambdaQueryWrapper<Operation> wrapper = new LambdaQueryWrapper<>();

        if (request.getRouteId() != null) {
            wrapper.eq(Operation::getRouteId, request.getRouteId());
        }

        if (StringUtils.hasText(request.getOpCode())) {
            wrapper.like(Operation::getOpCode, request.getOpCode());
        }

        if (StringUtils.hasText(request.getOpName())) {
            wrapper.like(Operation::getOpName, request.getOpName());
        }

        if (StringUtils.hasText(request.getStationGroup())) {
            wrapper.eq(Operation::getStationGroup, request.getStationGroup());
        }

        if (request.getStatus() != null) {
            wrapper.eq(Operation::getStatus, request.getStatus());
        }

        wrapper.orderByAsc(Operation::getRouteId, Operation::getSeqNo);

        return wrapper;
    }

    /**
     * 转换为OperationDTO
     */
    private OperationDTO convertToDTO(Operation operation) {
        OperationDTO dto = new OperationDTO();
        dto.setId(operation.getId());
        dto.setRouteId(operation.getRouteId());
        dto.setOpCode(operation.getOpCode());
        dto.setOpName(operation.getOpName());
        dto.setSeqNo(operation.getSeqNo());
        dto.setStdMinutesPerUnit(operation.getStdMinutesPerUnit());
        dto.setSetupMinutes(operation.getSetupMinutes());
        dto.setStationGroup(operation.getStationGroup());
        dto.setConstraintJson(operation.getConstraintJson());
        dto.setStatus(operation.getStatus());
        dto.setStatusText(operation.getStatus() == 1 ? "启用" : "禁用");
        dto.setRemark(operation.getRemark());

        return dto;
    }
}

