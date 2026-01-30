package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.RouteCreateRequest;
import com.aps.dto.request.masterdata.RouteQueryRequest;
import com.aps.dto.request.masterdata.RouteUpdateRequest;
import com.aps.dto.response.masterdata.RouteDTO;
import com.aps.dto.response.masterdata.RouteDetailDTO;
import com.aps.masterdata.RouteService;
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
 * 工艺路线管理控制器
 */
@Slf4j
@Tag(name = "工艺路线管理", description = "工艺路线的增删改查接口")
@RestController
@RequestMapping("/api/masterdata/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "分页查询工艺路线列表")
    @GetMapping
    public ApiResponse<PageResult<RouteDTO>> listRoutes(@ModelAttribute RouteQueryRequest request) {
        log.info("查询工艺路线列表: routeCode={}, modelId={}, processType={}, version={}, status={}",
                request.getRouteCode(), request.getModelId(), request.getProcessType(),
                request.getVersion(), request.getStatus());
        PageResult<RouteDTO> result = routeService.listRoutes(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取工艺路线详情（包含工序）")
    @GetMapping("/{id}/detail")
    public ApiResponse<RouteDetailDTO> getRouteDetailById(
            @Parameter(description = "工艺路线ID") @PathVariable Long id) {
        log.info("查询工艺路线详情: id={}", id);
        RouteDetailDTO detail = routeService.getRouteDetailById(id);
        return ApiResponse.success(detail);
    }

    @Operation(summary = "根据ID获取工艺路线基本信息")
    @GetMapping("/{id}")
    public ApiResponse<RouteDTO> getRouteById(
            @Parameter(description = "工艺路线ID") @PathVariable Long id) {
        log.info("查询工艺路线基本信息: id={}", id);
        RouteDTO route = routeService.getRouteById(id);
        return ApiResponse.success(route);
    }

    @Operation(summary = "创建工艺路线")
    @PostMapping
    public ApiResponse<Long> createRoute(@Validated @RequestBody RouteCreateRequest request) {
        log.info("创建工艺路线: routeCode={}, modelId={}, processType={}",
                request.getRouteCode(), request.getModelId(), request.getProcessType());
        Long routeId = routeService.createRoute(request);
        return ApiResponse.success(routeId);
    }

    @Operation(summary = "更新工艺路线")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateRoute(
            @Parameter(description = "工艺路线ID") @PathVariable Long id,
            @Validated @RequestBody RouteUpdateRequest request) {
        log.info("更新工艺路线: id={}", id);
        request.setId(id);
        routeService.updateRoute(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除工艺路线")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRoute(
            @Parameter(description = "工艺路线ID") @PathVariable Long id) {
        log.info("删除工艺路线: id={}", id);
        routeService.deleteRoute(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量删除工艺路线")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteRoutes(@RequestBody List<Long> ids) {
        log.info("批量删除工艺路线: count={}", ids.size());
        routeService.batchDeleteRoutes(ids);
        return ApiResponse.success();
    }

    @Operation(summary = "根据车型ID查询工艺路线列表")
    @GetMapping("/model/{modelId}")
    public ApiResponse<PageResult<RouteDTO>> listRoutesByModelId(
            @Parameter(description = "车型ID") @PathVariable Long modelId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("根据车型查询工艺路线列表: modelId={}", modelId);
        PageResult<RouteDTO> result = routeService.listRoutesByModelId(modelId, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @Operation(summary = "复制工艺路线（创建新版本）")
    @PostMapping("/{id}/copy")
    public ApiResponse<Long> copyRoute(
            @Parameter(description = "源工艺路线ID") @PathVariable Long id,
            @Parameter(description = "新版本号") @RequestParam String newVersion) {
        log.info("复制工艺路线: id={}, newVersion={}", id, newVersion);
        Long newRouteId = routeService.copyRoute(id, newVersion);
        return ApiResponse.success(newRouteId);
    }

    @Operation(summary = "启用/禁用工艺路线")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "工艺路线ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("更新工艺路线状态: id={}, status={}", id, status);
        routeService.updateStatus(id, status);
        return ApiResponse.success();
    }
}
