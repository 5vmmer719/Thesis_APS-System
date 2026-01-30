package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.LineCreateRequest;
import com.aps.dto.request.masterdata.LineQueryRequest;
import com.aps.dto.request.masterdata.LineUpdateRequest;
import com.aps.dto.response.masterdata.LineDTO;
import com.aps.masterdata.LineService;
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
 * 产线管理控制器
 */
@Slf4j
@Tag(name = "产线管理", description = "产线的增删改查接口")
@RestController
@RequestMapping("/api/masterdata/lines")
@RequiredArgsConstructor
public class LineController {

    private final LineService lineService;

    @Operation(summary = "分页查询产线列表")
    @GetMapping
    public ApiResponse<PageResult<LineDTO>> listLines(@ModelAttribute LineQueryRequest request) {
        log.info("查询产线列表: workshopId={}, lineCode={}, lineName={}",
                request.getWorkshopId(), request.getLineCode(), request.getLineName());
        PageResult<LineDTO> result = lineService.listLines(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取产线详情")
    @GetMapping("/{id}")
    public ApiResponse<LineDTO> getLineById(
            @Parameter(description = "产线ID") @PathVariable Long id) {
        log.info("查询产线详情: id={}", id);
        LineDTO line = lineService.getLineById(id);
        return ApiResponse.success(line);
    }

    @Operation(summary = "创建产线")
    @PostMapping
    public ApiResponse<Long> createLine(@Validated @RequestBody LineCreateRequest request) {
        log.info("创建产线: lineCode={}, lineName={}, workshopId={}",
                request.getLineCode(), request.getLineName(), request.getWorkshopId());
        Long lineId = lineService.createLine(request);
        return ApiResponse.success(lineId);
    }

    @Operation(summary = "更新产线")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateLine(
            @Parameter(description = "产线ID") @PathVariable Long id,
            @Validated @RequestBody LineUpdateRequest request) {
        log.info("更新产线: id={}", id);
        request.setId(id);
        lineService.updateLine(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除产线")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLine(
            @Parameter(description = "产线ID") @PathVariable Long id) {
        log.info("删除产线: id={}", id);
        lineService.deleteLine(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量删除产线")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteLines(@RequestBody List<Long> ids) {
        log.info("批量删除产线: count={}", ids.size());
        lineService.batchDeleteLines(ids);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用产线")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "产线ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("更新产线状态: id={}, status={}", id, status);
        lineService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @Operation(summary = "根据车间ID查询产线列表")
    @GetMapping("/workshop/{workshopId}")
    public ApiResponse<List<LineDTO>> listLinesByWorkshopId(
            @Parameter(description = "车间ID") @PathVariable Long workshopId) {
        log.info("根据车间ID查询产线列表: workshopId={}", workshopId);
        List<LineDTO> lines = lineService.listLinesByWorkshopId(workshopId);
        return ApiResponse.success(lines);
    }

    @Operation(summary = "根据工艺类型查询产线列表")
    @GetMapping("/process-type/{processType}")
    public ApiResponse<List<LineDTO>> listLinesByProcessType(
            @Parameter(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装") @PathVariable Integer processType) {
        log.info("根据工艺类型查询产线列表: processType={}", processType);
        List<LineDTO> lines = lineService.listLinesByProcessType(processType);
        return ApiResponse.success(lines);
    }

    @Operation(summary = "查询所有启用的产线（用于下拉选择）")
    @GetMapping("/active")
    public ApiResponse<List<LineDTO>> listAllActiveLines() {
        log.info("查询所有启用的产线");
        List<LineDTO> lines = lineService.listAllActiveLines();
        return ApiResponse.success(lines);
    }
}
