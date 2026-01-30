package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.StationCreateRequest;
import com.aps.dto.request.masterdata.StationQueryRequest;
import com.aps.dto.request.masterdata.StationUpdateRequest;
import com.aps.dto.response.masterdata.StationDTO;
import com.aps.entity.masterdata.Line;
import com.aps.entity.masterdata.Station;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.LineMapper;
import com.aps.mapper.masterdata.StationMapper;
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
 * 工位服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationMapper stationMapper;
    private final LineMapper lineMapper;

    @Override
    public PageResult<StationDTO> listStations(StationQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Station> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<Station> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Station> resultPage = stationMapper.selectPage(page, wrapper);

        // 批量查询产线信息
        List<Long> lineIds = resultPage.getRecords().stream()
                .map(Station::getLineId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Line> lineMap = lineIds.isEmpty() ? Map.of() :
                lineMapper.selectBatchIds(lineIds).stream()
                        .collect(Collectors.toMap(Line::getId, l -> l));

        // 转换为DTO
        List<StationDTO> stationDTOS = resultPage.getRecords().stream()
                .map(station -> convertToDTO(station, lineMap.get(station.getLineId())))
                .collect(Collectors.toList());

        log.info("分页查询工位结果: total={}, records={}", resultPage.getTotal(), stationDTOS.size());

        return new PageResult<>(
                stationDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public StationDTO getStationById(Long id) {
        Station station = stationMapper.selectById(id);
        if (station == null) {
            throw new BusinessException("工位不存在");
        }

        // 查询产线信息
        Line line = lineMapper.selectById(station.getLineId());

        return convertToDTO(station, line);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStation(StationCreateRequest request) {
        // 校验产线是否存在
        Line line = lineMapper.selectById(request.getLineId());
        if (line == null) {
            throw new BusinessException("产线不存在");
        }

        // 校验工位编码是否重复
        Long count = stationMapper.selectCount(
                new LambdaQueryWrapper<Station>().eq(Station::getStationCode, request.getStationCode())
        );
        if (count > 0) {
            throw new BusinessException("工位编码已存在");
        }

        // 创建工位
        Station station = new Station();
        BeanUtils.copyProperties(request, station);

        stationMapper.insert(station);
        log.info("创建工位成功: id={}, stationCode={}", station.getId(), station.getStationCode());

        return station.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStation(StationUpdateRequest request) {
        // 查询工位是否存在
        Station station = stationMapper.selectById(request.getId());
        if (station == null) {
            throw new BusinessException("工位不存在");
        }

        // 更新工位信息
        if (StringUtils.hasText(request.getStationName())) {
            station.setStationName(request.getStationName());
        }
        if (request.getCapacityQtyPerShift() != null) {
            station.setCapacityQtyPerShift(request.getCapacityQtyPerShift());
        }
        if (request.getStatus() != null) {
            station.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            station.setRemark(request.getRemark());
        }

        stationMapper.updateById(station);
        log.info("更新工位成功: id={}", station.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStation(Long id) {
        // 查询工位是否存在
        Station station = stationMapper.selectById(id);
        if (station == null) {
            throw new BusinessException("工位不存在");
        }

        // TODO: 检查是否有关联的设备、工位组等

        // 逻辑删除工位
        stationMapper.deleteById(id);
        log.info("删除工位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteStations(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // TODO: 检查是否有关联的设备、工位组等

        // 批量逻辑删除工位
        stationMapper.deleteBatchIds(ids);
        log.info("批量删除工位成功: count={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Station station = stationMapper.selectById(id);
        if (station == null) {
            throw new BusinessException("工位不存在");
        }

        station.setStatus(status);
        stationMapper.updateById(station);

        log.info("更新工位状态成功: id={}, status={}", id, status);
    }

    @Override
    public List<StationDTO> listStationsByLineId(Long lineId) {
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Station::getLineId, lineId)
                .eq(Station::getStatus, 1)
                .orderByAsc(Station::getStationCode);

        List<Station> stations = stationMapper.selectList(wrapper);

        // 查询产线信息
        Line line = lineMapper.selectById(lineId);

        return stations.stream()
                .map(station -> convertToDTO(station, line))
                .collect(Collectors.toList());
    }

    @Override
    public List<StationDTO> listAllActiveStations() {
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Station::getStatus, 1)
                .orderByAsc(Station::getStationCode);

        List<Station> stations = stationMapper.selectList(wrapper);

        return stations.stream()
                .map(station -> convertToDTO(station, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<StationDTO> listStationsByIds(List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return List.of();
        }

        List<Station> stations = stationMapper.selectBatchIds(stationIds);

        // 批量查询产线信息
        List<Long> lineIds = stations.stream()
                .map(Station::getLineId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Line> lineMap = lineIds.isEmpty() ? Map.of() :
                lineMapper.selectBatchIds(lineIds).stream()
                        .collect(Collectors.toMap(Line::getId, l -> l));

        return stations.stream()
                .map(station -> convertToDTO(station, lineMap.get(station.getLineId())))
                .collect(Collectors.toList());
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Station> buildQueryWrapper(StationQueryRequest request) {
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();

        if (request.getLineId() != null) {
            wrapper.eq(Station::getLineId, request.getLineId());
        }

        if (StringUtils.hasText(request.getStationCode())) {
            wrapper.like(Station::getStationCode, request.getStationCode());
        }

        if (StringUtils.hasText(request.getStationName())) {
            wrapper.like(Station::getStationName, request.getStationName());
        }

        if (request.getStatus() != null) {
            wrapper.eq(Station::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Station::getCreatedTime);

        return wrapper;
    }

    /**
     * 转换为StationDTO
     */
    private StationDTO convertToDTO(Station station, Line line) {
        StationDTO dto = new StationDTO();
        dto.setId(station.getId());
        dto.setLineId(station.getLineId());
        if (line != null) {
            dto.setLineCode(line.getLineCode());
            dto.setLineName(line.getLineName());
        }
        dto.setStationCode(station.getStationCode());
        dto.setStationName(station.getStationName());
        dto.setCapacityQtyPerShift(station.getCapacityQtyPerShift());
        dto.setStatus(station.getStatus());
        dto.setStatusText(station.getStatus() == 1 ? "启用" : "禁用");
        dto.setRemark(station.getRemark());
        dto.setCreatedTime(station.getCreatedTime());
        dto.setCreatedBy(station.getCreatedBy());
        dto.setUpdatedTime(station.getUpdatedTime());
        dto.setUpdatedBy(station.getUpdatedBy());

        return dto;
    }
}
