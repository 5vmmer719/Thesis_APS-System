package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.ItemCreateRequest;
import com.aps.dto.request.masterdata.ItemQueryRequest;
import com.aps.dto.request.masterdata.ItemUpdateRequest;
import com.aps.dto.response.masterdata.ItemDTO;
import com.aps.masterdata.ItemService;
import com.aps.response.ApiResponse;
import com.aps.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "物料管理")
@RestController
@RequestMapping("/md/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "分页查询物料列表")
    @GetMapping
    public ApiResponse<PageResult<ItemDTO>> listItems(@ModelAttribute ItemQueryRequest request) {
        // 添加日志查看接收到的参数
        log.info("Controller 接收到的参数: keyword={}, itemType={}, status={}",
                request.getKeyword(), request.getItemType(), request.getStatus());

        PageResult<ItemDTO> result = itemService.listItems(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取物料详情")
    @GetMapping("/{id}")
    public ApiResponse<ItemDTO> getItemById(@PathVariable Long id) {
        ItemDTO item = itemService.getItemById(id);
        return ApiResponse.success(item);
    }

    @Operation(summary = "创建物料")
    @PostMapping
    public ApiResponse<Long> createItem(@Validated @RequestBody ItemCreateRequest request) {
        Long itemId = itemService.createItem(request);
        return ApiResponse.success(itemId);
    }

    @Operation(summary = "更新物料")
    @PatchMapping("/{id}")
    public ApiResponse<Void> updateItem(@PathVariable Long id,
                                        @Validated @RequestBody ItemUpdateRequest request) {
        request.setId(id);
        itemService.updateItem(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除物料")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ApiResponse.success();
    }
}
