package com.aps.controller.order;


import com.aps.dto.request.order.SalesOrderCreateRequest;
import com.aps.dto.request.order.SalesOrderQueryRequest;
import com.aps.dto.request.order.ToProdRequest;
import com.aps.dto.response.order.SalesOrderDTO;
import com.aps.order.SalesOrderService;
import com.aps.response.ApiResponse;
import com.aps.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 销售订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sales-orders")
@RequiredArgsConstructor
@Tag(name = "销售订单管理", description = "销售订单的创建、查询、审批、转生产等操作")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    /**
     * 创建销售订单
     */
    @PostMapping
    @Operation(summary = "创建销售订单", description = "创建新的销售订单")
    public ApiResponse<Long> create(@Valid @RequestBody SalesOrderCreateRequest request) {
        log.info("接收创建销售订单请求, salesNo={}", request.getSalesNo());
        Long id = salesOrderService.create(request);
        return ApiResponse.success(id);
    }

    /**
     * 分页查询销售订单
     */
    @GetMapping
    @Operation(summary = "分页查询销售订单", description = "根据条件分页查询销售订单列表")
    public ApiResponse<PageResult<SalesOrderDTO>> list(@Valid SalesOrderQueryRequest request) {
        log.info("接收查询销售订单请求, request={}", request);
        PageResult<SalesOrderDTO> result = salesOrderService.list(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取销售订单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取销售订单详情", description = "根据ID获取销售订单详细信息")
    public ApiResponse<SalesOrderDTO> getById(
            @Parameter(description = "销售订单ID", required = true)
            @PathVariable Long id) {
        log.info("接收获取销售订单详情请求, id={}", id);
        SalesOrderDTO dto = salesOrderService.getById(id);
        return ApiResponse.success(dto);
    }

    /**
     * 审批销售订单
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "审批销售订单", description = "审批通过销售订单")
    public ApiResponse<Void> approve(
            @Parameter(description = "销售订单ID", required = true)
            @PathVariable Long id) {
        log.info("接收审批销售订单请求, id={}", id);
        salesOrderService.approve(id);
        return ApiResponse.success();
    }

    /**
     * 转换为生产订单
     */
    @PostMapping("/{id}/to-prod")
    @Operation(summary = "转换为生产订单", description = "将销售订单转换为生产订单")
    public ApiResponse<Long> toProdOrder(
            @Parameter(description = "销售订单ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ToProdRequest request) {
        log.info("接收销售订单转生产订单请求, id={}, request={}", id, request);
        Long prodOrderId = salesOrderService.toProdOrder(id, request);
        return ApiResponse.success(prodOrderId);
    }

    /**
     * 取消销售订单
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消销售订单", description = "取消销售订单")
    public ApiResponse<Void> cancel(
            @Parameter(description = "销售订单ID", required = true)
            @PathVariable Long id) {
        log.info("接收取消销售订单请求, id={}", id);
        salesOrderService.cancel(id);
        return ApiResponse.success();
    }
}
