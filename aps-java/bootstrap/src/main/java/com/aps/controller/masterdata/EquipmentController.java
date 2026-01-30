package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.EquipmentCreateRequest;
import com.aps.dto.request.masterdata.EquipmentQueryRequest;
import com.aps.dto.response.masterdata.EquipmentDTO;
import com.aps.masterdata.EquipmentService;
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
 * 设备管理控制器
 */
@Slf4j
@Tag(name = "设备管理", description = "设备的增删改查接口")
@RestController
@RequestMapping("/api/masterdata/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Operation(summary = "分页查询设备列表")
    @GetMapping
    public ApiResponse<PageResult<EquipmentDTO>> listEquipments(@ModelAttribute EquipmentQueryRequest request) {
        log.info("查询设备列表: stationId={}, equipCode={}, equipName={}",
                request.getStationId(), request.getEquipCode(), request.getEquipName());
        PageResult<EquipmentDTO> result = equipmentService.listEquipments(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取设备详情")
    @GetMapping("/{id}")
    public ApiResponse<EquipmentDTO> getEquipmentById(
            @Parameter(description = "设备ID") @PathVariable Long id) {
        log.info("查询设备详情: id={}", id);
        EquipmentDTO equipment = equipmentService.getEquipmentById(id);
        return ApiResponse.success(equipment);
    }

    @Operation(summary = "创建设备")
    @PostMapping
    public ApiResponse<Long> createEquipment(@Validated @RequestBody EquipmentCreateRequest request) {
        log.info("创建设备: equipCode={}, equipName={}, stationId={}",
                request.getEquipCode(), request.getEquipName(), request.getStationId());
        Long equipmentId = equipmentService.createEquipment(request);
        return ApiResponse.success(equipmentId);
    }

    @Operation(summary = "更新设备")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateEquipment(
            @Parameter(description = "设备ID") @PathVariable Long id,
            @Validated @RequestBody EquipmentCreateRequest request) {
        log.info("更新设备: id={}", id);
        equipmentService.updateEquipment(id, request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEquipment(
            @Parameter(description = "设备ID") @PathVariable Long id) {
        log.info("删除设备: id={}", id);
        equipmentService.deleteEquipment(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量删除设备")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteEquipments(@RequestBody List<Long> ids) {
        log.info("批量删除设备: count={}", ids.size());
        equipmentService.batchDeleteEquipments(ids);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用设备")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "设备ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("更新设备状态: id={}, status={}", id, status);
        equipmentService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @Operation(summary = "根据工位ID查询设备列表")
    @GetMapping("/station/{stationId}")
    public ApiResponse<List<EquipmentDTO>> listEquipmentsByStationId(
            @Parameter(description = "工位ID") @PathVariable Long stationId) {
        log.info("根据工位ID查询设备列表: stationId={}", stationId);
        List<EquipmentDTO> equipments = equipmentService.listEquipmentsByStationId(stationId);
        return ApiResponse.success(equipments);
    }

    @Operation(summary = "查询所有启用的设备（用于下拉选择）")
    @GetMapping("/active")
    public ApiResponse<List<EquipmentDTO>> listAllActiveEquipments() {
        log.info("查询所有启用的设备");
        List<EquipmentDTO> equipments = equipmentService.listAllActiveEquipments();
        return ApiResponse.success(equipments);
    }
}
