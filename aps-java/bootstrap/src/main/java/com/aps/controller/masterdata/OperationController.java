package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.OperationCreateRequest;
import com.aps.dto.request.masterdata.OperationQueryRequest;
import com.aps.dto.response.masterdata.OperationDTO;
import com.aps.masterdata.OperationService;
import com.aps.response.ApiResponse;
import com.aps.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工序管理控制器
 */
@Slf4j
@Tag(name = "工序管理", description = "工序的增删改查接口")
@RestController
@RequestMapping("/api/masterdata/operations")
@RequiredArgsConstructor
public class OperationController {

    private final OperationService operationService;

    @Operation(summary = "分页查询工序列表")
    @GetMapping
    public ApiResponse<PageResult<OperationDTO>> listOperations(@ModelAttribute OperationQueryRequest request) {
        log.info("查询工序列表: routeId={}, opCode={}, opName={}",
                request.getRouteId(), request.getOpCode(), request.getOpName());
        PageResult<OperationDTO> result = operationService.listOperations(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取工序详情")
    @GetMapping("/{id}")
    public ApiResponse<OperationDTO> getOperationById(
            @Parameter(description = "工序ID") @PathVariable Long id) {
        log.info("查询工序详情: id={}", id);
        OperationDTO operation = operationService.getOperationById(id);
        return ApiResponse.success(operation);
    }

    @Operation(summary = "根据路线ID查询工序列表（按序号排序）")
    @GetMapping("/route/{routeId}")
    public ApiResponse<List<OperationDTO>> listOperationsByRouteId(
            @Parameter(description = "工艺路线ID") @PathVariable Long routeId) {
        log.info("根据路线ID查询工序列表: routeId={}", routeId);
        List<OperationDTO> operations = operationService.listOperationsByRouteId(routeId);
        return ApiResponse.success(operations);
    }

    @Operation(summary = "创建工序")
    @PostMapping("/route/{routeId}")
    public ApiResponse<Long> createOperation(
            @Parameter(description = "工艺路线ID") @PathVariable Long routeId,
            @Validated @RequestBody OperationCreateRequest request) {
        log.info("创建工序: routeId={}, opCode={}", routeId, request.getOpCode());
        Long operationId = operationService.createOperation(routeId, request);
        return ApiResponse.success(operationId);
    }

    @Operation(summary = "更新工序")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateOperation(
            @Parameter(description = "工序ID") @PathVariable Long id,
            @Validated @RequestBody OperationCreateRequest request) {
        log.info("更新工序: id={}", id);
        operationService.updateOperation(id, request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除工序")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOperation(
            @Parameter(description = "工序ID") @PathVariable Long id) {
        log.info("删除工序: id={}", id);
        operationService.deleteOperation(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量删除工序")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteOperations(@RequestBody List<Long> ids) {
        log.info("批量删除工序: count={}", ids.size());
        operationService.batchDeleteOperations(ids);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用工序")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "工序ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("更新工序状态: id={}, status={}", id, status);
        operationService.updateStatus(id, status);
        return ApiResponse.success();
    }
}
