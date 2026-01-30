package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.LaborGroupCreateRequest;
import com.aps.dto.request.masterdata.LaborGroupQueryRequest;
import com.aps.dto.response.masterdata.LaborGroupDTO;
import com.aps.masterdata.LaborGroupService;
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
 * 人力组管理控制器
 */
@Slf4j
@Tag(name = "人力组管理", description = "人力组的增删改查接口")
@RestController
@RequestMapping("/api/masterdata/labor-groups")
@RequiredArgsConstructor
public class LaborGroupController {

    private final LaborGroupService laborGroupService;

    @Operation(summary = "分页查询人力组列表")
    @GetMapping
    public ApiResponse<PageResult<LaborGroupDTO>> listLaborGroups(@ModelAttribute LaborGroupQueryRequest request) {
        log.info("查询人力组列表: laborCode={}, laborName={}",
                request.getLaborCode(), request.getLaborName());
        PageResult<LaborGroupDTO> result = laborGroupService.listLaborGroups(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取人力组详情（包含关联工位）")
    @GetMapping("/{id}")
    public ApiResponse<LaborGroupDTO> getLaborGroupById(
            @Parameter(description = "人力组ID") @PathVariable Long id) {
        log.info("查询人力组详情: id={}", id);
        LaborGroupDTO laborGroup = laborGroupService.getLaborGroupById(id);
        return ApiResponse.success(laborGroup);
    }

    @Operation(summary = "创建人力组")
    @PostMapping
    public ApiResponse<Long> createLaborGroup(@Validated @RequestBody LaborGroupCreateRequest request) {
        log.info("创建人力组: laborCode={}, laborName={}, headcount={}",
                request.getLaborCode(), request.getLaborName(), request.getHeadcount());
        Long laborGroupId = laborGroupService.createLaborGroup(request);
        return ApiResponse.success(laborGroupId);
    }

    @Operation(summary = "更新人力组")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateLaborGroup(
            @Parameter(description = "人力组ID") @PathVariable Long id,
            @Validated @RequestBody LaborGroupCreateRequest request) {
        log.info("更新人力组: id={}", id);
        laborGroupService.updateLaborGroup(id, request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除人力组")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLaborGroup(
            @Parameter(description = "人力组ID") @PathVariable Long id) {
        log.info("删除人力组: id={}", id);
        laborGroupService.deleteLaborGroup(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量删除人力组")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteLaborGroups(@RequestBody List<Long> ids) {
        log.info("批量删除人力组: count={}", ids.size());
        laborGroupService.batchDeleteLaborGroups(ids);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用人力组")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "人力组ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("更新人力组状态: id={}, status={}", id, status);
        laborGroupService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @Operation(summary = "查询所有启用的人力组（用于下拉选择）")
    @GetMapping("/active")
    public ApiResponse<List<LaborGroupDTO>> listAllActiveLaborGroups() {
        log.info("查询所有启用的人力组");
        List<LaborGroupDTO> laborGroups = laborGroupService.listAllActiveLaborGroups();
        return ApiResponse.success(laborGroups);
    }

    @Operation(summary = "为人力组添加工位")
    @PostMapping("/{laborId}/stations")
    public ApiResponse<Void> addStationsToLaborGroup(
            @Parameter(description = "人力组ID") @PathVariable Long laborId,
            @RequestBody List<Long> stationIds) {
        log.info("为人力组添加工位: laborId={}, stationIds={}", laborId, stationIds);
        laborGroupService.addStationsToLaborGroup(laborId, stationIds);
        return ApiResponse.success();
    }

    @Operation(summary = "从人力组移除工位")
    @DeleteMapping("/{laborId}/stations")
    public ApiResponse<Void> removeStationsFromLaborGroup(
            @Parameter(description = "人力组ID") @PathVariable Long laborId,
            @RequestBody List<Long> stationIds) {
        log.info("从人力组移除工位: laborId={}, stationIds={}", laborId, stationIds);
        laborGroupService.removeStationsFromLaborGroup(laborId, stationIds);
        return ApiResponse.success();
    }
}
