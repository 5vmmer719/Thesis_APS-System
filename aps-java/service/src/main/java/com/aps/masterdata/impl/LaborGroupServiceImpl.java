package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.LaborGroupCreateRequest;
import com.aps.dto.request.masterdata.LaborGroupQueryRequest;
import com.aps.dto.response.masterdata.LaborGroupDTO;
import com.aps.dto.response.masterdata.StationDTO;
import com.aps.entity.masterdata.LaborGroup;
import com.aps.entity.masterdata.LaborGroupRel;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.LaborGroupMapper;
import com.aps.mapper.masterdata.LaborGroupRelMapper;
import com.aps.masterdata.LaborGroupService;
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
 * 人力组服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LaborGroupServiceImpl implements LaborGroupService {

    private final LaborGroupMapper laborGroupMapper;
    private final LaborGroupRelMapper laborGroupRelMapper;
    private final StationService stationService;

    @Override
    public PageResult<LaborGroupDTO> listLaborGroups(LaborGroupQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<LaborGroup> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<LaborGroup> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<LaborGroup> resultPage = laborGroupMapper.selectPage(page, wrapper);

        // 批量查询工位数量
        List<Long> laborIds = resultPage.getRecords().stream()
                .map(LaborGroup::getId)
                .collect(Collectors.toList());

        Map<Long, Long> stationCountMap = laborIds.isEmpty() ? Map.of() :
                laborGroupRelMapper.selectList(new LambdaQueryWrapper<LaborGroupRel>()
                                .in(LaborGroupRel::getLaborId, laborIds))
                        .stream()
                        .collect(Collectors.groupingBy(LaborGroupRel::getLaborId, Collectors.counting()));

        // 转换为DTO
        List<LaborGroupDTO> laborGroupDTOS = resultPage.getRecords().stream()
                .map(laborGroup -> convertToDTO(laborGroup, stationCountMap.getOrDefault(laborGroup.getId(), 0L).intValue(), null))
                .collect(Collectors.toList());

        log.info("分页查询人力组结果: total={}, records={}", resultPage.getTotal(), laborGroupDTOS.size());

        return new PageResult<>(
                laborGroupDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public LaborGroupDTO getLaborGroupById(Long id) {
        LaborGroup laborGroup = laborGroupMapper.selectById(id);
        if (laborGroup == null) {
            throw new BusinessException("人力组不存在");
        }

        // 查询关联的工位
        List<Long> stationIds = laborGroupRelMapper.selectList(
                        new LambdaQueryWrapper<LaborGroupRel>().eq(LaborGroupRel::getLaborId, id))
                .stream()
                .map(LaborGroupRel::getStationId)
                .collect(Collectors.toList());

        return convertToDTO(laborGroup, stationIds.size(),
                stationIds.isEmpty() ? null : stationService.listStationsByIds(stationIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLaborGroup(LaborGroupCreateRequest request) {
        // 校验人力组编码是否重复
        Long count = laborGroupMapper.selectCount(
                new LambdaQueryWrapper<LaborGroup>().eq(LaborGroup::getLaborCode, request.getLaborCode())
        );
        if (count > 0) {
            throw new BusinessException("人力组编码已存在");
        }

        // 创建人力组
        LaborGroup laborGroup = new LaborGroup();
        BeanUtils.copyProperties(request, laborGroup);

        laborGroupMapper.insert(laborGroup);
        log.info("创建人力组成功: id={}, laborCode={}", laborGroup.getId(), laborGroup.getLaborCode());

        // 添加工位关联
        if (request.getStationIds() != null && !request.getStationIds().isEmpty()) {
            addStationsToLaborGroup(laborGroup.getId(), request.getStationIds());
        }

        return laborGroup.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLaborGroup(Long id, LaborGroupCreateRequest request) {
        // 查询人力组是否存在
        LaborGroup laborGroup = laborGroupMapper.selectById(id);
        if (laborGroup == null) {
            throw new BusinessException("人力组不存在");
        }

        // 更新人力组信息
        if (StringUtils.hasText(request.getLaborName())) {
            laborGroup.setLaborName(request.getLaborName());
        }
        if (request.getHeadcount() != null) {
            laborGroup.setHeadcount(request.getHeadcount());
        }
        if (request.getStatus() != null) {
            laborGroup.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            laborGroup.setRemark(request.getRemark());
        }

        laborGroupMapper.updateById(laborGroup);

        // 如果提供了工位列表，则全量更新
        if (request.getStationIds() != null) {
            // 删除旧关联
            laborGroupRelMapper.delete(
                    new LambdaQueryWrapper<LaborGroupRel>().eq(LaborGroupRel::getLaborId, id)
            );

            // 添加新关联
            if (!request.getStationIds().isEmpty()) {
                addStationsToLaborGroup(id, request.getStationIds());
            }
        }

        log.info("更新人力组成功: id={}", laborGroup.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLaborGroup(Long id) {
        // 查询人力组是否存在
        LaborGroup laborGroup = laborGroupMapper.selectById(id);
        if (laborGroup == null) {
            throw new BusinessException("人力组不存在");
        }

        // 删除工位关联
        laborGroupRelMapper.delete(
                new LambdaQueryWrapper<LaborGroupRel>().eq(LaborGroupRel::getLaborId, id)
        );

        // 逻辑删除人力组
        laborGroupMapper.deleteById(id);
        log.info("删除人力组成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteLaborGroups(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 删除工位关联
        laborGroupRelMapper.delete(
                new LambdaQueryWrapper<LaborGroupRel>().in(LaborGroupRel::getLaborId, ids)
        );

        // 批量逻辑删除人力组
        laborGroupMapper.deleteBatchIds(ids);
        log.info("批量删除人力组成功: count={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        LaborGroup laborGroup = laborGroupMapper.selectById(id);
        if (laborGroup == null) {
            throw new BusinessException("人力组不存在");
        }

        laborGroup.setStatus(status);
        laborGroupMapper.updateById(laborGroup);

        log.info("更新人力组状态成功: id={}, status={}", id, status);
    }

    @Override
    public List<LaborGroupDTO> listAllActiveLaborGroups() {
        LambdaQueryWrapper<LaborGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LaborGroup::getStatus, 1)
                .orderByAsc(LaborGroup::getLaborCode);

        List<LaborGroup> laborGroups = laborGroupMapper.selectList(wrapper);

        return laborGroups.stream()
                .map(laborGroup -> convertToDTO(laborGroup, 0, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStationsToLaborGroup(Long laborId, List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return;
        }

        // 校验人力组是否存在
        LaborGroup laborGroup = laborGroupMapper.selectById(laborId);
        if (laborGroup == null) {
            throw new BusinessException("人力组不存在");
        }

        // 查询已存在的关联
        List<Long> existingStationIds = laborGroupRelMapper.selectList(
                        new LambdaQueryWrapper<LaborGroupRel>().eq(LaborGroupRel::getLaborId, laborId))
                .stream()
                .map(LaborGroupRel::getStationId)
                .collect(Collectors.toList());

        // 过滤出需要新增的工位
        List<Long> newStationIds = stationIds.stream()
                .filter(stationId -> !existingStationIds.contains(stationId))
                .collect(Collectors.toList());

        // 批量插入关联
        for (Long stationId : newStationIds) {
            LaborGroupRel rel = new LaborGroupRel();
            rel.setLaborId(laborId);
            rel.setStationId(stationId);
            laborGroupRelMapper.insert(rel);
        }

        log.info("为人力组添加工位成功: laborId={}, count={}", laborId, newStationIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeStationsFromLaborGroup(Long laborId, List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return;
        }

        laborGroupRelMapper.delete(
                new LambdaQueryWrapper<LaborGroupRel>()
                        .eq(LaborGroupRel::getLaborId, laborId)
                        .in(LaborGroupRel::getStationId, stationIds)
        );

        log.info("从人力组移除工位成功: laborId={}, count={}", laborId, stationIds.size());
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<LaborGroup> buildQueryWrapper(LaborGroupQueryRequest request) {
        LambdaQueryWrapper<LaborGroup> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getLaborCode())) {
            wrapper.like(LaborGroup::getLaborCode, request.getLaborCode());
        }

        if (StringUtils.hasText(request.getLaborName())) {
            wrapper.like(LaborGroup::getLaborName, request.getLaborName());
        }

        if (request.getStatus() != null) {
            wrapper.eq(LaborGroup::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(LaborGroup::getCreatedTime);

        return wrapper;
    }

    /**
     * 转换为LaborGroupDTO
     */
    private LaborGroupDTO convertToDTO(LaborGroup laborGroup, Integer stationCount,
                                       List<StationDTO> stations) {
        LaborGroupDTO dto = new LaborGroupDTO();
        dto.setId(laborGroup.getId());
        dto.setLaborCode(laborGroup.getLaborCode());
        dto.setLaborName(laborGroup.getLaborName());
        dto.setHeadcount(laborGroup.getHeadcount());
        dto.setStatus(laborGroup.getStatus());
        dto.setStatusText(laborGroup.getStatus() == 1 ? "启用" : "禁用");
        dto.setStationCount(stationCount);
        dto.setStations(stations);
        dto.setRemark(laborGroup.getRemark());
        dto.setCreatedTime(laborGroup.getCreatedTime());
        dto.setCreatedBy(laborGroup.getCreatedBy());
        dto.setUpdatedTime(laborGroup.getUpdatedTime());
        dto.setUpdatedBy(laborGroup.getUpdatedBy());

        return dto;
    }
}
