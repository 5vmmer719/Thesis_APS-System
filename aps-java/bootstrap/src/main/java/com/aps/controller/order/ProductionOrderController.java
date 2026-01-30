package com.aps.controller.order;


import com.aps.dto.request.order.*;
import com.aps.dto.response.order.ProdOrderDTO;
import com.aps.order.ProductionOrderService;
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
 * 生产订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/prod-orders")
@RequiredArgsConstructor
@Tag(name = "生产订单管理", description = "生产订单的创建、查询、审批、属性设置等操作")
public class ProductionOrderController {

    private final ProductionOrderService productionOrderService;

    /**
     * 创建生产订单
     */
    @PostMapping
    @Operation(summary = "创建生产订单", description = "创建新的生产订单")
    public ApiResponse<Long> create(@Valid @RequestBody ProdOrderCreateRequest request) {
        log.info("接收创建生产订单请求, prodNo={}", request.getProdNo());
        Long id = productionOrderService.create(request);
        return ApiResponse.success(id);
    }

    /**
     * 分页查询生产订单
     */
    @GetMapping
    @Operation(summary = "分页查询生产订单", description = "根据条件分页查询生产订单列表")
    public ApiResponse<PageResult<ProdOrderDTO>> list(@Valid ProdOrderQueryRequest request) {
        log.info("接收查询生产订单请求, request={}", request);
        PageResult<ProdOrderDTO> result = productionOrderService.list(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取生产订单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取生产订单详情", description = "根据ID获取生产订单详细信息（含属性）")
    public ApiResponse<ProdOrderDTO> getById(
            @Parameter(description = "生产订单ID", required = true)
            @PathVariable Long id) {
        log.info("接收获取生产订单详情请求, id={}", id);
        ProdOrderDTO dto = productionOrderService.getById(id);
        return ApiResponse.success(dto);
    }

    /**
     * 更新生产订单
     */
    @PatchMapping("/{id}")
    @Operation(summary = "更新生产订单", description = "更新生产订单基本信息（仅草稿/待审批状态）")
    public ApiResponse<Void> update(
            @Parameter(description = "生产订单ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProdOrderUpdateRequest request) {
        log.info("接收更新生产订单请求, id={}, request={}", id, request);
        productionOrderService.update(id, request);
        return ApiResponse.success();
    }

    /**
     * 设置订单属性
     */
    @PostMapping("/{id}/attrs:set")
    @Operation(summary = "设置订单属性", description = "设置生产订单的属性（颜色、内饰等）")
    public ApiResponse<Void> setAttrs(
            @Parameter(description = "生产订单ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProdOrderAttrsSetRequest request) {
        log.info("接收设置订单属性请求, id={}, attrCount={}", id, request.getAttrs().size());
        productionOrderService.setAttrs(id, request);
        return ApiResponse.success();
    }

    /**
     * 提交审批
     */
    @PostMapping("/{id}/submit-approve")
    @Operation(summary = "提交审批", description = "提交生产订单审批")
    public ApiResponse<Void> submitApprove(
            @Parameter(description = "生产订单ID", required = true)
            @PathVariable Long id) {
        log.info("接收提交审批请求, id={}", id);
        productionOrderService.submitApprove(id);
        return ApiResponse.success();
    }

    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过", description = "审批通过生产订单（管理员）")
    public ApiResponse<Void> approve(
            @Parameter(description = "生产订单ID", required = true)
            @PathVariable Long id) {
        log.info("接收审批通过请求, id={}", id);
        productionOrderService.approve(id);
        return ApiResponse.success();
    }

    /**
     * 审批驳回
     */
    @PostMapping("/{id}/reject")
    @Operation(summary = "审批驳回", description = "驳回生产订单（管理员）")
    public ApiResponse<Void> reject(
            @Parameter(description = "生产订单ID", required = true)
            @PathVariable Long id,
            @RequestBody(required = false) CommentRequest request) {
        String comment = request != null ? request.getComment() : null;
        log.info("接收审批驳回请求, id={}, comment={}", id, comment);
        productionOrderService.reject(id, comment);
        return ApiResponse.success();
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消生产订单")
    public ApiResponse<Void> cancel(
            @Parameter(description = "生产订单ID", required = true)
            @PathVariable Long id) {
        log.info("接收取消订单请求, id={}", id);
        productionOrderService.cancel(id);
        return ApiResponse.success();
    }
}
