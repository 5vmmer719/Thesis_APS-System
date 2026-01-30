// bootstrap/src/main/java/com/aps/controller/execution/ExceptionController.java
package com.aps.controller.execution;

import com.aps.dto.request.execution.ExceptionCreateRequest;
import com.aps.dto.request.execution.ExceptionQueryRequest;
import com.aps.dto.request.order.CommentRequest;
import com.aps.dto.response.execution.ExceptionDTO;
import com.aps.execution.ExceptionService;
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
import jakarta.validation.constraints.NotNull;

/**
 * 异常控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/exceptions")
@RequiredArgsConstructor
@Validated
@Tag(name = "异常管理", description = "执行异常的创建、查询、处理、关闭等操作")
public class ExceptionController {

    private final ExceptionService exceptionService;

    /**
     * 创建异常
     * POST /api/v1/exceptions
     */
    @PostMapping
    @Operation(summary = "创建异常", description = "创建执行异常记录")
    public ApiResponse<Long> create(@RequestBody @Valid ExceptionCreateRequest request) {
        log.info("接收创建异常请求, woId={}, type={}, level={}",
                request.getWoId(), request.getType(), request.getLevel());
        Long exceptionId = exceptionService.createException(request);
        return ApiResponse.success(exceptionId);
    }

    /**
     * 分页查询异常列表
     * GET /api/v1/exceptions
     */
    @GetMapping
    @Operation(summary = "分页查询异常列表", description = "根据条件分页查询异常")
    public ApiResponse<PageResult<ExceptionDTO>> list(@Valid ExceptionQueryRequest request) {
        log.info("接收查询异常列表请求, request={}", request);
        PageResult<ExceptionDTO> result = exceptionService.queryExceptions(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取异常详情
     * GET /api/v1/exceptions/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取异常详情", description = "根据ID获取异常详细信息")
    public ApiResponse<ExceptionDTO> getById(
            @Parameter(description = "异常ID", required = true)
            @PathVariable @NotNull Long id) {
        log.info("接收获取异常详情请求, id={}", id);
        ExceptionDTO dto = exceptionService.getExceptionDetail(id);
        return ApiResponse.success(dto);
    }

    /**
     * 接单处理异常
     * POST /api/v1/exceptions/{id}/accept
     */
    @PostMapping("/{id}/accept")
    @Operation(summary = "接单处理异常", description = "接单处理异常（状态：新建 → 处理中）")
    public ApiResponse<Void> accept(
            @Parameter(description = "异常ID", required = true)
            @PathVariable @NotNull Long id) {
        log.info("接收接单处理异常请求, id={}", id);
        exceptionService.acceptException(id);
        return ApiResponse.success();
    }

    /**
     * 关闭异常
     * POST /api/v1/exceptions/{id}/close
     */
    @PostMapping("/{id}/close")
    @Operation(summary = "关闭异常", description = "关闭异常（状态：处理中 → 已关闭）")
    public ApiResponse<Void> close(
            @Parameter(description = "异常ID", required = true)
            @PathVariable @NotNull Long id,
            @RequestBody(required = false) CommentRequest request) {
        String comment = request != null ? request.getComment() : null;
        log.info("接收关闭异常请求, id={}, comment={}", id, comment);
        exceptionService.closeException(id, comment);
        return ApiResponse.success();
    }
}
