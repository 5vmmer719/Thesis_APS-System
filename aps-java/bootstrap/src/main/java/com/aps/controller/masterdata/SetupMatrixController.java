package com.aps.controller.masterdata;

import com.aps.dto.request.masterdata.SetupMatrixBatchImportRequest;
import com.aps.dto.request.masterdata.SetupMatrixCreateRequest;
import com.aps.dto.request.masterdata.SetupMatrixQueryRequest;
import com.aps.dto.request.masterdata.SetupMatrixUpdateRequest;
import com.aps.dto.response.masterdata.SetupMatrixDTO;
import com.aps.masterdata.SetupMatrixService;
import com.aps.response.ApiResponse;
import com.aps.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 换型矩阵管理控制器
 *
 * @author APS System
 * @since 2024-01-30
 */
@Slf4j
@Tag(name = "换型矩阵管理")
@RestController
@RequestMapping("/md/setup-matrices")
@RequiredArgsConstructor
public class SetupMatrixController {

    private final SetupMatrixService setupMatrixService;

    @Operation(summary = "分页查询换型矩阵列表")
    @GetMapping
    public ApiResponse<PageResult<SetupMatrixDTO>> listSetupMatrices(@ModelAttribute SetupMatrixQueryRequest request) {
        log.info("查询换型矩阵列表: keyword={}, processType={}, status={}",
                request.getKeyword(), request.getProcessType(), request.getStatus());
        PageResult<SetupMatrixDTO> result = setupMatrixService.listSetupMatrices(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取换型矩阵详情")
    @GetMapping("/{id}")
    public ApiResponse<SetupMatrixDTO> getSetupMatrixById(@PathVariable Long id) {
        SetupMatrixDTO setupMatrix = setupMatrixService.getSetupMatrixById(id);
        return ApiResponse.success(setupMatrix);
    }

    @Operation(summary = "创建换型矩阵")
    @PostMapping
    public ApiResponse<Long> createSetupMatrix(@Validated @RequestBody SetupMatrixCreateRequest request) {
        Long id = setupMatrixService.createSetupMatrix(request);
        return ApiResponse.success(id);
    }

    @Operation(summary = "更新换型矩阵")
    @PatchMapping("/{id}")
    public ApiResponse<Void> updateSetupMatrix(@PathVariable Long id,
                                               @Validated @RequestBody SetupMatrixUpdateRequest request) {
        request.setId(id);
        setupMatrixService.updateSetupMatrix(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除换型矩阵")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSetupMatrix(@PathVariable Long id) {
        setupMatrixService.deleteSetupMatrix(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量导入换型矩阵", description = "支持REPLACE（替换）和APPEND（追加）两种模式")
    @PostMapping("/batch-import")
    public ApiResponse<Void> batchImportSetupMatrix(@Validated @RequestBody SetupMatrixBatchImportRequest request) {
        log.info("批量导入换型矩阵: processType={}, mode={}, itemCount={}",
                request.getProcessType(), request.getMode(), request.getItems().size());
        setupMatrixService.batchImportSetupMatrix(request);
        return ApiResponse.success();
    }

    @Operation(summary = "获取指定工艺的所有启用换型矩阵", description = "供前端展示换型矩阵表格使用")
    @GetMapping("/process/{processType}")
    public ApiResponse<List<SetupMatrixDTO>> getActiveSetupMatricesByProcess(@PathVariable Integer processType) {
        List<SetupMatrixDTO> result = setupMatrixService.getActiveSetupMatricesByProcess(processType);
        return ApiResponse.success(result);
    }
}

