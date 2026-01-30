package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.ToolingCreateRequest;
import com.aps.dto.request.masterdata.ToolingQueryRequest;
import com.aps.dto.response.masterdata.ToolingDTO;
import com.aps.masterdata.ToolingService;
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
 * 工装管理控制器
 */
@Slf4j
@Tag(name = "工装管理", description = "工装的增删改查接口")
@RestController
@RequestMapping("/api/masterdata/toolings")
@RequiredArgsConstructor
public class ToolingController {

    private final ToolingService toolingService;

    @Operation(summary = "分页查询工装列表")
    @GetMapping
    public ApiResponse<PageResult<ToolingDTO>> listToolings(@ModelAttribute ToolingQueryRequest request) {
        log.info("查询工装列表: toolingCode={}, toolingName={}, toolingType={}",
                request.getToolingCode(), request.getToolingName(), request.getToolingType());
        PageResult<ToolingDTO> result = toolingService.listToolings(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取工装详情")
    @GetMapping("/{id}")
    public ApiResponse<ToolingDTO> getToolingById(
            @Parameter(description = "工装ID") @PathVariable Long id) {
        log.info("查询工装详情: id={}", id);
        ToolingDTO tooling = toolingService.getToolingById(id);
        return ApiResponse.success(tooling);
    }

    @Operation(summary = "创建工装")
    @PostMapping
    public ApiResponse<Long> createTooling(@Validated @RequestBody ToolingCreateRequest request) {
        log.info("创建工装: toolingCode={}, toolingName={}, toolingType={}",
                request.getToolingCode(), request.getToolingName(), request.getToolingType());
        Long toolingId = toolingService.createTooling(request);
        return ApiResponse.success(toolingId);
    }

    @Operation(summary = "更新工装")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateTooling(
            @Parameter(description = "工装ID") @PathVariable Long id,
            @Validated @RequestBody ToolingCreateRequest request) {
        log.info("更新工装: id={}", id);
        toolingService.updateTooling(id, request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除工装")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTooling(
            @Parameter(description = "工装ID") @PathVariable Long id) {
        log.info("删除工装: id={}", id);
        toolingService.deleteTooling(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量删除工装")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteToolings(@RequestBody List<Long> ids) {
        log.info("批量删除工装: count={}", ids.size());
        toolingService.batchDeleteToolings(ids);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用工装")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "工装ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("更新工装状态: id={}, status={}", id, status);
        toolingService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @Operation(summary = "根据工装类型查询工装列表")
    @GetMapping("/type/{toolingType}")
    public ApiResponse<List<ToolingDTO>> listToolingsByType(
            @Parameter(description = "工装类型：1-模具，2-夹具，3-检具") @PathVariable Integer toolingType) {
        log.info("根据工装类型查询工装列表: toolingType={}", toolingType);
        List<ToolingDTO> toolings = toolingService.listToolingsByType(toolingType);
        return ApiResponse.success(toolings);
    }

    @Operation(summary = "查询所有启用的工装（用于下拉选择）")
    @GetMapping("/active")
    public ApiResponse<List<ToolingDTO>> listAllActiveToolings() {
        log.info("查询所有启用的工装");
        List<ToolingDTO> toolings = toolingService.listAllActiveToolings();
        return ApiResponse.success(toolings);
    }

    @Operation(summary = "绑定工装到资源（工位或设备）")
    @PostMapping("/{toolingId}/bind")
    public ApiResponse<Void> bindToolingToResource(
            @Parameter(description = "工装ID") @PathVariable Long toolingId,
            @Parameter(description = "资源类型：2-工位，3-设备") @RequestParam Integer resourceType,
            @Parameter(description = "资源ID") @RequestParam Long resourceId) {
        log.info("绑定工装到资源: toolingId={}, resourceType={}, resourceId={}",
                toolingId, resourceType, resourceId);
        toolingService.bindToolingToResource(toolingId, resourceType, resourceId);
        return ApiResponse.success();
    }

    @Operation(summary = "解绑工装与资源")
    @DeleteMapping("/{toolingId}/unbind")
    public ApiResponse<Void> unbindToolingFromResource(
            @Parameter(description = "工装ID") @PathVariable Long toolingId,
            @Parameter(description = "资源类型：2-工位，3-设备") @RequestParam Integer resourceType,
            @Parameter(description = "资源ID") @RequestParam Long resourceId) {
        log.info("解绑工装与资源: toolingId={}, resourceType={}, resourceId={}",
                toolingId, resourceType, resourceId);
        toolingService.unbindToolingFromResource(toolingId, resourceType, resourceId);
        return ApiResponse.success();
    }

    @Operation(summary = "查询资源绑定的工装列表")
    @GetMapping("/resource")
    public ApiResponse<List<ToolingDTO>> listToolingsByResource(
            @Parameter(description = "资源类型：2-工位，3-设备") @RequestParam Integer resourceType,
            @Parameter(description = "资源ID") @RequestParam Long resourceId) {
        log.info("查询资源绑定的工装列表: resourceType={}, resourceId={}", resourceType, resourceId);
        List<ToolingDTO> toolings = toolingService.listToolingsByResource(resourceType, resourceId);
        return ApiResponse.success(toolings);
    }
}
