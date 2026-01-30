package com.aps.controller.masterdata;



import com.aps.dto.request.masterdata.StationGroupCreateRequest;
import com.aps.dto.request.masterdata.StationGroupQueryRequest;
import com.aps.dto.request.masterdata.StationGroupUpdateRequest;
import com.aps.dto.response.masterdata.StationGroupDTO;
import com.aps.masterdata.StationGroupService;
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
 * 工位组管理控制器
 */
@Slf4j
@Tag(name = "工位组管理", description = "工位组的增删改查接口")
@RestController
@RequestMapping("/api/masterdata/station-groups")
@RequiredArgsConstructor
public class StationGroupController {

    private final StationGroupService stationGroupService;

    @Operation(summary = "分页查询工位组列表")
    @GetMapping
    public ApiResponse<PageResult<StationGroupDTO>> listStationGroups(@ModelAttribute StationGroupQueryRequest request) {
        log.info("查询工位组列表: groupCode={}, groupName={}",
                request.getGroupCode(), request.getGroupName());
        PageResult<StationGroupDTO> result = stationGroupService.listStationGroups(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取工位组详情（包含工位列表）")
    @GetMapping("/{id}")
    public ApiResponse<StationGroupDTO> getStationGroupById(
            @Parameter(description = "工位组ID") @PathVariable Long id) {
        log.info("查询工位组详情: id={}", id);
        StationGroupDTO stationGroup = stationGroupService.getStationGroupById(id);
        return ApiResponse.success(stationGroup);
    }

    @Operation(summary = "创建工位组")
    @PostMapping
    public ApiResponse<Long> createStationGroup(@Validated @RequestBody StationGroupCreateRequest request) {
        log.info("创建工位组: groupCode={}, groupName={}",
                request.getGroupCode(), request.getGroupName());
        Long groupId = stationGroupService.createStationGroup(request);
        return ApiResponse.success(groupId);
    }

    @Operation(summary = "更新工位组")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateStationGroup(
            @Parameter(description = "工位组ID") @PathVariable Long id,
            @Validated @RequestBody StationGroupUpdateRequest request) {
        log.info("更新工位组: id={}", id);
        request.setId(id);
        stationGroupService.updateStationGroup(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除工位组")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStationGroup(
            @Parameter(description = "工位组ID") @PathVariable Long id) {
        log.info("删除工位组: id={}", id);
        stationGroupService.deleteStationGroup(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量删除工位组")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteStationGroups(@RequestBody List<Long> ids) {
        log.info("批量删除工位组: count={}", ids.size());
        stationGroupService.batchDeleteStationGroups(ids);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用工位组")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "工位组ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("更新工位组状态: id={}, status={}", id, status);
        stationGroupService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @Operation(summary = "查询所有启用的工位组（用于下拉选择）")
    @GetMapping("/active")
    public ApiResponse<List<StationGroupDTO>> listAllActiveStationGroups() {
        log.info("查询所有启用的工位组");
        List<StationGroupDTO> groups = stationGroupService.listAllActiveStationGroups();
        return ApiResponse.success(groups);
    }

    @Operation(summary = "为工位组添加工位")
    @PostMapping("/{groupId}/stations")
    public ApiResponse<Void> addStationsToGroup(
            @Parameter(description = "工位组ID") @PathVariable Long groupId,
            @RequestBody List<Long> stationIds) {
        log.info("为工位组添加工位: groupId={}, stationIds={}", groupId, stationIds);
        stationGroupService.addStationsToGroup(groupId, stationIds);
        return ApiResponse.success();
    }

    @Operation(summary = "从工位组移除工位")
    @DeleteMapping("/{groupId}/stations")
    public ApiResponse<Void> removeStationsFromGroup(
            @Parameter(description = "工位组ID") @PathVariable Long groupId,
            @RequestBody List<Long> stationIds) {
        log.info("从工位组移除工位: groupId={}, stationIds={}", groupId, stationIds);
        stationGroupService.removeStationsFromGroup(groupId, stationIds);
        return ApiResponse.success();
    }
}
