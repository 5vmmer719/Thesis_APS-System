package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.StationCreateRequest;
import com.aps.dto.request.masterdata.StationQueryRequest;
import com.aps.dto.request.masterdata.StationUpdateRequest;
import com.aps.dto.response.masterdata.StationDTO;
import com.aps.masterdata.StationService;
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
 * 工位管理控制器
 */
@Slf4j
@Tag(name = "工位管理", description = "工位的增删改查接口")
@RestController
@RequestMapping("/api/masterdata/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @Operation(summary = "分页查询工位列表")
    @GetMapping
    public ApiResponse<PageResult<StationDTO>> listStations(@ModelAttribute StationQueryRequest request) {
        log.info("查询工位列表: lineId={}, stationCode={}, stationName={}",
                request.getLineId(), request.getStationCode(), request.getStationName());
        PageResult<StationDTO> result = stationService.listStations(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取工位详情")
    @GetMapping("/{id}")
    public ApiResponse<StationDTO> getStationById(
            @Parameter(description = "工位ID") @PathVariable Long id) {
        log.info("查询工位详情: id={}", id);
        StationDTO station = stationService.getStationById(id);
        return ApiResponse.success(station);
    }

    @Operation(summary = "创建工位")
    @PostMapping
    public ApiResponse<Long> createStation(@Validated @RequestBody StationCreateRequest request) {
        log.info("创建工位: stationCode={}, stationName={}, lineId={}",
                request.getStationCode(), request.getStationName(), request.getLineId());
        Long stationId = stationService.createStation(request);
        return ApiResponse.success(stationId);
    }

    @Operation(summary = "更新工位")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateStation(
            @Parameter(description = "工位ID") @PathVariable Long id,
            @Validated @RequestBody StationUpdateRequest request) {
        log.info("更新工位: id={}", id);
        request.setId(id);
        stationService.updateStation(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除工位")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStation(
            @Parameter(description = "工位ID") @PathVariable Long id) {
        log.info("删除工位: id={}", id);
        stationService.deleteStation(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量删除工位")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteStations(@RequestBody List<Long> ids) {
        log.info("批量删除工位: count={}", ids.size());
        stationService.batchDeleteStations(ids);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用工位")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "工位ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("更新工位状态: id={}, status={}", id, status);
        stationService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @Operation(summary = "根据产线ID查询工位列表")
    @GetMapping("/line/{lineId}")
    public ApiResponse<List<StationDTO>> listStationsByLineId(
            @Parameter(description = "产线ID") @PathVariable Long lineId) {
        log.info("根据产线ID查询工位列表: lineId={}", lineId);
        List<StationDTO> stations = stationService.listStationsByLineId(lineId);
        return ApiResponse.success(stations);
    }

    @Operation(summary = "查询所有启用的工位（用于下拉选择）")
    @GetMapping("/active")
    public ApiResponse<List<StationDTO>> listAllActiveStations() {
        log.info("查询所有启用的工位");
        List<StationDTO> stations = stationService.listAllActiveStations();
        return ApiResponse.success(stations);
    }
}
