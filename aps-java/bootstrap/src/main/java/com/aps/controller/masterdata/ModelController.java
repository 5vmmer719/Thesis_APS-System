package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.ModelCreateRequest;
import com.aps.dto.request.masterdata.ModelQueryRequest;
import com.aps.dto.request.masterdata.ModelUpdateRequest;
import com.aps.dto.response.masterdata.ModelDTO;
import com.aps.masterdata.ModelService;
import com.aps.response.ApiResponse;
import com.aps.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 车型管理控制器
 */
@Slf4j
@Tag(name = "车型管理")
@RestController
@RequestMapping("/md/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @Operation(summary = "分页查询车型列表")
    @GetMapping
    public ApiResponse<PageResult<ModelDTO>> listModels(@ModelAttribute ModelQueryRequest request) {
        // 添加日志查看接收到的参数
        log.info("Controller 接收到的参数: keyword={}, platform={}, status={}",
                request.getKeyword(), request.getPlatform(), request.getStatus());
        PageResult<ModelDTO> result = modelService.listModels(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取车型详情")
    @GetMapping("/{id}")
    public ApiResponse<ModelDTO> getModelById(@PathVariable Long id) {
        ModelDTO model = modelService.getModelById(id);
        return ApiResponse.success(model);
    }

    @Operation(summary = "创建车型")
    @PostMapping
    public ApiResponse<Long> createModel(@Validated @RequestBody ModelCreateRequest request) {
        Long modelId = modelService.createModel(request);
        return ApiResponse.success(modelId);
    }

    @Operation(summary = "更新车型")
    @PatchMapping("/{id}")
    public ApiResponse<Void> updateModel(@PathVariable Long id,
                                         @Validated @RequestBody ModelUpdateRequest request) {
        request.setId(id);
        modelService.updateModel(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除车型")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteModel(@PathVariable Long id) {
        modelService.deleteModel(id);
        return ApiResponse.success();
    }
}
