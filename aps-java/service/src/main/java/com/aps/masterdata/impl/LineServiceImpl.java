package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.LineCreateRequest;
import com.aps.dto.request.masterdata.LineQueryRequest;
import com.aps.dto.request.masterdata.LineUpdateRequest;
import com.aps.dto.response.masterdata.LineDTO;
import com.aps.entity.masterdata.Line;
import com.aps.entity.masterdata.Station;
import com.aps.entity.masterdata.Workshop;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.LineMapper;
import com.aps.mapper.masterdata.StationMapper;
import com.aps.mapper.masterdata.WorkshopMapper;
import com.aps.masterdata.LineService;
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
 * 产线服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineServiceImpl implements LineService {

    private final LineMapper lineMapper;
    private final WorkshopMapper workshopMapper;
    private final StationMapper stationMapper;

    // 工艺类型映射
    private static final Map<Integer, String> PROCESS_TYPE_MAP = Map.of(
            1, "冲压",
            2, "焊装",
            3, "涂装",
            4, "总装"
    );

    @Override
    public PageResult<LineDTO> listLines(LineQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Line> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<Line> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Line> resultPage = lineMapper.selectPage(page, wrapper);

        // 批量查询车间信息
        List<Long> workshopIds = resultPage.getRecords().stream()
                .map(Line::getWorkshopId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Workshop> workshopMap = workshopIds.isEmpty() ? Map.of() :
                workshopMapper.selectBatchIds(workshopIds).stream()
                        .collect(Collectors.toMap(Workshop::getId, w -> w));

        // 批量查询工位数量
        List<Long> lineIds = resultPage.getRecords().stream()
                .map(Line::getId)
                .collect(Collectors.toList());

        Map<Long, Long> stationCountMap = lineIds.isEmpty() ? Map.of() :
                stationMapper.selectList(new LambdaQueryWrapper<Station>()
                                .in(Station::getLineId, lineIds))
                        .stream()
                        .collect(Collectors.groupingBy(Station::getLineId, Collectors.counting()));

        // 转换为DTO
        List<LineDTO> lineDTOS = resultPage.getRecords().stream()
                .map(line -> convertToDTO(line, workshopMap.get(line.getWorkshopId()),
                        stationCountMap.getOrDefault(line.getId(), 0L).intValue()))
                .collect(Collectors.toList());

        log.info("分页查询产线结果: total={}, records={}", resultPage.getTotal(), lineDTOS.size());

        return new PageResult<>(
                lineDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public LineDTO getLineById(Long id) {
        Line line = lineMapper.selectById(id);
        if (line == null) {
            throw new BusinessException("产线不存在");
        }

        // 查询车间信息
        Workshop workshop = workshopMapper.selectById(line.getWorkshopId());

        // 查询工位数量
        Long stationCount = stationMapper.selectCount(
                new LambdaQueryWrapper<Station>().eq(Station::getLineId, id)
        );

        return convertToDTO(line, workshop, stationCount.intValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLine(LineCreateRequest request) {
        // 校验车间是否存在
        Workshop workshop = workshopMapper.selectById(request.getWorkshopId());
        if (workshop == null) {
            throw new BusinessException("车间不存在");
        }

        // 校验产线编码是否重复
        Long count = lineMapper.selectCount(
                new LambdaQueryWrapper<Line>().eq(Line::getLineCode, request.getLineCode())
        );
        if (count > 0) {
            throw new BusinessException("产线编码已存在");
        }

        // 创建产线
        Line line = new Line();
        BeanUtils.copyProperties(request, line);

        lineMapper.insert(line);
        log.info("创建产线成功: id={}, lineCode={}", line.getId(), line.getLineCode());

        return line.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLine(LineUpdateRequest request) {
        // 查询产线是否存在
        Line line = lineMapper.selectById(request.getId());
        if (line == null) {
            throw new BusinessException("产线不存在");
        }

        // 更新产线信息
        if (StringUtils.hasText(request.getLineName())) {
            line.setLineName(request.getLineName());
        }
        if (request.getProcessType() != null) {
            line.setProcessType(request.getProcessType());
        }
        if (request.getStatus() != null) {
            line.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            line.setRemark(request.getRemark());
        }

        lineMapper.updateById(line);
        log.info("更新产线成功: id={}", line.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLine(Long id) {
        // 查询产线是否存在
        Line line = lineMapper.selectById(id);
        if (line == null) {
            throw new BusinessException("产线不存在");
        }

        // 检查是否有关联的工位
        Long stationCount = stationMapper.selectCount(
                new LambdaQueryWrapper<Station>().eq(Station::getLineId, id)
        );
        if (stationCount > 0) {
            throw new BusinessException("该产线下存在工位，无法删除");
        }

        // 逻辑删除产线
        lineMapper.deleteById(id);
        log.info("删除产线成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteLines(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 检查是否有关联的工位
        Long stationCount = stationMapper.selectCount(
                new LambdaQueryWrapper<Station>().in(Station::getLineId, ids)
        );
        if (stationCount > 0) {
            throw new BusinessException("部分产线下存在工位，无法删除");
        }

        // 批量逻辑删除产线
        lineMapper.deleteBatchIds(ids);
        log.info("批量删除产线成功: count={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Line line = lineMapper.selectById(id);
        if (line == null) {
            throw new BusinessException("产线不存在");
        }

        line.setStatus(status);
        lineMapper.updateById(line);

        log.info("更新产线状态成功: id={}, status={}", id, status);
    }

    @Override
    public List<LineDTO> listLinesByWorkshopId(Long workshopId) {
        LambdaQueryWrapper<Line> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Line::getWorkshopId, workshopId)
                .eq(Line::getStatus, 1)
                .orderByAsc(Line::getLineCode);

        List<Line> lines = lineMapper.selectList(wrapper);

        // 查询车间信息
        Workshop workshop = workshopMapper.selectById(workshopId);

        return lines.stream()
                .map(line -> convertToDTO(line, workshop, 0))
                .collect(Collectors.toList());
    }

    @Override
    public List<LineDTO> listLinesByProcessType(Integer processType) {
        LambdaQueryWrapper<Line> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Line::getProcessType, processType)
                .eq(Line::getStatus, 1)
                .orderByAsc(Line::getLineCode);

        List<Line> lines = lineMapper.selectList(wrapper);

        return lines.stream()
                .map(line -> convertToDTO(line, null, 0))
                .collect(Collectors.toList());
    }

    @Override
    public List<LineDTO> listAllActiveLines() {
        LambdaQueryWrapper<Line> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Line::getStatus, 1)
                .orderByAsc(Line::getLineCode);

        List<Line> lines = lineMapper.selectList(wrapper);

        return lines.stream()
                .map(line -> convertToDTO(line, null, 0))
                .collect(Collectors.toList());
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Line> buildQueryWrapper(LineQueryRequest request) {
        LambdaQueryWrapper<Line> wrapper = new LambdaQueryWrapper<>();

        if (request.getWorkshopId() != null) {
            wrapper.eq(Line::getWorkshopId, request.getWorkshopId());
        }

        if (StringUtils.hasText(request.getLineCode())) {
            wrapper.like(Line::getLineCode, request.getLineCode());
        }

        if (StringUtils.hasText(request.getLineName())) {
            wrapper.like(Line::getLineName, request.getLineName());
        }

        if (request.getProcessType() != null) {
            wrapper.eq(Line::getProcessType, request.getProcessType());
        }

        if (request.getStatus() != null) {
            wrapper.eq(Line::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Line::getCreatedTime);

        return wrapper;
    }

    /**
     * 转换为LineDTO
     */
    private LineDTO convertToDTO(Line line, Workshop workshop, Integer stationCount) {
        LineDTO dto = new LineDTO();
        dto.setId(line.getId());
        dto.setWorkshopId(line.getWorkshopId());
        if (workshop != null) {
            dto.setWorkshopCode(workshop.getWorkshopCode());
            dto.setWorkshopName(workshop.getWorkshopName());
        }
        dto.setLineCode(line.getLineCode());
        dto.setLineName(line.getLineName());
        dto.setProcessType(line.getProcessType());
        dto.setProcessTypeText(PROCESS_TYPE_MAP.getOrDefault(line.getProcessType(), "未知"));
        dto.setStatus(line.getStatus());
        dto.setStatusText(line.getStatus() == 1 ? "启用" : "禁用");
        dto.setStationCount(stationCount);
        dto.setRemark(line.getRemark());
        dto.setCreatedTime(line.getCreatedTime());
        dto.setUpdatedTime(line.getUpdatedTime());
        dto.setCreatedBy(line.getCreatedBy());
        dto.setUpdatedBy(line.getUpdatedBy());

        return dto;
    }
}
