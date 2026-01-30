package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.StationGroupCreateRequest;
import com.aps.dto.request.masterdata.StationGroupQueryRequest;
import com.aps.dto.request.masterdata.StationGroupUpdateRequest;
import com.aps.dto.response.masterdata.StationDTO;
import com.aps.dto.response.masterdata.StationGroupDTO;
import com.aps.entity.masterdata.StationGroup;
import com.aps.entity.masterdata.StationGroupRel;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.StationGroupMapper;
import com.aps.mapper.masterdata.StationGroupRelMapper;
import com.aps.masterdata.StationGroupService;
import com.aps.masterdata.StationService;
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
 * 工位组服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StationGroupServiceImpl implements StationGroupService {

    private final StationGroupMapper stationGroupMapper;
    private final StationGroupRelMapper stationGroupRelMapper;
    private final StationService stationService;

    @Override
    public PageResult<StationGroupDTO> listStationGroups(StationGroupQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<StationGroup> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<StationGroup> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<StationGroup> resultPage = stationGroupMapper.selectPage(page, wrapper);

        // 批量查询工位数量
        List<Long> groupIds = resultPage.getRecords().stream()
                .map(StationGroup::getId)
                .collect(Collectors.toList());

        Map<Long, Long> stationCountMap = groupIds.isEmpty() ? Map.of() :
                stationGroupRelMapper.selectList(new LambdaQueryWrapper<StationGroupRel>()
                                .in(StationGroupRel::getGroupId, groupIds))
                        .stream()
                        .collect(Collectors.groupingBy(StationGroupRel::getGroupId, Collectors.counting()));

        // 转换为DTO
        List<StationGroupDTO> groupDTOS = resultPage.getRecords().stream()
                .map(group -> convertToDTO(group, stationCountMap.getOrDefault(group.getId(), 0L).intValue(), null))
                .collect(Collectors.toList());

        log.info("分页查询工位组结果: total={}, records={}", resultPage.getTotal(), groupDTOS.size());

        return new PageResult<>(
                groupDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public StationGroupDTO getStationGroupById(Long id) {
        StationGroup group = stationGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("工位组不存在");
        }

        // 查询关联的工位
        List<Long> stationIds = stationGroupRelMapper.selectList(
                        new LambdaQueryWrapper<StationGroupRel>().eq(StationGroupRel::getGroupId, id))
                .stream()
                .map(StationGroupRel::getStationId)
                .collect(Collectors.toList());

        return convertToDTO(group, stationIds.size(),
                stationIds.isEmpty() ? null : stationService.listStationsByIds(stationIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStationGroup(StationGroupCreateRequest request) {
        // 校验工位组编码是否重复
        Long count = stationGroupMapper.selectCount(
                new LambdaQueryWrapper<StationGroup>().eq(StationGroup::getGroupCode, request.getGroupCode())
        );
        if (count > 0) {
            throw new BusinessException("工位组编码已存在");
        }

        // 创建工位组
        StationGroup group = new StationGroup();
        BeanUtils.copyProperties(request, group);

        stationGroupMapper.insert(group);
        log.info("创建工位组成功: id={}, groupCode={}", group.getId(), group.getGroupCode());

        // 添加工位关联
        if (request.getStationIds() != null && !request.getStationIds().isEmpty()) {
            addStationsToGroup(group.getId(), request.getStationIds());
        }

        return group.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStationGroup(StationGroupUpdateRequest request) {
        // 查询工位组是否存在
        StationGroup group = stationGroupMapper.selectById(request.getId());
        if (group == null) {
            throw new BusinessException("工位组不存在");
        }

        // 更新工位组信息
        if (StringUtils.hasText(request.getGroupName())) {
            group.setGroupName(request.getGroupName());
        }
        if (request.getStatus() != null) {
            group.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            group.setRemark(request.getRemark());
        }

        stationGroupMapper.updateById(group);
        log.info("更新工位组成功: id={}", group.getId());

        // 如果提供了工位列表，则全量更新
        if (request.getStationIds() != null) {
            // 删除旧关联
            stationGroupRelMapper.delete(
                    new LambdaQueryWrapper<StationGroupRel>().eq(StationGroupRel::getGroupId, request.getId())
            );

            // 添加新关联
            if (!request.getStationIds().isEmpty()) {
                addStationsToGroup(request.getId(), request.getStationIds());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStationGroup(Long id) {
        // 查询工位组是否存在
        StationGroup group = stationGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("工位组不存在");
        }

        // 删除工位关联
        stationGroupRelMapper.delete(
                new LambdaQueryWrapper<StationGroupRel>().eq(StationGroupRel::getGroupId, id)
        );

        // 逻辑删除工位组
        stationGroupMapper.deleteById(id);
        log.info("删除工位组成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteStationGroups(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 删除工位关联
        stationGroupRelMapper.delete(
                new LambdaQueryWrapper<StationGroupRel>().in(StationGroupRel::getGroupId, ids)
        );

        // 批量逻辑删除工位组
        stationGroupMapper.deleteBatchIds(ids);
        log.info("批量删除工位组成功: count={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        StationGroup group = stationGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("工位组不存在");
        }

        group.setStatus(status);
        stationGroupMapper.updateById(group);

        log.info("更新工位组状态成功: id={}, status={}", id, status);
    }

    @Override
    public List<StationGroupDTO> listAllActiveStationGroups() {
        LambdaQueryWrapper<StationGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StationGroup::getStatus, 1)
                .orderByAsc(StationGroup::getGroupCode);

        List<StationGroup> groups = stationGroupMapper.selectList(wrapper);

        return groups.stream()
                .map(group -> convertToDTO(group, 0, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStationsToGroup(Long groupId, List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return;
        }

        // 校验工位组是否存在
        StationGroup group = stationGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("工位组不存在");
        }

        // 查询已存在的关联
        List<Long> existingStationIds = stationGroupRelMapper.selectList(
                        new LambdaQueryWrapper<StationGroupRel>().eq(StationGroupRel::getGroupId, groupId))
                .stream()
                .map(StationGroupRel::getStationId)
                .collect(Collectors.toList());

        // 过滤出需要新增的工位
        List<Long> newStationIds = stationIds.stream()
                .filter(stationId -> !existingStationIds.contains(stationId))
                .collect(Collectors.toList());

        // 批量插入关联
        for (Long stationId : newStationIds) {
            StationGroupRel rel = new StationGroupRel();
            rel.setGroupId(groupId);
            rel.setStationId(stationId);
            stationGroupRelMapper.insert(rel);
        }

        log.info("为工位组添加工位成功: groupId={}, count={}", groupId, newStationIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeStationsFromGroup(Long groupId, List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return;
        }

        stationGroupRelMapper.delete(
                new LambdaQueryWrapper<StationGroupRel>()
                        .eq(StationGroupRel::getGroupId, groupId)
                        .in(StationGroupRel::getStationId, stationIds)
        );

        log.info("从工位组移除工位成功: groupId={}, count={}", groupId, stationIds.size());
    }

    @Override
    public List<Long> getStationIdsByGroupCode(String groupCode) {
        // 查询工位组
        StationGroup group = stationGroupMapper.selectOne(
                new LambdaQueryWrapper<StationGroup>().eq(StationGroup::getGroupCode, groupCode)
        );

        if (group == null) {
            return List.of();
        }

        // 查询关联的工位ID
        return stationGroupRelMapper.selectList(
                        new LambdaQueryWrapper<StationGroupRel>().eq(StationGroupRel::getGroupId, group.getId()))
                .stream()
                .map(StationGroupRel::getStationId)
                .collect(Collectors.toList());
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<StationGroup> buildQueryWrapper(StationGroupQueryRequest request) {
        LambdaQueryWrapper<StationGroup> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getGroupCode())) {
            wrapper.like(StationGroup::getGroupCode, request.getGroupCode());
        }

        if (StringUtils.hasText(request.getGroupName())) {
            wrapper.like(StationGroup::getGroupName, request.getGroupName());
        }

        if (request.getStatus() != null) {
            wrapper.eq(StationGroup::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(StationGroup::getCreatedTime);

        return wrapper;
    }

    /**
     * 转换为StationGroupDTO
     */
    private StationGroupDTO convertToDTO(StationGroup group, Integer stationCount,
                                         List<StationDTO> stations) {
        StationGroupDTO dto = new StationGroupDTO();
        dto.setId(group.getId());
        dto.setGroupCode(group.getGroupCode());
        dto.setGroupName(group.getGroupName());
        dto.setStatus(group.getStatus());
        dto.setStatusText(group.getStatus() == 1 ? "启用" : "禁用");
        dto.setStationCount(stationCount);
        dto.setStations(stations);
        dto.setRemark(group.getRemark());
        dto.setCreatedTime(group.getCreatedTime());
        dto.setUpdatedTime(group.getUpdatedTime());
        dto.setCreatedBy(group.getCreatedBy());
        dto.setUpdatedBy(group.getUpdatedBy());

        return dto;
    }
}
