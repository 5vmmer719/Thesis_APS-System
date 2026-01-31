package com.aps.controller.plan;

import com.aps.dto.request.plan.MrpQueryRequest;
import com.aps.dto.request.plan.MrpRunRequest;
import com.aps.dto.response.plan.MrpDTO;
import com.aps.dto.response.plan.MrpItemDTO;
import com.aps.plan.MrpService;
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
 * MRP物料需求计划控制器
 *
 * @author APS System
 * @since 2024-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/plan/mrp")
@RequiredArgsConstructor
@Tag(name = "Plan-MRP", description = "MRP物料需求计划管理")
public class MrpController {

    private final MrpService mrpService;

    @PostMapping("/run")
    @Operation(summary = "运行MRP")
    public ApiResponse<Long> run(@Validated @RequestBody MrpRunRequest request) {
        Long id = mrpService.run(request);
        return ApiResponse.success(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除MRP")
    public ApiResponse<Void> delete(@Parameter(description = "MRP ID") @PathVariable Long id) {
        mrpService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取MRP详情")
    public ApiResponse<MrpDTO> getById(@Parameter(description = "MRP ID") @PathVariable Long id) {
        MrpDTO dto = mrpService.getById(id);
        return ApiResponse.success(dto);
    }

    @GetMapping
    @Operation(summary = "分页查询MRP列表")
    public ApiResponse<PageResult<MrpDTO>> page(@Validated MrpQueryRequest request) {
        PageResult<MrpDTO> result = mrpService.page(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "查询MRP明细列表")
    public ApiResponse<List<MrpItemDTO>> listItems(@Parameter(description = "MRP ID") @PathVariable Long id) {
        List<MrpItemDTO> items = mrpService.listItems(id);
        return ApiResponse.success(items);
    }
}

