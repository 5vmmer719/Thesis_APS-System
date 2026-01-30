package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.BomCreateRequest;
import com.aps.dto.request.masterdata.BomQueryRequest;
import com.aps.dto.request.masterdata.BomUpdateRequest;
import com.aps.dto.response.masterdata.BomDTO;
import com.aps.dto.response.masterdata.BomDetailDTO;
import com.aps.masterdata.BomService;
import com.aps.response.ApiResponse;
import com.aps.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * BOM管理控制器
 */
@Slf4j
@Tag(name = "BOM管理")
@RestController
@RequestMapping("/md/boms")
@RequiredArgsConstructor
public class BomController {

    private final BomService bomService;

    @Operation(summary = "分页查询BOM列表")
    @GetMapping
    public ApiResponse<PageResult<BomDTO>> listBoms(@ModelAttribute BomQueryRequest request) {
        log.info("查询BOM列表: keyword={}, modelId={}, version={}, status={}",
                request.getKeyword(), request.getModelId(), request.getVersion(), request.getStatus());
        PageResult<BomDTO> result = bomService.listBoms(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取BOM详情（包含明细）")
    @GetMapping("/{id}/detail")
    public ApiResponse<BomDetailDTO> getBomDetailById(
            @Parameter(description = "BOM ID") @PathVariable Long id) {
        log.info("查询BOM详情: id={}", id);
        BomDetailDTO detail = bomService.getBomDetailById(id);
        return ApiResponse.success(detail);
    }

    @Operation(summary = "根据ID获取BOM基本信息")
    @GetMapping("/{id}")
    public ApiResponse<BomDTO> getBomById(
            @Parameter(description = "BOM ID") @PathVariable Long id) {
        log.info("查询BOM基本信息: id={}", id);
        BomDTO bom = bomService.getBomById(id);
        return ApiResponse.success(bom);
    }

    @Operation(summary = "创建BOM")
    @PostMapping
    public ApiResponse<Long> createBom(@Validated @RequestBody BomCreateRequest request) {
        Long bomId = bomService.createBom(request);
        return ApiResponse.success(bomId);
    }

    @Operation(summary = "更新BOM")
    @PatchMapping("/{id}")
    public ApiResponse<Void> updateBom(
            @Parameter(description = "BOM ID") @PathVariable Long id,
            @Validated @RequestBody BomUpdateRequest request) {
        log.info("更新BOM: id={}", id);
        request.setId(id);
        bomService.updateBom(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除BOM")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBom(
            @Parameter(description = "BOM ID") @PathVariable Long id) {
        log.info("删除BOM: id={}", id);
        bomService.deleteBom(id);
        return ApiResponse.success();
    }

    @Operation(summary = "根据车型ID查询BOM列表")
    @GetMapping("/model/{modelId}")
    public ApiResponse<PageResult<BomDTO>> listBomsByModelId(
            @Parameter(description = "车型ID") @PathVariable Long modelId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("根据车型查询BOM列表: modelId={}", modelId);
        PageResult<BomDTO> result = bomService.listBomsByModelId(modelId, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @Operation(summary = "复制BOM（创建新版本）")
    @PostMapping("/{id}/copy")
    public ApiResponse<Long> copyBom(
            @Parameter(description = "源BOM ID") @PathVariable Long id,
            @Parameter(description = "新版本号") @RequestParam String newVersion) {
        log.info("复制BOM: id={}, newVersion={}", id, newVersion);
        Long newBomId = bomService.copyBom(id, newVersion);
        return ApiResponse.success(newBomId);
    }
}
