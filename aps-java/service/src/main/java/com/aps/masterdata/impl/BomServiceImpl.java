package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.BomCreateRequest;
import com.aps.dto.request.masterdata.BomItemCreateRequest;
import com.aps.dto.request.masterdata.BomQueryRequest;
import com.aps.dto.request.masterdata.BomUpdateRequest;
import com.aps.dto.response.masterdata.BomDTO;
import com.aps.dto.response.masterdata.BomDetailDTO;
import com.aps.dto.response.masterdata.BomItemDTO;
import com.aps.entity.masterdata.Bom;
import com.aps.entity.masterdata.BomItem;
import com.aps.entity.masterdata.Item;
import com.aps.entity.masterdata.Model;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.BomItemMapper;
import com.aps.mapper.masterdata.BomMapper;
import com.aps.mapper.masterdata.ItemMapper;
import com.aps.mapper.masterdata.ModelMapper;
import com.aps.masterdata.BomService;
import com.aps.response.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BOM服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BomServiceImpl implements BomService {

    private final BomMapper bomMapper;
    private final BomItemMapper bomItemMapper;
    private final ModelMapper modelMapper;
    private final ItemMapper itemMapper;

    @Override
    public PageResult<BomDTO> listBoms(BomQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Bom> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索（BOM编码）
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Bom::getBomCode, request.getKeyword());
        }

        // 按车型筛选
        if (request.getModelId() != null) {
            wrapper.eq(Bom::getModelId, request.getModelId());
        }

        // 按版本筛选
        if (StringUtils.hasText(request.getVersion())) {
            wrapper.eq(Bom::getVersion, request.getVersion());
        }

        // 按状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(Bom::getStatus, request.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Bom::getCreatedTime);

        // 分页查询
        Page<Bom> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Bom> resultPage = bomMapper.selectPage(page, wrapper);

        // 获取所有车型ID
        List<Long> modelIds = resultPage.getRecords().stream()
                .map(Bom::getModelId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询车型信息
        Map<Long, Model> modelMap = modelIds.isEmpty() ? Map.of() :
                modelMapper.selectBatchIds(modelIds).stream()
                        .collect(Collectors.toMap(Model::getId, m -> m));

        // 批量查询每个BOM的物料数量
        List<Long> bomIds = resultPage.getRecords().stream()
                .map(Bom::getId)
                .collect(Collectors.toList());

        Map<Long, Long> itemCountMap = bomIds.isEmpty() ? Map.of() :
                bomItemMapper.selectList(new LambdaQueryWrapper<BomItem>()
                                .in(BomItem::getBomId, bomIds))
                        .stream()
                        .collect(Collectors.groupingBy(BomItem::getBomId, Collectors.counting()));

        // 转换为DTO
        List<BomDTO> bomDTOS = resultPage.getRecords().stream()
                .map(bom -> convertToDTO(bom, modelMap.get(bom.getModelId()),
                        itemCountMap.getOrDefault(bom.getId(), 0L).intValue()))
                .collect(Collectors.toList());

        log.info("分页查询BOM结果: total={}, pages={}, current={}, size={}, records={}",
                resultPage.getTotal(), resultPage.getPages(), resultPage.getCurrent(),
                resultPage.getSize(), resultPage.getRecords().size());

        return new PageResult<>(
                bomDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public BomDetailDTO getBomDetailById(Long id) {
        // 查询BOM基本信息
        Bom bom = bomMapper.selectById(id);
        if (bom == null) {
            throw new BusinessException("BOM不存在");
        }

        // 查询车型信息
        Model model = modelMapper.selectById(bom.getModelId());

        // 查询BOM明细
        List<BomItem> bomItems = bomItemMapper.selectList(
                new LambdaQueryWrapper<BomItem>()
                        .eq(BomItem::getBomId, id)
                        .orderByAsc(BomItem::getLevelNo)
        );

        // 获取所有物料编码
        List<String> itemCodes = bomItems.stream()
                .map(BomItem::getItemCode)
                .collect(Collectors.toList());

        // 批量查询物料信息（通过编码）
        Map<String, Item> itemMap = itemCodes.isEmpty() ? Map.of() :
                itemMapper.selectList(new LambdaQueryWrapper<Item>()
                                .in(Item::getItemCode, itemCodes))
                        .stream()
                        .collect(Collectors.toMap(Item::getItemCode, i -> i));

        // 转换为DTO
        BomDTO bomDTO = convertToDTO(bom, model, bomItems.size());
        List<BomItemDTO> itemDTOS = bomItems.stream()
                .map(bomItem -> convertToItemDTO(bomItem, itemMap.get(bomItem.getItemCode())))
                .collect(Collectors.toList());

        BomDetailDTO detailDTO = new BomDetailDTO();
        detailDTO.setBom(bomDTO);
        detailDTO.setItems(itemDTOS);

        return detailDTO;
    }

    @Override
    public BomDTO getBomById(Long id) {
        Bom bom = bomMapper.selectById(id);
        if (bom == null) {
            throw new BusinessException("BOM不存在");
        }

        // 查询车型信息
        Model model = modelMapper.selectById(bom.getModelId());

        // 查询物料数量
        Long itemCount = bomItemMapper.selectCount(
                new LambdaQueryWrapper<BomItem>().eq(BomItem::getBomId, id)
        );

        return convertToDTO(bom, model, itemCount.intValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBom(BomCreateRequest request) {
        // 校验BOM编码是否重复
        Long count = bomMapper.selectCount(
                new LambdaQueryWrapper<Bom>().eq(Bom::getBomCode, request.getBomCode())
        );
        if (count > 0) {
            throw new BusinessException("BOM编码已存在");
        }

        // 校验车型是否存在
        Model model = modelMapper.selectById(request.getModelId());
        if (model == null) {
            throw new BusinessException("车型不存在");
        }

        // 校验物料是否存在
        List<String> itemCodes = request.getItems().stream()
                .map(BomItemCreateRequest::getItemCode)
                .collect(Collectors.toList());
        List<Item> items = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .in(Item::getItemCode, itemCodes));
        if (items.size() != itemCodes.size()) {
            throw new BusinessException("部分物料不存在");
        }

        // 创建BOM主记录
        Bom bom = new Bom();
        bom.setBomCode(request.getBomCode());
        // bomName 字段不存在于数据库，已标记为 @TableField(exist = false)
        // bom.setBomName(request.getBomName());
        bom.setModelId(request.getModelId());
        bom.setVersion(request.getVersion());
        bom.setEffectiveFrom(request.getEffectiveFrom());
        bom.setEffectiveTo(request.getEffectiveTo());
        bom.setStatus(request.getStatus());
        bom.setRemark(request.getRemark());

        bomMapper.insert(bom);
        log.info("创建BOM成功: id={}, bomCode={}", bom.getId(), bom.getBomCode());

        // 创建BOM明细
        List<BomItem> bomItems = request.getItems().stream()
                .map(itemRequest -> {
                    BomItem bomItem = new BomItem();
                    bomItem.setBomId(bom.getId());
                    bomItem.setParentItemCode(itemRequest.getParentItemCode());
                    bomItem.setItemCode(itemRequest.getItemCode());
                    bomItem.setQty(itemRequest.getQty());
                    bomItem.setLossRate(itemRequest.getLossRate());
                    bomItem.setLevelNo(itemRequest.getLevelNo());
                    bomItem.setIsOptional(itemRequest.getIsOptional());
                    bomItem.setOptionGroup(itemRequest.getOptionGroup());
                    bomItem.setAltGroup(itemRequest.getAltGroup());
                    bomItem.setRemark(itemRequest.getRemark());
                    return bomItem;
                })
                .collect(Collectors.toList());

        bomItems.forEach(bomItemMapper::insert);
        log.info("创建BOM明细成功: bomId={}, itemCount={}", bom.getId(), bomItems.size());

        return bom.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBom(BomUpdateRequest request) {
        // 查询BOM是否存在
        Bom bom = bomMapper.selectById(request.getId());
        if (bom == null) {
            throw new BusinessException("BOM不存在");
        }

        // 更新BOM基本信息
        // bomName 字段不存在于数据库，已标记为 @TableField(exist = false)
        // if (StringUtils.hasText(request.getBomName())) {
        //     bom.setBomName(request.getBomName());
        // }
        if (StringUtils.hasText(request.getVersion())) {
            bom.setVersion(request.getVersion());
        }
        if (request.getEffectiveFrom() != null) {
            bom.setEffectiveFrom(request.getEffectiveFrom());
        }
        if (request.getEffectiveTo() != null) {
            bom.setEffectiveTo(request.getEffectiveTo());
        }
        if (request.getStatus() != null) {
            bom.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            bom.setRemark(request.getRemark());
        }

        bomMapper.updateById(bom);
        log.info("更新BOM基本信息成功: id={}", bom.getId());

        // 如果提供了明细列表，则全量更新明细
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // 删除原有明细
            bomItemMapper.delete(new LambdaQueryWrapper<BomItem>()
                    .eq(BomItem::getBomId, request.getId()));

            // 校验物料是否存在
            List<String> itemCodes = request.getItems().stream()
                    .map(BomItemCreateRequest::getItemCode)
                    .collect(Collectors.toList());
            List<Item> items = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                    .in(Item::getItemCode, itemCodes));
            if (items.size() != itemCodes.size()) {
                throw new BusinessException("部分物料不存在");
            }

            // 插入新明细
            List<BomItem> bomItems = request.getItems().stream()
                    .map(itemRequest -> {
                        BomItem bomItem = new BomItem();
                        bomItem.setBomId(request.getId());
                        bomItem.setParentItemCode(itemRequest.getParentItemCode());
                        bomItem.setItemCode(itemRequest.getItemCode());
                        bomItem.setQty(itemRequest.getQty());
                        bomItem.setLossRate(itemRequest.getLossRate());
                        bomItem.setLevelNo(itemRequest.getLevelNo());
                        bomItem.setIsOptional(itemRequest.getIsOptional());
                        bomItem.setOptionGroup(itemRequest.getOptionGroup());
                        bomItem.setAltGroup(itemRequest.getAltGroup());
                        bomItem.setRemark(itemRequest.getRemark());
                        return bomItem;
                    })
                    .collect(Collectors.toList());

            bomItems.forEach(bomItemMapper::insert);
            log.info("更新BOM明细成功: bomId={}, itemCount={}", request.getId(), bomItems.size());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBom(Long id) {
        // 查询BOM是否存在
        Bom bom = bomMapper.selectById(id);
        if (bom == null) {
            throw new BusinessException("BOM不存在");
        }

        // 逻辑删除BOM
        bomMapper.deleteById(id);

        // 逻辑删除BOM明细
        bomItemMapper.delete(new LambdaQueryWrapper<BomItem>()
                .eq(BomItem::getBomId, id));

        log.info("删除BOM成功: id={}", id);
    }

    @Override
    public PageResult<BomDTO> listBomsByModelId(Long modelId, Integer pageNum, Integer pageSize) {
        // 校验车型是否存在
        Model model = modelMapper.selectById(modelId);
        if (model == null) {
            throw new BusinessException("车型不存在");
        }

        // 构建查询条件
        LambdaQueryWrapper<Bom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bom::getModelId, modelId)
                .orderByDesc(Bom::getCreatedTime);

        // 分页查询
        Page<Bom> page = new Page<>(pageNum, pageSize);
        Page<Bom> resultPage = bomMapper.selectPage(page, wrapper);

        // 批量查询每个BOM的物料数量
        List<Long> bomIds = resultPage.getRecords().stream()
                .map(Bom::getId)
                .collect(Collectors.toList());

        Map<Long, Long> itemCountMap = bomIds.isEmpty() ? Map.of() :
                bomItemMapper.selectList(new LambdaQueryWrapper<BomItem>()
                                .in(BomItem::getBomId, bomIds))
                        .stream()
                        .collect(Collectors.groupingBy(BomItem::getBomId, Collectors.counting()));

        // 转换为DTO
        List<BomDTO> bomDTOS = resultPage.getRecords().stream()
                .map(bom -> convertToDTO(bom, model,
                        itemCountMap.getOrDefault(bom.getId(), 0L).intValue()))
                .collect(Collectors.toList());

        return new PageResult<>(
                bomDTOS,
                resultPage.getTotal(),
                pageNum,
                pageSize
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyBom(Long bomId, String newVersion) {
        // 查询源BOM
        Bom sourceBom = bomMapper.selectById(bomId);
        if (sourceBom == null) {
            throw new BusinessException("源BOM不存在");
        }

        // 生成新的BOM编码
        String newBomCode = sourceBom.getBomCode() + "_" + newVersion;

        // 校验新编码是否重复
        Long count = bomMapper.selectCount(
                new LambdaQueryWrapper<Bom>().eq(Bom::getBomCode, newBomCode)
        );
        if (count > 0) {
            throw new BusinessException("新BOM编码已存在");
        }

        // 创建新BOM
        Bom newBom = new Bom();
        newBom.setBomCode(newBomCode);
        // bomName 字段不存在于数据库，已标记为 @TableField(exist = false)
        // newBom.setBomName(sourceBom.getBomName() + " (" + newVersion + ")");
        newBom.setModelId(sourceBom.getModelId());
        newBom.setVersion(newVersion);
        newBom.setEffectiveFrom(sourceBom.getEffectiveFrom());
        newBom.setEffectiveTo(sourceBom.getEffectiveTo());
        newBom.setStatus(sourceBom.getStatus());
        newBom.setRemark("从 " + sourceBom.getBomCode() + " 复制");

        bomMapper.insert(newBom);
        log.info("复制BOM成功: sourceId={}, newId={}, newVersion={}",
                bomId, newBom.getId(), newVersion);

        // 复制BOM明细
        List<BomItem> sourceItems = bomItemMapper.selectList(
                new LambdaQueryWrapper<BomItem>()
                        .eq(BomItem::getBomId, bomId)
                        .orderByAsc(BomItem::getLevelNo)
        );

        List<BomItem> newItems = sourceItems.stream()
                .map(sourceItem -> {
                    BomItem newItem = new BomItem();
                    newItem.setBomId(newBom.getId());
                    newItem.setParentItemCode(sourceItem.getParentItemCode());
                    newItem.setItemCode(sourceItem.getItemCode());
                    newItem.setQty(sourceItem.getQty());
                    newItem.setLossRate(sourceItem.getLossRate());
                    newItem.setLevelNo(sourceItem.getLevelNo());
                    newItem.setIsOptional(sourceItem.getIsOptional());
                    newItem.setOptionGroup(sourceItem.getOptionGroup());
                    newItem.setAltGroup(sourceItem.getAltGroup());
                    newItem.setRemark(sourceItem.getRemark());
                    return newItem;
                })
                .collect(Collectors.toList());

        newItems.forEach(bomItemMapper::insert);
        log.info("复制BOM明细成功: newBomId={}, itemCount={}", newBom.getId(), newItems.size());

        return newBom.getId();
    }

    /**
     * 转换为BomDTO
     */
    private BomDTO convertToDTO(Bom bom, Model model, Integer itemCount) {
        BomDTO dto = new BomDTO();
        dto.setId(bom.getId());
        dto.setBomCode(bom.getBomCode());
        // bomName 字段不存在于数据库，已标记为 @TableField(exist = false)
        // dto.setBomName(bom.getBomName());
        dto.setModelId(bom.getModelId());
        dto.setVersion(bom.getVersion());
        dto.setEffectiveFrom(bom.getEffectiveFrom());
        dto.setEffectiveTo(bom.getEffectiveTo());
        dto.setStatus(bom.getStatus());
        dto.setStatusText(bom.getStatus() == 1 ? "启用" : "停用");
        dto.setItemCount(itemCount);
        dto.setRemark(bom.getRemark());
        dto.setCreatedTime(bom.getCreatedTime());
        dto.setCreatedBy(bom.getCreatedBy());
        dto.setUpdatedTime(bom.getUpdatedTime());
        dto.setUpdatedBy(bom.getUpdatedBy());

        if (model != null) {
            dto.setModelCode(model.getModelCode());
            dto.setModelName(model.getModelName());
        }

        return dto;
    }

    /**
     * 转换为BomItemDTO
     */
    private BomItemDTO convertToItemDTO(BomItem bomItem, Item item) {
        BomItemDTO dto = new BomItemDTO();
        dto.setId(bomItem.getId());
        dto.setBomId(bomItem.getBomId());
        dto.setParentItemCode(bomItem.getParentItemCode());
        dto.setItemCode(bomItem.getItemCode());
        dto.setQty(bomItem.getQty());
        dto.setLossRate(bomItem.getLossRate());
        dto.setLevelNo(bomItem.getLevelNo());
        dto.setIsOptional(bomItem.getIsOptional());
        dto.setOptionGroup(bomItem.getOptionGroup());
        dto.setAltGroup(bomItem.getAltGroup());
        dto.setRemark(bomItem.getRemark());

        if (item != null) {
            dto.setItemName(item.getItemName());
            dto.setItemType(item.getItemType());
        }

        return dto;
    }
}

