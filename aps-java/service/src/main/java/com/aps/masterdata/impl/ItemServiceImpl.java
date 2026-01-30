package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.ItemCreateRequest;
import com.aps.dto.request.masterdata.ItemQueryRequest;
import com.aps.dto.request.masterdata.ItemUpdateRequest;
import com.aps.dto.response.masterdata.ItemDTO;
import com.aps.entity.masterdata.Item;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.ItemMapper;
import com.aps.masterdata.ItemService;
import com.aps.response.PageResult;
import com.aps.utils.RequestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemMapper itemMapper;

    // 物料类型映射
    private static final Map<Integer, String> ITEM_TYPE_MAP = new HashMap<>();
    static {
        ITEM_TYPE_MAP.put(1, "原料");
        ITEM_TYPE_MAP.put(2, "半成品");
        ITEM_TYPE_MAP.put(3, "成品");
        ITEM_TYPE_MAP.put(4, "辅料");
    }

    @Override
    public PageResult<ItemDTO> listItems(ItemQueryRequest request) {
        // 添加调试日志
        log.info("查询参数 - keyword: {}, itemType: {}, status: {}, pageNum: {}, pageSize: {}",
                request.getKeyword(),
                request.getItemType(),
                request.getStatus(),
                request.getPageNum(),
                request.getPageSize());

        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getKeyword())) {
            log.info("添加关键字搜索条件: {}", request.getKeyword());
            wrapper.and(w -> w.like(Item::getItemCode, request.getKeyword())
                    .or()
                    .like(Item::getItemName, request.getKeyword()));
        }

        if (request.getItemType() != null) {
            log.info("添加类型筛选条件: {}", request.getItemType());
            wrapper.eq(Item::getItemType, request.getItemType());
        }

        if (request.getStatus() != null) {
            log.info("添加状态筛选条件: {}", request.getStatus());
            wrapper.eq(Item::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Item::getCreatedTime);

        Page<Item> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Item> resultPage = itemMapper.selectPage(page, wrapper);

        log.info("查询结果数量: {}", resultPage.getRecords().size());

        List<ItemDTO> itemDTOS = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(
                itemDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }


    @Override
    public ItemDTO getItemById(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null || item.getDeleted() == 1) {
            throw new BusinessException(40401, "物料不存在");
        }
        return convertToDTO(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createItem(ItemCreateRequest request) {
        // 检查物料编码是否已存在
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getItemCode, request.getItemCode())
                .eq(Item::getDeleted, 0);

        if (itemMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(40001, "物料编码已存在");
        }

        // 创建物料
        Item item = new Item();
        BeanUtils.copyProperties(request, item);
        item.setDeleted(0);

        itemMapper.insert(item);

        log.info("创建物料成功: id={}, itemCode={}, 操作人={}",
                item.getId(), item.getItemCode(), RequestUtil.getUsername());

        return item.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(ItemUpdateRequest request) {
        // 检查物料是否存在
        Item item = itemMapper.selectById(request.getId());
        if (item == null || item.getDeleted() == 1) {
            throw new BusinessException(40401, "物料不存在");
        }

        // 更新物料信息 - 只更新非空字段
        if (StringUtils.hasText(request.getItemName())) {
            item.setItemName(request.getItemName());
        }
        if (request.getItemType() != null) {
            item.setItemType(request.getItemType());
        }
        if (StringUtils.hasText(request.getUom())) {
            item.setUom(request.getUom());
        }
        if (StringUtils.hasText(request.getSpec())) {
            item.setSpec(request.getSpec());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getRemark())) {
            item.setRemark(request.getRemark());
        }

        itemMapper.updateById(item);

        log.info("更新物料成功: id={}, itemCode={}, 操作人={}",
                item.getId(), item.getItemCode(), RequestUtil.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        // 检查物料是否存在
        Item item = itemMapper.selectById(id);
        if (item == null || item.getDeleted() == 1) {
            throw new BusinessException(40401, "物料不存在");
        }

        // TODO: 检查物料是否被BOM引用
        // LambdaQueryWrapper<BomItem> bomWrapper = new LambdaQueryWrapper<>();
        // bomWrapper.eq(BomItem::getChildCode, item.getItemCode())
        //           .eq(BomItem::getDeleted, 0);
        // if (bomItemMapper.selectCount(bomWrapper) > 0) {
        //     throw new BusinessException(40003, "物料已被BOM引用，无法删除");
        // }

        // 逻辑删除
        LambdaUpdateWrapper<Item> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Item::getId, id)
                .set(Item::getDeleted, 1)
                .set(Item::getUpdatedBy, RequestUtil.getUsername())
                .set(Item::getUpdatedTime, LocalDateTime.now());

        itemMapper.update(null, wrapper);

        log.info("删除物料成功: id={}, itemCode={}, 操作人={}",
                item.getId(), item.getItemCode(), RequestUtil.getUsername());
    }

    /**
     * 实体转 DTO
     */
    private ItemDTO convertToDTO(Item item) {
        ItemDTO dto = new ItemDTO();
        BeanUtils.copyProperties(item, dto);

        // 设置物料类型文本
        dto.setItemTypeText(ITEM_TYPE_MAP.getOrDefault(item.getItemType(), "未知"));

        // 设置状态文本
        dto.setStatusText(item.getStatus() == 1 ? "启用" : "停用");

        return dto;
    }
}
