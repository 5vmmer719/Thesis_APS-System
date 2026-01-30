package com.aps.controller.masterdata;



import com.aps.dto.request.masterdata.WorkshopCreateRequest;
import com.aps.dto.request.masterdata.WorkshopQueryRequest;
import com.aps.dto.request.masterdata.WorkshopUpdateRequest;
import com.aps.dto.response.masterdata.WorkshopDTO;
import com.aps.masterdata.WorkshopService;
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
 * 车间管理控制器
 */
@Slf4j
@Tag(name = "车间管理", description = "车间的增删改查接口")
@RestController
@RequestMapping("/api/masterdata/workshops")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;

    @Operation(summary = "分页查询车间列表")
    @GetMapping
    public ApiResponse<PageResult<WorkshopDTO>> listWorkshops(@ModelAttribute WorkshopQueryRequest request) {
        log.info("查询车间列表: workshopCode={}, workshopName={}, processType={}",
                request.getWorkshopCode(), request.getWorkshopName(), request.getProcessType());
        PageResult<WorkshopDTO> result = workshopService.listWorkshops(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取车间详情")
    @GetMapping("/{id}")
    public ApiResponse<WorkshopDTO> getWorkshopById(
            @Parameter(description = "车间ID") @PathVariable Long id) {
        log.info("查询车间详情: id={}", id);
        WorkshopDTO workshop = workshopService.getWorkshopById(id);
        return ApiResponse.success(workshop);
    }

    @Operation(summary = "创建车间")
    @PostMapping
    public ApiResponse<Long> createWorkshop(@Validated @RequestBody WorkshopCreateRequest request) {
        log.info("创建车间: workshopCode={}, workshopName={}",
                request.getWorkshopCode(), request.getWorkshopName());
        Long workshopId = workshopService.createWorkshop(request);
        return ApiResponse.success(workshopId);
    }

    @Operation(summary = "更新车间")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateWorkshop(
            @Parameter(description = "车间ID") @PathVariable Long id,
            @Validated @RequestBody WorkshopUpdateRequest request) {
        log.info("更新车间: id={}", id);
        request.setId(id);
        workshopService.updateWorkshop(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除车间")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWorkshop(
            @Parameter(description = "车间ID") @PathVariable Long id) {
        log.info("删除车间: id={}", id);
        workshopService.deleteWorkshop(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量删除车间")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteWorkshops(@RequestBody List<Long> ids) {
        log.info("批量删除车间: count={}", ids.size());
        workshopService.batchDeleteWorkshops(ids);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用车间")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "车间ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("更新车间状态: id={}, status={}", id, status);
        workshopService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @Operation(summary = "查询所有启用的车间（用于下拉选择）")
    @GetMapping("/active")
    public ApiResponse<List<WorkshopDTO>> listAllActiveWorkshops() {
        log.info("查询所有启用的车间");
        List<WorkshopDTO> workshops = workshopService.listAllActiveWorkshops();
        return ApiResponse.success(workshops);
    }

    @Operation(summary = "根据工艺类型查询车间列表")
    @GetMapping("/process-type/{processType}")
    public ApiResponse<List<WorkshopDTO>> listWorkshopsByProcessType(
            @Parameter(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装") @PathVariable Integer processType) {
        log.info("根据工艺类型查询车间列表: processType={}", processType);
        List<WorkshopDTO> workshops = workshopService.listWorkshopsByProcessType(processType);
        return ApiResponse.success(workshops);
    }
}
