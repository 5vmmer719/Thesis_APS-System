// bootstrap/src/main/java/com/aps/controller/execution/WorkOrderController.java
package com.aps.controller.execution;

import com.aps.dto.request.execution.WorkOrderActionRequest;
import com.aps.dto.request.execution.WorkOrderQueryRequest;
import com.aps.dto.request.execution.WorkOrderReportRequest;
import com.aps.dto.response.execution.WorkOrderDTO;
import com.aps.execution.WorkOrderService;
import com.aps.response.ApiResponse;
import com.aps.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 工单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/work-orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "工单管理", description = "工单的查询、下达、执行、报工等操作")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    /**
     * 分页查询工单列表
     * GET /api/v1/work-orders
     */
    @GetMapping
    @Operation(summary = "分页查询工单列表", description = "根据条件分页查询工单")
    public ApiResponse<PageResult<WorkOrderDTO>> list(@Valid WorkOrderQueryRequest request) {
        log.info("接收查询工单列表请求, request={}", request);
        PageResult<WorkOrderDTO> result = workOrderService.queryWorkOrders(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取工单详情
     * GET /api/v1/work-orders/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取工单详情", description = "根据ID获取工单详细信息")
    public ApiResponse<WorkOrderDTO> getById(
            @Parameter(description = "工单ID", required = true)
            @PathVariable @NotNull Long id) {
        log.info("接收获取工单详情请求, id={}", id);
        WorkOrderDTO dto = workOrderService.getWorkOrderDetail(id);
        return ApiResponse.success(dto);
    }

    /**
     * 下达工单
     * POST /api/v1/work-orders/{id}/release
     */
    @PostMapping("/{id}/release")
    @Operation(summary = "下达工单", description = "下达工单（状态：待下达 → 已下达）")
    public ApiResponse<Void> release(
            @Parameter(description = "工单ID", required = true)
            @PathVariable @NotNull Long id,
            @RequestBody(required = false) WorkOrderActionRequest request) {
        log.info("接收下达工单请求, id={}, request={}", id, request);
        workOrderService.releaseWorkOrder(id, request);
        return ApiResponse.success();
    }

    /**
     * 开始工单
     * POST /api/v1/work-orders/{id}/start
     */
    @PostMapping("/{id}/start")
    @Operation(summary = "开始工单", description = "开始工单（状态：已下达 → 执行中）")
    public ApiResponse<Void> start(
            @Parameter(description = "工单ID", required = true)
            @PathVariable @NotNull Long id,
            @RequestBody(required = false) WorkOrderActionRequest request) {
        log.info("接收开始工单请求, id={}, request={}", id, request);
        workOrderService.startWorkOrder(id, request);
        return ApiResponse.success();
    }

    /**
     * 暂停工单
     * POST /api/v1/work-orders/{id}/pause
     */
    @PostMapping("/{id}/pause")
    @Operation(summary = "暂停工单", description = "暂停工单（状态：执行中 → 暂停）")
    public ApiResponse<Void> pause(
            @Parameter(description = "工单ID", required = true)
            @PathVariable @NotNull Long id,
            @RequestBody(required = false) WorkOrderActionRequest request) {
        log.info("接收暂停工单请求, id={}, request={}", id, request);
        workOrderService.pauseWorkOrder(id, request);
        return ApiResponse.success();
    }

    /**
     * 恢复工单
     * POST /api/v1/work-orders/{id}/resume
     */
    @PostMapping("/{id}/resume")
    @Operation(summary = "恢复工单", description = "恢复工单（状态：暂停 → 执行中）")
    public ApiResponse<Void> resume(
            @Parameter(description = "工单ID", required = true)
            @PathVariable @NotNull Long id,
            @RequestBody(required = false) WorkOrderActionRequest request) {
        log.info("接收恢复工单请求, id={}, request={}", id, request);
        workOrderService.resumeWorkOrder(id, request);
        return ApiResponse.success();
    }

    /**
     * 完成工单
     * POST /api/v1/work-orders/{id}/complete
     */
    @PostMapping("/{id}/complete")
    @Operation(summary = "完成工单", description = "完成工单（状态：执行中 → 完成）")
    public ApiResponse<Void> complete(
            @Parameter(description = "工单ID", required = true)
            @PathVariable @NotNull Long id,
            @RequestBody(required = false) WorkOrderActionRequest request) {
        log.info("接收完成工单请求, id={}, request={}", id, request);
        workOrderService.completeWorkOrder(id, request);
        return ApiResponse.success();
    }

    /**
     * 作废工单
     * POST /api/v1/work-orders/{id}/void
     */
    @PostMapping("/{id}/void")
    @Operation(summary = "作废工单", description = "作废工单（管理员操作）")
    public ApiResponse<Void> voidWorkOrder(
            @Parameter(description = "工单ID", required = true)
            @PathVariable @NotNull Long id,
            @RequestBody @Valid VoidRequest request) {
        log.info("接收作废工单请求, id={}, reason={}", id, request.getReason());
        workOrderService.voidWorkOrder(id, request.getReason());
        return ApiResponse.success();
    }

    /**
     * 工单报工
     * POST /api/v1/work-orders/{id}/report
     */
    @PostMapping("/{id}/report")
    @Operation(summary = "工单报工", description = "工单报工（记录良品/不良品数量）")
    public ApiResponse<Void> report(
            @Parameter(description = "工单ID", required = true)
            @PathVariable @NotNull Long id,
            @RequestBody @Valid WorkOrderReportRequest request) {
        log.info("接收工单报工请求, id={}, qtyGood={}, qtyBad={}",
                id, request.getQtyGood(), request.getQtyBad());
        workOrderService.reportWorkOrder(id, request);
        return ApiResponse.success();
    }

    /**
     * 作废请求内部类
     */
    @lombok.Data
    public static class VoidRequest {
        @NotBlank(message = "作废原因不能为空")
        @Parameter(description = "作废原因", required = true)
        private String reason;
    }
}
