package com.aps.masterdata.impl;

import com.aps.dto.request.masterdata.SetupMatrixBatchImportRequest;
import com.aps.dto.request.masterdata.SetupMatrixCreateRequest;
import com.aps.dto.request.masterdata.SetupMatrixQueryRequest;
import com.aps.dto.request.masterdata.SetupMatrixUpdateRequest;
import com.aps.dto.response.masterdata.SetupMatrixDTO;
import com.aps.entity.masterdata.SetupMatrix;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.SetupMatrixMapper;
import com.aps.masterdata.SetupMatrixService;
import com.aps.response.PageResult;
import com.aps.utils.RequestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 换型矩阵Service实现类
 *
 * @author APS System
 * @since 2024-01-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SetupMatrixServiceImpl implements SetupMatrixService {

    private final SetupMatrixMapper setupMatrixMapper;

    @Override
    public PageResult<SetupMatrixDTO> listSetupMatrices(SetupMatrixQueryRequest request) {
        LambdaQueryWrapper<SetupMatrix> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索（源键或目标键）
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(SetupMatrix::getFromKey, request.getKeyword())
                    .or()
                    .like(SetupMatrix::getToKey, request.getKeyword()));
        }

        // 工艺类型过滤
        if (request.getProcessType() != null) {
            wrapper.eq(SetupMatrix::getProcessType, request.getProcessType());
        }

        // 状态过滤
        if (request.getStatus() != null) {
            wrapper.eq(SetupMatrix::getStatus, request.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(SetupMatrix::getCreatedTime);

        Page<SetupMatrix> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<SetupMatrix> resultPage = setupMatrixMapper.selectPage(page, wrapper);

        List<SetupMatrixDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(
                dtoList,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public SetupMatrixDTO getSetupMatrixById(Long id) {
        SetupMatrix setupMatrix = setupMatrixMapper.selectById(id);
        if (setupMatrix == null) {
            throw new BusinessException(40401, "换型矩阵不存在");
        }
        return convertToDTO(setupMatrix);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSetupMatrix(SetupMatrixCreateRequest request) {
        // 校验是否已存在相同的换型配置
        LambdaQueryWrapper<SetupMatrix> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetupMatrix::getProcessType, request.getProcessType())
                .eq(SetupMatrix::getFromKey, request.getFromKey())
                .eq(SetupMatrix::getToKey, request.getToKey());

        if (setupMatrixMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(40001, "该换型配置已存在");
        }

        SetupMatrix setupMatrix = new SetupMatrix();
        BeanUtils.copyProperties(request, setupMatrix);

        // 设置默认值
        if (setupMatrix.getStatus() == null) {
            setupMatrix.setStatus(1);
        }
        if (setupMatrix.getSetupCost() == null) {
            setupMatrix.setSetupCost(BigDecimal.ZERO);
        }

        setupMatrixMapper.insert(setupMatrix);

        log.info("创建换型矩阵成功: id={}, processType={}, fromKey={}, toKey={}, 操作人={}",
                setupMatrix.getId(), setupMatrix.getProcessType(),
                setupMatrix.getFromKey(), setupMatrix.getToKey(),
                RequestUtil.getUsername());

        return setupMatrix.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSetupMatrix(SetupMatrixUpdateRequest request) {
        SetupMatrix setupMatrix = setupMatrixMapper.selectById(request.getId());
        if (setupMatrix == null) {
            throw new BusinessException(40401, "换型矩阵不存在");
        }

        // 更新字段
        if (request.getSetupMinutes() != null) {
            setupMatrix.setSetupMinutes(request.getSetupMinutes());
        }
        if (request.getSetupCost() != null) {
            setupMatrix.setSetupCost(request.getSetupCost());
        }
        if (request.getStatus() != null) {
            setupMatrix.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getRemark())) {
            setupMatrix.setRemark(request.getRemark());
        }

        setupMatrixMapper.updateById(setupMatrix);

        log.info("更新换型矩阵成功: id={}, processType={}, fromKey={}, toKey={}, 操作人={}",
                setupMatrix.getId(), setupMatrix.getProcessType(),
                setupMatrix.getFromKey(), setupMatrix.getToKey(),
                RequestUtil.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSetupMatrix(Long id) {
        SetupMatrix setupMatrix = setupMatrixMapper.selectById(id);
        if (setupMatrix == null) {
            throw new BusinessException(40401, "换型矩阵不存在");
        }

        setupMatrixMapper.deleteById(id);

        log.info("删除换型矩阵成功: id={}, processType={}, fromKey={}, toKey={}, 操作人={}",
                setupMatrix.getId(), setupMatrix.getProcessType(),
                setupMatrix.getFromKey(), setupMatrix.getToKey(),
                RequestUtil.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImportSetupMatrix(SetupMatrixBatchImportRequest request) {
        Integer processType = request.getProcessType();
        String mode = request.getMode();

        // 如果是替换模式，先删除该工艺下的所有换型矩阵
        if ("REPLACE".equalsIgnoreCase(mode)) {
            LambdaQueryWrapper<SetupMatrix> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(SetupMatrix::getProcessType, processType);
            int deletedCount = setupMatrixMapper.delete(deleteWrapper);
            log.info("替换模式：删除工艺{}的{}条换型矩阵", processType, deletedCount);
        }

        // 批量插入
        int successCount = 0;
        int skipCount = 0;

        for (SetupMatrixBatchImportRequest.SetupMatrixItem item : request.getItems()) {
            // 检查是否已存在
            LambdaQueryWrapper<SetupMatrix> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SetupMatrix::getProcessType, processType)
                    .eq(SetupMatrix::getFromKey, item.getFromKey())
                    .eq(SetupMatrix::getToKey, item.getToKey());

            if (setupMatrixMapper.selectCount(wrapper) > 0) {
                skipCount++;
                log.warn("跳过重复的换型配置: processType={}, fromKey={}, toKey={}",
                        processType, item.getFromKey(), item.getToKey());
                continue;
            }

            // 插入新记录
            SetupMatrix setupMatrix = new SetupMatrix();
            setupMatrix.setProcessType(processType);
            setupMatrix.setFromKey(item.getFromKey());
            setupMatrix.setToKey(item.getToKey());
            setupMatrix.setSetupMinutes(item.getSetupMinutes());
            setupMatrix.setSetupCost(item.getSetupCost() != null ? item.getSetupCost() : BigDecimal.ZERO);
            setupMatrix.setStatus(1);
            setupMatrix.setRemark(item.getRemark());

            setupMatrixMapper.insert(setupMatrix);
            successCount++;
        }

        log.info("批量导入换型矩阵完成: processType={}, 模式={}, 成功={}, 跳过={}, 操作人={}",
                processType, mode, successCount, skipCount, RequestUtil.getUsername());
    }

    @Override
    public Integer getSetupMinutes(Integer processType, String fromKey, String toKey) {
        // 如果源键和目标键相同，返回0（无需换型）
        if (fromKey != null && fromKey.equals(toKey)) {
            return 0;
        }

        LambdaQueryWrapper<SetupMatrix> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetupMatrix::getProcessType, processType)
                .eq(SetupMatrix::getFromKey, fromKey)
                .eq(SetupMatrix::getToKey, toKey)
                .eq(SetupMatrix::getStatus, 1);

        SetupMatrix setupMatrix = setupMatrixMapper.selectOne(wrapper);
        return setupMatrix != null ? setupMatrix.getSetupMinutes() : 0;
    }

    @Override
    public List<SetupMatrixDTO> getActiveSetupMatricesByProcess(Integer processType) {
        LambdaQueryWrapper<SetupMatrix> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetupMatrix::getProcessType, processType)
                .eq(SetupMatrix::getStatus, 1)
                .orderByAsc(SetupMatrix::getFromKey)
                .orderByAsc(SetupMatrix::getToKey);

        List<SetupMatrix> list = setupMatrixMapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 实体转DTO
     */
    private SetupMatrixDTO convertToDTO(SetupMatrix setupMatrix) {
        SetupMatrixDTO dto = new SetupMatrixDTO();
        BeanUtils.copyProperties(setupMatrix, dto);
        
        // 设置工艺类型名称
        dto.setProcessTypeName(SetupMatrix.ProcessType.getDesc(setupMatrix.getProcessType()));
        
        // 设置状态文本
        dto.setStatusText(setupMatrix.getStatus() == 1 ? "启用" : "停用");
        
        return dto;
    }
}

