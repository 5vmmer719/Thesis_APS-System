package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.RouteCreateRequest;
import com.aps.dto.request.masterdata.RouteQueryRequest;
import com.aps.dto.request.masterdata.RouteUpdateRequest;
import com.aps.dto.response.masterdata.OperationDTO;
import com.aps.dto.response.masterdata.RouteDTO;
import com.aps.dto.response.masterdata.RouteDetailDTO;
import com.aps.entity.masterdata.Operation;
import com.aps.entity.masterdata.Route;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.OperationMapper;
import com.aps.mapper.masterdata.RouteMapper;
import com.aps.masterdata.RouteService;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工艺路线服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteMapper routeMapper;
    private final OperationMapper operationMapper;

    // 工艺类型映射
    private static final Map<Integer, String> PROCESS_TYPE_MAP = Map.of(
            1, "冲压",
            2, "焊装",
            3, "涂装",
            4, "总装"
    );

    @Override
    public PageResult<RouteDTO> listRoutes(RouteQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Route> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<Route> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Route> resultPage = routeMapper.selectPage(page, wrapper);

        // 批量查询工序数量和工时统计
        List<Long> routeIds = resultPage.getRecords().stream()
                .map(Route::getId)
                .collect(Collectors.toList());

        Map<Long, List<Operation>> operationMap = routeIds.isEmpty() ? Map.of() :
                operationMapper.selectList(new LambdaQueryWrapper<Operation>()
                                .in(Operation::getRouteId, routeIds))
                        .stream()
                        .collect(Collectors.groupingBy(Operation::getRouteId));

        // 转换为DTO
        List<RouteDTO> routeDTOS = resultPage.getRecords().stream()
                .map(route -> convertToDTO(route, operationMap.getOrDefault(route.getId(), List.of())))
                .collect(Collectors.toList());

        log.info("分页查询工艺路线结果: total={}, records={}", resultPage.getTotal(), routeDTOS.size());

        return new PageResult<>(
                routeDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public RouteDetailDTO getRouteDetailById(Long id) {
        // 查询工艺路线基本信息
        Route route = routeMapper.selectById(id);
        if (route == null) {
            throw new BusinessException("工艺路线不存在");
        }

        // 查询工序列表
        List<Operation> operations = operationMapper.selectList(
                new LambdaQueryWrapper<Operation>()
                        .eq(Operation::getRouteId, id)
                        .orderByAsc(Operation::getSeqNo)
        );

        // 转换为DTO
        RouteDTO routeDTO = convertToDTO(route, operations);
        List<OperationDTO> operationDTOS = operations.stream()
                .map(this::convertToOperationDTO)
                .collect(Collectors.toList());

        RouteDetailDTO detailDTO = new RouteDetailDTO();
        detailDTO.setRoute(routeDTO);
        detailDTO.setOperations(operationDTOS);

        return detailDTO;
    }

    @Override
    public RouteDTO getRouteById(Long id) {
        Route route = routeMapper.selectById(id);
        if (route == null) {
            throw new BusinessException("工艺路线不存在");
        }

        // 查询工序列表
        List<Operation> operations = operationMapper.selectList(
                new LambdaQueryWrapper<Operation>().eq(Operation::getRouteId, id)
        );

        return convertToDTO(route, operations);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRoute(RouteCreateRequest request) {
        // 校验路线编码是否重复
        Long count = routeMapper.selectCount(
                new LambdaQueryWrapper<Route>().eq(Route::getRouteCode, request.getRouteCode())
        );
        if (count > 0) {
            throw new BusinessException("路线编码已存在");
        }

        // 校验车型+工艺类型+版本的唯一性
        count = routeMapper.selectCount(
                new LambdaQueryWrapper<Route>()
                        .eq(Route::getModelId, request.getModelId())
                        .eq(Route::getProcessType, request.getProcessType())
                        .eq(Route::getVersion, request.getVersion())
        );
        if (count > 0) {
            throw new BusinessException("该车型的该工艺类型版本已存在");
        }

        // 创建工艺路线主记录
        Route route = new Route();
        route.setRouteCode(request.getRouteCode());
        route.setModelId(request.getModelId());
        route.setProcessType(request.getProcessType());
        route.setVersion(request.getVersion());
        route.setStatus(request.getStatus());
        route.setRemark(request.getRemark());

        routeMapper.insert(route);
        log.info("创建工艺路线成功: id={}, routeCode={}", route.getId(), route.getRouteCode());

        // 创建工序记录
        if (request.getOperations() != null && !request.getOperations().isEmpty()) {
            List<Operation> operations = request.getOperations().stream()
                    .map(opRequest -> {
                        Operation operation = new Operation();
                        operation.setRouteId(route.getId());
                        operation.setOpCode(opRequest.getOpCode());
                        operation.setOpName(opRequest.getOpName());
                        operation.setSeqNo(opRequest.getSeqNo());
                        operation.setStdMinutesPerUnit(opRequest.getStdMinutesPerUnit());
                        operation.setSetupMinutes(opRequest.getSetupMinutes());
                        operation.setStationGroup(opRequest.getStationGroup());
                        operation.setConstraintJson(opRequest.getConstraintJson());
                        operation.setStatus(opRequest.getStatus());
                        operation.setRemark(opRequest.getRemark());
                        return operation;
                    })
                    .collect(Collectors.toList());

            operations.forEach(operationMapper::insert);
            log.info("创建工序成功: routeId={}, operationCount={}", route.getId(), operations.size());
        }

        return route.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoute(RouteUpdateRequest request) {
        // 查询工艺路线是否存在
        Route route = routeMapper.selectById(request.getId());
        if (route == null) {
            throw new BusinessException("工艺路线不存在");
        }

        // 更新工艺路线基本信息
        if (StringUtils.hasText(request.getVersion())) {
            route.setVersion(request.getVersion());
        }
        if (request.getStatus() != null) {
            route.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            route.setRemark(request.getRemark());
        }

        routeMapper.updateById(route);
        log.info("更新工艺路线基本信息成功: id={}", route.getId());

        // 如果提供了工序列表，则全量更新工序
        if (request.getOperations() != null && !request.getOperations().isEmpty()) {
            // 删除原有工序
            operationMapper.delete(new LambdaQueryWrapper<Operation>()
                    .eq(Operation::getRouteId, request.getId()));

            // 插入新工序
            List<Operation> operations = request.getOperations().stream()
                    .map(opRequest -> {
                        Operation operation = new Operation();
                        operation.setRouteId(request.getId());
                        operation.setOpCode(opRequest.getOpCode());
                        operation.setOpName(opRequest.getOpName());
                        operation.setSeqNo(opRequest.getSeqNo());
                        operation.setStdMinutesPerUnit(opRequest.getStdMinutesPerUnit());
                        operation.setSetupMinutes(opRequest.getSetupMinutes());
                        operation.setStationGroup(opRequest.getStationGroup());
                        operation.setConstraintJson(opRequest.getConstraintJson());
                        operation.setStatus(opRequest.getStatus());
                        operation.setRemark(opRequest.getRemark());
                        return operation;
                    })
                    .collect(Collectors.toList());

            operations.forEach(operationMapper::insert);
            log.info("更新工序成功: routeId={}, operationCount={}", request.getId(), operations.size());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoute(Long id) {
        // 查询工艺路线是否存在
        Route route = routeMapper.selectById(id);
        if (route == null) {
            throw new BusinessException("工艺路线不存在");
        }

        // 逻辑删除工艺路线
        routeMapper.deleteById(id);

        // 逻辑删除工序
        operationMapper.delete(new LambdaQueryWrapper<Operation>()
                .eq(Operation::getRouteId, id));

        log.info("删除工艺路线成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRoutes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 批量逻辑删除工艺路线
        routeMapper.deleteBatchIds(ids);

        // 批量逻辑删除工序
        operationMapper.delete(new LambdaQueryWrapper<Operation>()
                .in(Operation::getRouteId, ids));

        log.info("批量删除工艺路线成功: count={}", ids.size());
    }

    @Override
    public PageResult<RouteDTO> listRoutesByModelId(Long modelId, Integer pageNum, Integer pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Route::getModelId, modelId)
                .orderByDesc(Route::getCreatedTime);

        // 分页查询
        Page<Route> page = new Page<>(pageNum, pageSize);
        IPage<Route> resultPage = routeMapper.selectPage(page, wrapper);

        // 批量查询工序
        List<Long> routeIds = resultPage.getRecords().stream()
                .map(Route::getId)
                .collect(Collectors.toList());

        Map<Long, List<Operation>> operationMap = routeIds.isEmpty() ? Map.of() :
                operationMapper.selectList(new LambdaQueryWrapper<Operation>()
                                .in(Operation::getRouteId, routeIds))
                        .stream()
                        .collect(Collectors.groupingBy(Operation::getRouteId));

        // 转换为DTO
        List<RouteDTO> routeDTOS = resultPage.getRecords().stream()
                .map(route -> convertToDTO(route, operationMap.getOrDefault(route.getId(), List.of())))
                .collect(Collectors.toList());

        return new PageResult<>(
                routeDTOS,
                resultPage.getTotal(),
                pageNum,
                pageSize
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyRoute(Long routeId, String newVersion) {
        // 查询源工艺路线
        Route sourceRoute = routeMapper.selectById(routeId);
        if (sourceRoute == null) {
            throw new BusinessException("源工艺路线不存在");
        }

        // 生成新的路线编码
        String newRouteCode = sourceRoute.getRouteCode() + "_" + newVersion;

        // 校验新编码是否重复
        Long count = routeMapper.selectCount(
                new LambdaQueryWrapper<Route>().eq(Route::getRouteCode, newRouteCode)
        );
        if (count > 0) {
            throw new BusinessException("新路线编码已存在");
        }

        // 校验车型+工艺类型+版本的唯一性
        count = routeMapper.selectCount(
                new LambdaQueryWrapper<Route>()
                        .eq(Route::getModelId, sourceRoute.getModelId())
                        .eq(Route::getProcessType, sourceRoute.getProcessType())
                        .eq(Route::getVersion, newVersion)
        );
        if (count > 0) {
            throw new BusinessException("该车型的该工艺类型版本已存在");
        }

        // 创建新工艺路线
        Route newRoute = new Route();
        newRoute.setRouteCode(newRouteCode);
        newRoute.setModelId(sourceRoute.getModelId());
        newRoute.setProcessType(sourceRoute.getProcessType());
        newRoute.setVersion(newVersion);
        newRoute.setStatus(sourceRoute.getStatus());
        newRoute.setRemark("从 " + sourceRoute.getRouteCode() + " 复制");

        routeMapper.insert(newRoute);
        log.info("复制工艺路线成功: sourceId={}, newId={}, newVersion={}",
                routeId, newRoute.getId(), newVersion);

        // 复制工序
        List<Operation> sourceOperations = operationMapper.selectList(
                new LambdaQueryWrapper<Operation>()
                        .eq(Operation::getRouteId, routeId)
                        .orderByAsc(Operation::getSeqNo)
        );

        List<Operation> newOperations = sourceOperations.stream()
                .map(sourceOp -> {
                    Operation newOp = new Operation();
                    newOp.setRouteId(newRoute.getId());
                    newOp.setOpCode(sourceOp.getOpCode());
                    newOp.setOpName(sourceOp.getOpName());
                    newOp.setSeqNo(sourceOp.getSeqNo());
                    newOp.setStdMinutesPerUnit(sourceOp.getStdMinutesPerUnit());
                    newOp.setSetupMinutes(sourceOp.getSetupMinutes());
                    newOp.setStationGroup(sourceOp.getStationGroup());
                    newOp.setConstraintJson(sourceOp.getConstraintJson());
                    newOp.setStatus(sourceOp.getStatus());
                    newOp.setRemark(sourceOp.getRemark());
                    return newOp;
                })
                .collect(Collectors.toList());

        newOperations.forEach(operationMapper::insert);
        log.info("复制工序成功: newRouteId={}, operationCount={}", newRoute.getId(), newOperations.size());

        return newRoute.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Route route = routeMapper.selectById(id);
        if (route == null) {
            throw new BusinessException("工艺路线不存在");
        }

        route.setStatus(status);
        routeMapper.updateById(route);

        log.info("更新工艺路线状态成功: id={}, status={}", id, status);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Route> buildQueryWrapper(RouteQueryRequest request) {
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getRouteCode())) {
            wrapper.like(Route::getRouteCode, request.getRouteCode());
        }

        if (request.getModelId() != null) {
            wrapper.eq(Route::getModelId, request.getModelId());
        }

        if (request.getProcessType() != null) {
            wrapper.eq(Route::getProcessType, request.getProcessType());
        }

        if (StringUtils.hasText(request.getVersion())) {
            wrapper.eq(Route::getVersion, request.getVersion());
        }

        if (request.getStatus() != null) {
            wrapper.eq(Route::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Route::getCreatedTime);

        return wrapper;
    }

    /**
     * 转换为RouteDTO
     */
    private RouteDTO convertToDTO(Route route, List<Operation> operations) {
        RouteDTO dto = new RouteDTO();
        dto.setId(route.getId());
        dto.setRouteCode(route.getRouteCode());
        dto.setModelId(route.getModelId());
        dto.setProcessType(route.getProcessType());
        dto.setProcessTypeText(PROCESS_TYPE_MAP.getOrDefault(route.getProcessType(), "未知"));
        dto.setVersion(route.getVersion());
        dto.setStatus(route.getStatus());
        dto.setStatusText(route.getStatus() == 1 ? "启用" : "禁用");
        dto.setOperationCount(operations.size());

        // 计算总工时
        int totalStdMinutes = operations.stream()
                .mapToInt(op -> op.getStdMinutesPerUnit() != null ? op.getStdMinutesPerUnit() : 0)
                .sum();
        int totalSetupMinutes = operations.stream()
                .mapToInt(op -> op.getSetupMinutes() != null ? op.getSetupMinutes() : 0)
                .sum();

        dto.setTotalStdMinutes(totalStdMinutes);
        dto.setTotalSetupMinutes(totalSetupMinutes);
        dto.setRemark(route.getRemark());
        dto.setCreatedTime(route.getCreatedTime());
        dto.setCreatedBy(route.getCreatedBy());
        dto.setUpdatedTime(route.getUpdatedTime());
        dto.setUpdatedBy(route.getUpdatedBy());

        return dto;
    }

    /**
     * 转换为OperationDTO
     */
    private OperationDTO convertToOperationDTO(Operation operation) {
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

