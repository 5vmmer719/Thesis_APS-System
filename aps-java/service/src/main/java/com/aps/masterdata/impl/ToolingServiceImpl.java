package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.ToolingCreateRequest;
import com.aps.dto.request.masterdata.ToolingQueryRequest;
import com.aps.dto.response.masterdata.ToolingDTO;
import com.aps.entity.masterdata.Tooling;
import com.aps.entity.masterdata.ToolingBind;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.ToolingBindMapper;
import com.aps.mapper.masterdata.ToolingMapper;
import com.aps.masterdata.ToolingService;
import com.aps.response.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工装服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolingServiceImpl implements ToolingService {

    private final ToolingMapper toolingMapper;
    private final ToolingBindMapper toolingBindMapper;

    // 工装类型映射
    private static final Map<Integer, String> TOOLING_TYPE_MAP = Map.of(
            1, "模具",
            2, "夹具",
            3, "检具"
    );

    @Override
    public PageResult<ToolingDTO> listToolings(ToolingQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Tooling> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<Tooling> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Tooling> resultPage = toolingMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<ToolingDTO> toolingDTOS = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("分页查询工装结果: total={}, records={}", resultPage.getTotal(), toolingDTOS.size());

        return new PageResult<>(
                toolingDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public ToolingDTO getToolingById(Long id) {
        Tooling tooling = toolingMapper.selectById(id);
        if (tooling == null) {
            throw new BusinessException("工装不存在");
        }

        return convertToDTO(tooling);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTooling(ToolingCreateRequest request) {
        // 校验工装编码是否重复
        Long count = toolingMapper.selectCount(
                new LambdaQueryWrapper<Tooling>().eq(Tooling::getToolingCode, request.getToolingCode())
        );
        if (count > 0) {
            throw new BusinessException("工装编码已存在");
        }

        // 创建工装
        Tooling tooling = new Tooling();
        BeanUtils.copyProperties(request, tooling);

        toolingMapper.insert(tooling);
        log.info("创建工装成功: id={}, toolingCode={}", tooling.getId(), tooling.getToolingCode());

        return tooling.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTooling(Long id, ToolingCreateRequest request) {
        // 查询工装是否存在
        Tooling tooling = toolingMapper.selectById(id);
        if (tooling == null) {
            throw new BusinessException("工装不存在");
        }

        // 更新工装信息
        if (StringUtils.hasText(request.getToolingName())) {
            tooling.setToolingName(request.getToolingName());
        }
        if (request.getToolingType() != null) {
            tooling.setToolingType(request.getToolingType());
        }
        if (request.getStatus() != null) {
            tooling.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            tooling.setRemark(request.getRemark());
        }

        toolingMapper.updateById(tooling);
        log.info("更新工装成功: id={}", tooling.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTooling(Long id) {
        // 查询工装是否存在
        Tooling tooling = toolingMapper.selectById(id);
        if (tooling == null) {
            throw new BusinessException("工装不存在");
        }

        // 检查是否有绑定关系
        Long bindCount = toolingBindMapper.selectCount(
                new LambdaQueryWrapper<ToolingBind>().eq(ToolingBind::getToolingId, id)
        );
        if (bindCount > 0) {
            throw new BusinessException("该工装存在绑定关系，无法删除");
        }

        // 逻辑删除工装
        toolingMapper.deleteById(id);
        log.info("删除工装成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteToolings(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 检查是否有绑定关系
        Long bindCount = toolingBindMapper.selectCount(
                new LambdaQueryWrapper<ToolingBind>().in(ToolingBind::getToolingId, ids)
        );
        if (bindCount > 0) {
            throw new BusinessException("部分工装存在绑定关系，无法删除");
        }

        // 批量逻辑删除工装
        toolingMapper.deleteBatchIds(ids);
        log.info("批量删除工装成功: count={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Tooling tooling = toolingMapper.selectById(id);
        if (tooling == null) {
            throw new BusinessException("工装不存在");
        }

        tooling.setStatus(status);
        toolingMapper.updateById(tooling);

        log.info("更新工装状态成功: id={}, status={}", id, status);
    }

    @Override
    public List<ToolingDTO> listToolingsByType(Integer toolingType) {
        LambdaQueryWrapper<Tooling> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tooling::getToolingType, toolingType)
                .eq(Tooling::getStatus, 1)
                .orderByAsc(Tooling::getToolingCode);

        List<Tooling> toolings = toolingMapper.selectList(wrapper);

        return toolings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ToolingDTO> listAllActiveToolings() {
        LambdaQueryWrapper<Tooling> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tooling::getStatus, 1)
                .orderByAsc(Tooling::getToolingCode);

        List<Tooling> toolings = toolingMapper.selectList(wrapper);

        return toolings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindToolingToResource(Long toolingId, Integer resourceType, Long resourceId) {
        // 校验工装是否存在
        Tooling tooling = toolingMapper.selectById(toolingId);
        if (tooling == null) {
            throw new BusinessException("工装不存在");
        }

        // 校验资源类型（2-工位，3-设备）
        if (resourceType != 2 && resourceType != 3) {
            throw new BusinessException("资源类型错误，只能绑定到工位或设备");
        }

        // 检查是否已绑定
        Long count = toolingBindMapper.selectCount(
                new LambdaQueryWrapper<ToolingBind>()
                        .eq(ToolingBind::getToolingId, toolingId)
                        .eq(ToolingBind::getResourceType, resourceType)
                        .eq(ToolingBind::getResourceId, resourceId)
        );
        if (count > 0) {
            throw new BusinessException("该工装已绑定到此资源");
        }

        // 创建绑定关系
        ToolingBind bind = new ToolingBind();
        bind.setToolingId(toolingId);
        bind.setResourceType(resourceType);
        bind.setResourceId(resourceId);
        bind.setStatus(1);

        toolingBindMapper.insert(bind);
        log.info("绑定工装到资源成功: toolingId={}, resourceType={}, resourceId={}",
                toolingId, resourceType, resourceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindToolingFromResource(Long toolingId, Integer resourceType, Long resourceId) {
        toolingBindMapper.delete(
                new LambdaQueryWrapper<ToolingBind>()
                        .eq(ToolingBind::getToolingId, toolingId)
                        .eq(ToolingBind::getResourceType, resourceType)
                        .eq(ToolingBind::getResourceId, resourceId)
        );

        log.info("解绑工装与资源成功: toolingId={}, resourceType={}, resourceId={}",
                toolingId, resourceType, resourceId);
    }

    @Override
    public List<ToolingDTO> listToolingsByResource(Integer resourceType, Long resourceId) {
        // 查询绑定关系
        List<Long> toolingIds = toolingBindMapper.selectList(
                        new LambdaQueryWrapper<ToolingBind>()
                                .eq(ToolingBind::getResourceType, resourceType)
                                .eq(ToolingBind::getResourceId, resourceId)
                                .eq(ToolingBind::getStatus, 1))
                .stream()
                .map(ToolingBind::getToolingId)
                .collect(Collectors.toList());

        if (toolingIds.isEmpty()) {
            return List.of();
        }

        // 查询工装信息
        List<Tooling> toolings = toolingMapper.selectBatchIds(toolingIds);

        return toolings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Tooling> buildQueryWrapper(ToolingQueryRequest request) {
        LambdaQueryWrapper<Tooling> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getToolingCode())) {
            wrapper.like(Tooling::getToolingCode, request.getToolingCode());
        }

        if (StringUtils.hasText(request.getToolingName())) {
            wrapper.like(Tooling::getToolingName, request.getToolingName());
        }

        if (request.getToolingType() != null) {
            wrapper.eq(Tooling::getToolingType, request.getToolingType());
        }

        if (request.getStatus() != null) {
            wrapper.eq(Tooling::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Tooling::getCreatedTime);

        return wrapper;
    }

    /**
     * 转换为ToolingDTO
     */
    private ToolingDTO convertToDTO(Tooling tooling) {
        ToolingDTO dto = new ToolingDTO();
        dto.setId(tooling.getId());
        dto.setToolingCode(tooling.getToolingCode());
        dto.setToolingName(tooling.getToolingName());
        dto.setToolingType(tooling.getToolingType());
        dto.setToolingTypeText(TOOLING_TYPE_MAP.getOrDefault(tooling.getToolingType(), "未知"));
        dto.setStatus(tooling.getStatus());
        dto.setStatusText(tooling.getStatus() == 1 ? "启用" : "禁用");
        dto.setRemark(tooling.getRemark());
        dto.setCreatedTime(tooling.getCreatedTime());
        dto.setCreatedBy(tooling.getCreatedBy());
        dto.setUpdatedTime(tooling.getUpdatedTime());
        dto.setUpdatedBy(tooling.getUpdatedBy());

        return dto;
    }
}
