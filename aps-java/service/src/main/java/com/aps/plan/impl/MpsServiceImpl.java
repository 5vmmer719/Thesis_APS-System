package com.aps.plan.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aps.dto.request.plan.*;
import com.aps.dto.response.plan.MpsDTO;
import com.aps.dto.response.plan.MpsItemDTO;
import com.aps.entity.masterdata.Model;
import com.aps.entity.plan.Mps;
import com.aps.entity.plan.MpsItem;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.ModelMapper;
import com.aps.mapper.plan.MpsItemMapper;
import com.aps.mapper.plan.MpsMapper;
import com.aps.plan.MpsService;
import com.aps.response.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.aps.constant.ErrorCode.*;

/**
 * MPS服务实现
 *
 * @author APS System
 * @since 2024-01-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MpsServiceImpl implements MpsService {

    private final MpsMapper mpsMapper;
    private final MpsItemMapper mpsItemMapper;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MpsCreateRequest request) {
        log.info("创建MPS, mpsNo={}", request.getMpsNo());

        // 1. 校验MPS编号唯一性
        LambdaQueryWrapper<Mps> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Mps::getMpsNo, request.getMpsNo())
                .eq(Mps::getDeleted, 0);
        if (mpsMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(DUPLICATE_DATA, "MPS编号已存在: " + request.getMpsNo());
        }

        // 2. 校验日期
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException(BAD_REQUEST, "结束日期不能早于开始日期");
        }

        // 3. 创建MPS
        Mps mps = new Mps();
        BeanUtil.copyProperties(request, mps);
        if (mps.getStatus() == null) {
            mps.setStatus(Mps.Status.DRAFT.getCode());
        }

        mpsMapper.insert(mps);

        log.info("创建MPS成功, id={}, mpsNo={}", mps.getId(), mps.getMpsNo());
        return mps.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MpsUpdateRequest request) {
        log.info("更新MPS, id={}, request={}", id, request);

        // 1. 查询MPS
        Mps mps = mpsMapper.selectById(id);
        if (mps == null || mps.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS不存在");
        }

        // 2. 校验状态（仅草稿可编辑）
        if (!Mps.Status.DRAFT.getCode().equals(mps.getStatus())) {
            throw new BusinessException(OPERATION_FORBIDDEN, "MPS状态不允许编辑，当前状态: "
                    + Mps.Status.getDesc(mps.getStatus()));
        }

        // 3. 更新字段
        if (request.getMpsNo() != null) {
            // 校验新编号唯一性
            LambdaQueryWrapper<Mps> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Mps::getMpsNo, request.getMpsNo())
                    .ne(Mps::getId, id)
                    .eq(Mps::getDeleted, 0);
            if (mpsMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(DUPLICATE_DATA, "MPS编号已存在: " + request.getMpsNo());
            }
            mps.setMpsNo(request.getMpsNo());
        }
        if (request.getStartDate() != null) {
            mps.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            mps.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            mps.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            mps.setRemark(request.getRemark());
        }

        // 4. 校验日期
        if (mps.getEndDate().isBefore(mps.getStartDate())) {
            throw new BusinessException(BAD_REQUEST, "结束日期不能早于开始日期");
        }

        mpsMapper.updateById(mps);

        log.info("更新MPS成功, id={}, mpsNo={}", id, mps.getMpsNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        log.info("删除MPS, id={}", id);

        // 1. 查询MPS
        Mps mps = mpsMapper.selectById(id);
        if (mps == null || mps.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS不存在");
        }

        // 2. 校验状态（仅草稿可删除）
        if (!Mps.Status.DRAFT.getCode().equals(mps.getStatus())) {
            throw new BusinessException(OPERATION_FORBIDDEN, "MPS状态不允许删除，当前状态: "
                    + Mps.Status.getDesc(mps.getStatus()));
        }

        // 3. 逻辑删除MPS
        mpsMapper.deleteById(id);

        // 4. 逻辑删除明细
        LambdaQueryWrapper<MpsItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MpsItem::getMpsId, id);
        List<MpsItem> items = mpsItemMapper.selectList(wrapper);
        for (MpsItem item : items) {
            mpsItemMapper.deleteById(item.getId());
        }

        log.info("删除MPS成功, id={}, mpsNo={}", id, mps.getMpsNo());
    }

    @Override
    public MpsDTO getById(Long id) {
        log.info("获取MPS详情, id={}", id);

        // 查询MPS
        Mps mps = mpsMapper.selectById(id);
        if (mps == null || mps.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS不存在");
        }

        MpsDTO dto = convertToDTO(mps);

        // 查询明细
        List<MpsItem> items = mpsItemMapper.selectListWithModel(id);
        if (items != null && !items.isEmpty()) {
            dto.setItems(items.stream()
                    .map(this::convertItemToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    public PageResult<MpsDTO> page(MpsQueryRequest request) {
        log.info("查询MPS列表, request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<Mps> wrapper = new LambdaQueryWrapper<>();

        if (request.getMpsNo() != null && !request.getMpsNo().isEmpty()) {
            wrapper.like(Mps::getMpsNo, request.getMpsNo());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Mps::getStatus, request.getStatus());
        }
        if (request.getStartDateFrom() != null) {
            wrapper.ge(Mps::getStartDate, request.getStartDateFrom());
        }
        if (request.getStartDateTo() != null) {
            wrapper.le(Mps::getStartDate, request.getStartDateTo());
        }
        if (request.getEndDateFrom() != null) {
            wrapper.ge(Mps::getEndDate, request.getEndDateFrom());
        }
        if (request.getEndDateTo() != null) {
            wrapper.le(Mps::getEndDate, request.getEndDateTo());
        }

        // 排序：创建时间降序
        wrapper.orderByDesc(Mps::getCreatedTime);

        // 分页查询
        Page<Mps> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Mps> result = mpsMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<MpsDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setItems(Long id, MpsItemSetRequest request) {
        log.info("设置MPS明细, id={}, mode={}, itemCount={}", id, request.getMode(), request.getItems().size());

        // 1. 查询MPS
        Mps mps = mpsMapper.selectById(id);
        if (mps == null || mps.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS不存在");
        }

        // 2. 校验状态（仅草稿可编辑）
        if (!Mps.Status.DRAFT.getCode().equals(mps.getStatus())) {
            throw new BusinessException(OPERATION_FORBIDDEN, "MPS状态不允许设置明细");
        }

        // 3. 校验车型
        for (MpsItemRequest itemReq : request.getItems()) {
            Model model = modelMapper.selectById(itemReq.getModelId());
            if (model == null || model.getDeleted() == 1) {
                throw new BusinessException(DATA_NOT_FOUND, "车型不存在: " + itemReq.getModelId());
            }
            if (model.getStatus() != 1) {
                throw new BusinessException(OPERATION_FORBIDDEN, "车型已停用: " + model.getModelCode());
            }

            // 校验日期在范围内
            if (itemReq.getBizDate().isBefore(mps.getStartDate())
                    || itemReq.getBizDate().isAfter(mps.getEndDate())) {
                throw new BusinessException(BAD_REQUEST,
                        "计划日期必须在MPS日期范围内: " + itemReq.getBizDate());
            }
        }

        // 4. 如果是替换模式，删除旧明细
        if (request.getMode() == MpsItemSetRequest.ModeEnum.REPLACE) {
            LambdaQueryWrapper<MpsItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MpsItem::getMpsId, id);
            List<MpsItem> oldItems = mpsItemMapper.selectList(wrapper);
            for (MpsItem item : oldItems) {
                mpsItemMapper.deleteById(item.getId());
            }
        }

        // 5. 插入新明细
        for (MpsItemRequest itemReq : request.getItems()) {
            MpsItem item = new MpsItem();
            item.setMpsId(id);
            item.setBizDate(itemReq.getBizDate());
            item.setModelId(itemReq.getModelId());
            item.setQty(itemReq.getQty());

            mpsItemMapper.insert(item);
        }

        log.info("设置MPS明细成功, id={}, mode={}, itemCount={}", id, request.getMode(), request.getItems().size());
    }

    @Override
    public List<MpsItemDTO> listItems(Long id) {
        log.info("查询MPS明细列表, id={}", id);

        // 查询MPS
        Mps mps = mpsMapper.selectById(id);
        if (mps == null || mps.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS不存在");
        }

        // 查询明细
        List<MpsItem> items = mpsItemMapper.selectListWithModel(id);
        return items.stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id, Long itemId) {
        log.info("删除MPS明细, mpsId={}, itemId={}", id, itemId);

        // 1. 查询MPS
        Mps mps = mpsMapper.selectById(id);
        if (mps == null || mps.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS不存在");
        }

        // 2. 校验状态（仅草稿可编辑）
        if (!Mps.Status.DRAFT.getCode().equals(mps.getStatus())) {
            throw new BusinessException(OPERATION_FORBIDDEN, "MPS状态不允许删除明细");
        }

        // 3. 查询明细
        MpsItem item = mpsItemMapper.selectById(itemId);
        if (item == null || item.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS明细不存在");
        }
        if (!item.getMpsId().equals(id)) {
            throw new BusinessException(BAD_REQUEST, "明细不属于该MPS");
        }

        // 4. 删除明细
        mpsItemMapper.deleteById(itemId);

        log.info("删除MPS明细成功, mpsId={}, itemId={}", id, itemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApproval(Long id) {
        log.info("提交MPS审批, id={}", id);

        // 1. 查询MPS
        Mps mps = mpsMapper.selectById(id);
        if (mps == null || mps.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS不存在");
        }

        // 2. 校验状态
        if (!Mps.Status.DRAFT.getCode().equals(mps.getStatus())) {
            throw new BusinessException(OPERATION_FORBIDDEN, "MPS状态不允许提交审批");
        }

        // 3. 校验是否有明细
        LambdaQueryWrapper<MpsItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MpsItem::getMpsId, id);
        long itemCount = mpsItemMapper.selectCount(wrapper);
        if (itemCount == 0) {
            throw new BusinessException(BAD_REQUEST, "MPS没有明细，无法提交审批");
        }

        // 4. 更新状态
        mps.setStatus(Mps.Status.APPROVING.getCode());
        mpsMapper.updateById(mps);

        log.info("提交MPS审批成功, id={}, mpsNo={}", id, mps.getMpsNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        log.info("批准MPS, id={}", id);

        // 1. 查询MPS
        Mps mps = mpsMapper.selectById(id);
        if (mps == null || mps.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS不存在");
        }

        // 2. 校验状态
        if (!Mps.Status.APPROVING.getCode().equals(mps.getStatus())) {
            throw new BusinessException(OPERATION_FORBIDDEN, "MPS状态不允许批准");
        }

        // 3. 更新状态
        mps.setStatus(Mps.Status.APPROVED.getCode());
        mpsMapper.updateById(mps);

        log.info("批准MPS成功, id={}, mpsNo={}", id, mps.getMpsNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id) {
        log.info("关闭MPS, id={}", id);

        // 1. 查询MPS
        Mps mps = mpsMapper.selectById(id);
        if (mps == null || mps.getDeleted() == 1) {
            throw new BusinessException(DATA_NOT_FOUND, "MPS不存在");
        }

        // 2. 校验状态（已批准才能关闭）
        if (!Mps.Status.APPROVED.getCode().equals(mps.getStatus())) {
            throw new BusinessException(OPERATION_FORBIDDEN, "MPS状态不允许关闭");
        }

        // 3. 更新状态
        mps.setStatus(Mps.Status.CLOSED.getCode());
        mpsMapper.updateById(mps);

        log.info("关闭MPS成功, id={}, mpsNo={}", id, mps.getMpsNo());
    }

    /**
     * 转换为DTO
     */
    private MpsDTO convertToDTO(Mps mps) {
        MpsDTO dto = new MpsDTO();
        BeanUtil.copyProperties(mps, dto);
        dto.setStatusDesc(Mps.Status.getDesc(mps.getStatus()));
        return dto;
    }

    /**
     * 转换明细为DTO
     */
    private MpsItemDTO convertItemToDTO(MpsItem item) {
        MpsItemDTO dto = new MpsItemDTO();
        BeanUtil.copyProperties(item, dto);
        return dto;
    }
}

