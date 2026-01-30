package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.EquipmentCreateRequest;
import com.aps.dto.request.masterdata.EquipmentQueryRequest;
import com.aps.dto.response.masterdata.EquipmentDTO;
import com.aps.entity.masterdata.Equipment;
import com.aps.entity.masterdata.Station;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.EquipmentMapper;
import com.aps.mapper.masterdata.StationMapper;
import com.aps.masterdata.EquipmentService;
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
 * 设备服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentMapper equipmentMapper;
    private final StationMapper stationMapper;

    @Override
    public PageResult<EquipmentDTO> listEquipments(EquipmentQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Equipment> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<Equipment> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Equipment> resultPage = equipmentMapper.selectPage(page, wrapper);

        // 批量查询工位信息
        List<Long> stationIds = resultPage.getRecords().stream()
                .map(Equipment::getStationId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Station> stationMap = stationIds.isEmpty() ? Map.of() :
                stationMapper.selectBatchIds(stationIds).stream()
                        .collect(Collectors.toMap(Station::getId, s -> s));

        // 转换为DTO
        List<EquipmentDTO> equipmentDTOS = resultPage.getRecords().stream()
                .map(equipment -> convertToDTO(equipment, stationMap.get(equipment.getStationId())))
                .collect(Collectors.toList());

        log.info("分页查询设备结果: total={}, records={}", resultPage.getTotal(), equipmentDTOS.size());

        return new PageResult<>(
                equipmentDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public EquipmentDTO getEquipmentById(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }

        // 查询工位信息
        Station station = stationMapper.selectById(equipment.getStationId());

        return convertToDTO(equipment, station);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEquipment(EquipmentCreateRequest request) {
        // 校验工位是否存在
        Station station = stationMapper.selectById(request.getStationId());
        if (station == null) {
            throw new BusinessException("工位不存在");
        }

        // 校验设备编码是否重复
        Long count = equipmentMapper.selectCount(
                new LambdaQueryWrapper<Equipment>().eq(Equipment::getEquipCode, request.getEquipCode())
        );
        if (count > 0) {
            throw new BusinessException("设备编码已存在");
        }

        // 创建设备
        Equipment equipment = new Equipment();
        BeanUtils.copyProperties(request, equipment);

        equipmentMapper.insert(equipment);
        log.info("创建设备成功: id={}, equipCode={}", equipment.getId(), equipment.getEquipCode());

        return equipment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEquipment(Long id, EquipmentCreateRequest request) {
        // 查询设备是否存在
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }

        // 更新设备信息
        if (StringUtils.hasText(request.getEquipName())) {
            equipment.setEquipName(request.getEquipName());
        }
        if (request.getStatus() != null) {
            equipment.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            equipment.setRemark(request.getRemark());
        }

        equipmentMapper.updateById(equipment);
        log.info("更新设备成功: id={}", equipment.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEquipment(Long id) {
        // 查询设备是否存在
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }

        // TODO: 检查是否有关联的工装绑定等

        // 逻辑删除设备
        equipmentMapper.deleteById(id);
        log.info("删除设备成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteEquipments(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // TODO: 检查是否有关联的工装绑定等

        // 批量逻辑删除设备
        equipmentMapper.deleteBatchIds(ids);
        log.info("批量删除设备成功: count={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }

        equipment.setStatus(status);
        equipmentMapper.updateById(equipment);

        log.info("更新设备状态成功: id={}, status={}", id, status);
    }

    @Override
    public List<EquipmentDTO> listEquipmentsByStationId(Long stationId) {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Equipment::getStationId, stationId)
                .eq(Equipment::getStatus, 1)
                .orderByAsc(Equipment::getEquipCode);

        List<Equipment> equipments = equipmentMapper.selectList(wrapper);

        // 查询工位信息
        Station station = stationMapper.selectById(stationId);

        return equipments.stream()
                .map(equipment -> convertToDTO(equipment, station))
                .collect(Collectors.toList());
    }

    @Override
    public List<EquipmentDTO> listAllActiveEquipments() {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Equipment::getStatus, 1)
                .orderByAsc(Equipment::getEquipCode);

        List<Equipment> equipments = equipmentMapper.selectList(wrapper);

        return equipments.stream()
                .map(equipment -> convertToDTO(equipment, null))
                .collect(Collectors.toList());
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Equipment> buildQueryWrapper(EquipmentQueryRequest request) {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();

        if (request.getStationId() != null) {
            wrapper.eq(Equipment::getStationId, request.getStationId());
        }

        if (StringUtils.hasText(request.getEquipCode())) {
            wrapper.like(Equipment::getEquipCode, request.getEquipCode());
        }

        if (StringUtils.hasText(request.getEquipName())) {
            wrapper.like(Equipment::getEquipName, request.getEquipName());
        }

        if (request.getStatus() != null) {
            wrapper.eq(Equipment::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Equipment::getCreatedTime);

        return wrapper;
    }

    /**
     * 转换为EquipmentDTO
     */
    private EquipmentDTO convertToDTO(Equipment equipment, Station station) {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(equipment.getId());
        dto.setStationId(equipment.getStationId());
        if (station != null) {
            dto.setStationCode(station.getStationCode());
            dto.setStationName(station.getStationName());
        }
        dto.setEquipCode(equipment.getEquipCode());
        dto.setEquipName(equipment.getEquipName());
        dto.setStatus(equipment.getStatus());
        dto.setStatusText(equipment.getStatus() == 1 ? "启用" : "禁用");
        dto.setRemark(equipment.getRemark());
        dto.setCreatedTime(equipment.getCreatedTime());
        dto.setCreatedBy(equipment.getCreatedBy());
        dto.setUpdatedTime(equipment.getUpdatedTime());
        dto.setUpdatedBy(equipment.getUpdatedBy());

        return dto;
    }
}
