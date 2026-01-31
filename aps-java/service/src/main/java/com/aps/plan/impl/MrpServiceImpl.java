package com.aps.plan.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aps.dto.request.plan.MrpQueryRequest;
import com.aps.dto.request.plan.MrpRunRequest;
import com.aps.dto.response.plan.MrpDTO;
import com.aps.dto.response.plan.MrpItemDTO;
import com.aps.entity.masterdata.Bom;
import com.aps.entity.masterdata.BomItem;
import com.aps.entity.masterdata.Item;
import com.aps.entity.plan.Mps;
import com.aps.entity.plan.MpsItem;
import com.aps.entity.plan.Mrp;
import com.aps.entity.plan.MrpItem;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.BomItemMapper;
import com.aps.mapper.masterdata.BomMapper;
import com.aps.mapper.masterdata.ItemMapper;
import com.aps.mapper.plan.MpsItemMapper;
import com.aps.mapper.plan.MpsMapper;
import com.aps.mapper.plan.MrpItemMapper;
import com.aps.mapper.plan.MrpMapper;
import com.aps.plan.MrpService;
import com.aps.response.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.aps.constant.ErrorCode.*;

/**
 * MRP服务实现
 *
 * @author APS System
 * @since 2024-01-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MrpServiceImpl implements MrpService {

    private final MrpMapper mrpMapper;
    private final MrpItemMapper mrpItemMapper;
    private final MpsMapper mpsMapper;
    private final MpsItemMapper mpsItemMapper;
    private final BomMapper bomMapper;
    private final BomItemMapper bomItemMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long run(MrpRunRequest request) {
        log.info("运行MRP, mpsId={}", request.getMpsId());

        // 1. 查询MPS
        Mps mps = mpsMapper.selectById(request.getMpsId());
        if (mps == null || mps.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS不存在");
        }

        // 2. 校验MPS状态（已批准才能运行MRP）
        if (!Mps.Status.APPROVED.getCode().equals(mps.getStatus())) {
            throw new BusinessException(OPERATION_FORBIDDEN, "MPS状态不允许运行MRP，当前状态: "
                    + Mps.Status.getDesc(mps.getStatus()));
        }

        // 3. 查询MPS明细
        List<MpsItem> mpsItems = mpsItemMapper.selectListWithModel(request.getMpsId());
        if (mpsItems == null || mpsItems.isEmpty()) {
            throw new BusinessException(BAD_REQUEST, "MPS没有明细，无法运行MRP");
        }

        // 4. 生成MRP编号
        String mrpNo = generateMrpNo();

        // 5. 创建MRP
        Mrp mrp = new Mrp();
        mrp.setMrpNo(mrpNo);
        mrp.setMpsId(request.getMpsId());
        mrp.setStatus(Mrp.Status.GENERATING.getCode());
        mrp.setRemark("基于MPS " + mps.getMpsNo() + " 生成");

        mrpMapper.insert(mrp);

        try {
            // 6. 计算物料需求
            Map<String, Map<LocalDate, BigDecimal>> materialRequirements = calculateMaterialRequirements(mpsItems);

            // 7. 生成MRP明细
            for (Map.Entry<String, Map<LocalDate, BigDecimal>> entry : materialRequirements.entrySet()) {
                String itemCode = entry.getKey();
                Map<LocalDate, BigDecimal> dateQtyMap = entry.getValue();

                for (Map.Entry<LocalDate, BigDecimal> dateEntry : dateQtyMap.entrySet()) {
                    LocalDate reqDate = dateEntry.getKey();
                    BigDecimal reqQty = dateEntry.getValue();

                    MrpItem mrpItem = new MrpItem();
                    mrpItem.setMrpId(mrp.getId());
                    mrpItem.setItemCode(itemCode);
                    mrpItem.setReqDate(reqDate);
                    mrpItem.setReqQty(reqQty);
                    mrpItem.setSupplyQty(BigDecimal.ZERO);
                    mrpItem.setShortageQty(reqQty); // 简化处理：假设供应为0，缺口=需求

                    mrpItemMapper.insert(mrpItem);
                }
            }

            // 8. 更新MRP状态为完成
            mrp.setStatus(Mrp.Status.COMPLETED.getCode());

            // 9. 设置结果数据
            Map<String, Object> resultPayload = new HashMap<>();
            resultPayload.put("totalItems", materialRequirements.size());
            resultPayload.put("totalRecords", materialRequirements.values().stream()
                    .mapToInt(Map::size)
                    .sum());
            resultPayload.put("completedAt", LocalDateTime.now());
            mrp.setResultPayload(resultPayload);

            mrpMapper.updateById(mrp);

            log.info("运行MRP成功, id={}, mrpNo={}, totalItems={}", mrp.getId(), mrp.getMrpNo(),
                    materialRequirements.size());

        } catch (Exception e) {
            log.error("运行MRP失败, id={}, mrpNo={}", mrp.getId(), mrp.getMrpNo(), e);

            // 更新MRP状态为失败
            mrp.setStatus(Mrp.Status.FAILED.getCode());
            mrp.setRemark("运行失败: " + e.getMessage());
            mrpMapper.updateById(mrp);

            throw new BusinessException(INTERNAL_ERROR, "运行MRP失败: " + e.getMessage());
        }

        return mrp.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        log.info("删除MRP, id={}", id);

        // 1. 查询MRP
        Mrp mrp = mrpMapper.selectById(id);
        if (mrp == null || mrp.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MRP不存在");
        }

        // 2. 逻辑删除MRP
        mrpMapper.deleteById(id);

        // 3. 逻辑删除明细
        LambdaQueryWrapper<MrpItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MrpItem::getMrpId, id);
        List<MrpItem> items = mrpItemMapper.selectList(wrapper);
        for (MrpItem item : items) {
            mrpItemMapper.deleteById(item.getId());
        }

        log.info("删除MRP成功, id={}, mrpNo={}", id, mrp.getMrpNo());
    }

    @Override
    public MrpDTO getById(Long id) {
        log.info("获取MRP详情, id={}", id);

        // 查询MRP
        Mrp mrp = mrpMapper.selectById(id);
        if (mrp == null || mrp.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MRP不存在");
        }

        // 查询MPS信息
        Mps mps = mpsMapper.selectById(mrp.getMpsId());
        if (mps != null) {
            mrp.setMpsNo(mps.getMpsNo());
        }

        MrpDTO dto = convertToDTO(mrp);

        // 查询明细
        List<MrpItem> items = mrpItemMapper.selectListWithItem(id);
        if (items != null && !items.isEmpty()) {
            dto.setItems(items.stream()
                    .map(this::convertItemToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    public PageResult<MrpDTO> page(MrpQueryRequest request) {
        log.info("查询MRP列表, request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<Mrp> wrapper = new LambdaQueryWrapper<>();

        if (request.getMrpNo() != null && !request.getMrpNo().isEmpty()) {
            wrapper.like(Mrp::getMrpNo, request.getMrpNo());
        }
        if (request.getMpsId() != null) {
            wrapper.eq(Mrp::getMpsId, request.getMpsId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Mrp::getStatus, request.getStatus());
        }

        // 排序：创建时间降序
        wrapper.orderByDesc(Mrp::getCreatedTime);

        // 分页查询
        Page<Mrp> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Mrp> result = mrpMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<MrpDTO> dtoList = result.getRecords().stream()
                .map(mrp -> {
                    // 查询MPS信息
                    Mps mps = mpsMapper.selectById(mrp.getMpsId());
                    if (mps != null) {
                        mrp.setMpsNo(mps.getMpsNo());
                    }
                    return convertToDTO(mrp);
                })
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public List<MrpItemDTO> listItems(Long id) {
        log.info("查询MRP明细列表, id={}", id);

        // 查询MRP
        Mrp mrp = mrpMapper.selectById(id);
        if (mrp == null || mrp.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MRP不存在");
        }

        // 查询明细
        List<MrpItem> items = mrpItemMapper.selectListWithItem(id);
        return items.stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 计算物料需求
     * 简化版本：仅考虑一级BOM展开
     *
     * @param mpsItems MPS明细列表
     * @return 物料需求 Map<物料编码, Map<需求日期, 需求数量>>
     */
    private Map<String, Map<LocalDate, BigDecimal>> calculateMaterialRequirements(List<MpsItem> mpsItems) {
        Map<String, Map<LocalDate, BigDecimal>> requirements = new HashMap<>();

        for (MpsItem mpsItem : mpsItems) {
            // 查询车型对应的有效BOM
            LambdaQueryWrapper<Bom> bomWrapper = new LambdaQueryWrapper<>();
            bomWrapper.eq(Bom::getModelId, mpsItem.getModelId())
                    .eq(Bom::getStatus, 1)
                    .eq(Bom::getDeleted, 0)
                    .le(Bom::getEffectiveFrom, LocalDate.now())
                    .and(w -> w.isNull(Bom::getEffectiveTo)
                            .or()
                            .ge(Bom::getEffectiveTo, LocalDate.now()))
                    .orderByDesc(Bom::getEffectiveFrom)
                    .last("LIMIT 1");

            Bom bom = bomMapper.selectOne(bomWrapper);
            if (bom == null) {
                log.warn("车型没有有效BOM, modelId={}", mpsItem.getModelId());
                continue;
            }

            // 查询BOM明细
            LambdaQueryWrapper<BomItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(BomItem::getBomId, bom.getId())
                    .eq(BomItem::getDeleted, 0);
            List<BomItem> bomItems = bomItemMapper.selectList(itemWrapper);

            // 计算物料需求
            for (BomItem bomItem : bomItems) {
                String itemCode = bomItem.getItemCode();
                BigDecimal itemQty = bomItem.getQty();
                BigDecimal lossRate = bomItem.getLossRate();

                // 考虑损耗率：实际需求 = 基础需求 * (1 + 损耗率)
                BigDecimal actualQty = itemQty
                        .multiply(BigDecimal.valueOf(mpsItem.getQty()))
                        .multiply(BigDecimal.ONE.add(lossRate));

                // 累加到需求Map
                requirements.computeIfAbsent(itemCode, k -> new HashMap<>())
                        .merge(mpsItem.getBizDate(), actualQty, BigDecimal::add);
            }
        }

        return requirements;
    }

    /**
     * 生成MRP编号
     */
    private String generateMrpNo() {
        String prefix = "MRP";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return prefix + datePart + randomPart;
    }

    /**
     * 转换为DTO
     */
    private MrpDTO convertToDTO(Mrp mrp) {
        MrpDTO dto = new MrpDTO();
        BeanUtil.copyProperties(mrp, dto);
        dto.setStatusDesc(Mrp.Status.getDesc(mrp.getStatus()));
        return dto;
    }

    /**
     * 转换明细为DTO
     */
    private MrpItemDTO convertItemToDTO(MrpItem item) {
        MrpItemDTO dto = new MrpItemDTO();
        BeanUtil.copyProperties(item, dto);
        return dto;
    }
}

