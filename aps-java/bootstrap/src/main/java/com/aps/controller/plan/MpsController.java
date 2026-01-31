package com.aps.controller.plan;

import com.aps.dto.request.plan.*;
import com.aps.dto.response.plan.MpsDTO;
import com.aps.dto.response.plan.MpsItemDTO;
import com.aps.plan.MpsService;
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
 * MPS主生产计划控制器
 *
 * @author APS System
 * @since 2024-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/plan/mps")
@RequiredArgsConstructor
@Tag(name = "Plan-MPS", description = "MPS主生产计划管理")
public class MpsController {

    private final MpsService mpsService;

    @PostMapping
    @Operation(summary = "创建MPS")
    public ApiResponse<Long> create(@Validated @RequestBody MpsCreateRequest request) {
        Long id = mpsService.create(request);
        return ApiResponse.success(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "更新MPS")
    public ApiResponse<Void> update(
            @Parameter(description = "MPS ID") @PathVariable Long id,
            @Validated @RequestBody MpsUpdateRequest request) {
        mpsService.update(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除MPS")
    public ApiResponse<Void> delete(@Parameter(description = "MPS ID") @PathVariable Long id) {
        mpsService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取MPS详情")
    public ApiResponse<MpsDTO> getById(@Parameter(description = "MPS ID") @PathVariable Long id) {
        MpsDTO dto = mpsService.getById(id);
        return ApiResponse.success(dto);
    }

    @GetMapping
    @Operation(summary = "分页查询MPS列表")
    public ApiResponse<PageResult<MpsDTO>> page(@Validated MpsQueryRequest request) {
        PageResult<MpsDTO> result = mpsService.page(request);
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "设置MPS明细")
    public ApiResponse<Void> setItems(
            @Parameter(description = "MPS ID") @PathVariable Long id,
            @Validated @RequestBody MpsItemSetRequest request) {
        mpsService.setItems(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "查询MPS明细列表")
    public ApiResponse<List<MpsItemDTO>> listItems(@Parameter(description = "MPS ID") @PathVariable Long id) {
        List<MpsItemDTO> items = mpsService.listItems(id);
        return ApiResponse.success(items);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "删除MPS明细")
    public ApiResponse<Void> deleteItem(
            @Parameter(description = "MPS ID") @PathVariable Long id,
            @Parameter(description = "明细ID") @PathVariable Long itemId) {
        mpsService.deleteItem(id, itemId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/submit-approval")
    @Operation(summary = "提交MPS审批")
    public ApiResponse<Void> submitApproval(@Parameter(description = "MPS ID") @PathVariable Long id) {
        mpsService.submitApproval(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "批准MPS")
    public ApiResponse<Void> approve(@Parameter(description = "MPS ID") @PathVariable Long id) {
        mpsService.approve(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "关闭MPS")
    public ApiResponse<Void> close(@Parameter(description = "MPS ID") @PathVariable Long id) {
        mpsService.close(id);
        return ApiResponse.success();
    }
}

