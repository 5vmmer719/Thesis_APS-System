package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.WorkshopCreateRequest;
import com.aps.dto.request.masterdata.WorkshopQueryRequest;
import com.aps.dto.request.masterdata.WorkshopUpdateRequest;
import com.aps.dto.response.masterdata.WorkshopDTO;
import com.aps.entity.masterdata.Line;
import com.aps.entity.masterdata.Workshop;
import com.aps.masterdata.WorkshopService;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.LineMapper;
import com.aps.mapper.masterdata.WorkshopMapper;
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
 * 车间服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkshopServiceImpl implements WorkshopService {

    private final WorkshopMapper workshopMapper;
    private final LineMapper lineMapper;

    // 工艺类型映射
    private static final Map<Integer, String> PROCESS_TYPE_MAP = Map.of(
            1, "冲压",
            2, "焊装",
            3, "涂装",
            4, "总装"
    );

    @Override
    public PageResult<WorkshopDTO> listWorkshops(WorkshopQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Workshop> wrapper = buildQueryWrapper(request);

        // 分页查询
        Page<Workshop> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Workshop> resultPage = workshopMapper.selectPage(page, wrapper);

        // 批量查询每个车间的产线数量
        List<Long> workshopIds = resultPage.getRecords().stream()
                .map(Workshop::getId)
                .collect(Collectors.toList());

        Map<Long, Long> lineCountMap = workshopIds.isEmpty() ? Map.of() :
                lineMapper.selectList(new LambdaQueryWrapper<Line>()
                                .in(Line::getWorkshopId, workshopIds))
                        .stream()
                        .collect(Collectors.groupingBy(Line::getWorkshopId, Collectors.counting()));

        // 转换为DTO
        List<WorkshopDTO> workshopDTOS = resultPage.getRecords().stream()
                .map(workshop -> convertToDTO(workshop, lineCountMap.getOrDefault(workshop.getId(), 0L).intValue()))
                .collect(Collectors.toList());

        log.info("分页查询车间结果: total={}, records={}", resultPage.getTotal(), workshopDTOS.size());

        return new PageResult<>(
                workshopDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public WorkshopDTO getWorkshopById(Long id) {
        Workshop workshop = workshopMapper.selectById(id);
        if (workshop == null) {
            throw new BusinessException("车间不存在");
        }

        // 查询产线数量
        Long lineCount = lineMapper.selectCount(
                new LambdaQueryWrapper<Line>().eq(Line::getWorkshopId, id)
        );

        return convertToDTO(workshop, lineCount.intValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkshop(WorkshopCreateRequest request) {
        // 校验车间编码是否重复
        Long count = workshopMapper.selectCount(
                new LambdaQueryWrapper<Workshop>().eq(Workshop::getWorkshopCode, request.getWorkshopCode())
        );
        if (count > 0) {
            throw new BusinessException("车间编码已存在");
        }

        // 创建车间
        Workshop workshop = new Workshop();
        BeanUtils.copyProperties(request, workshop);

        workshopMapper.insert(workshop);
        log.info("创建车间成功: id={}, workshopCode={}", workshop.getId(), workshop.getWorkshopCode());

        return workshop.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkshop(WorkshopUpdateRequest request) {
        // 查询车间是否存在
        Workshop workshop = workshopMapper.selectById(request.getId());
        if (workshop == null) {
            throw new BusinessException("车间不存在");
        }

        // 更新车间信息
        if (StringUtils.hasText(request.getWorkshopName())) {
            workshop.setWorkshopName(request.getWorkshopName());
        }
        if (request.getProcessType() != null) {
            workshop.setProcessType(request.getProcessType());
        }
        if (request.getStatus() != null) {
            workshop.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            workshop.setRemark(request.getRemark());
        }

        workshopMapper.updateById(workshop);
        log.info("更新车间成功: id={}", workshop.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkshop(Long id) {
        // 查询车间是否存在
        Workshop workshop = workshopMapper.selectById(id);
        if (workshop == null) {
            throw new BusinessException("车间不存在");
        }

        // 检查是否有关联的产线
        Long lineCount = lineMapper.selectCount(
                new LambdaQueryWrapper<Line>().eq(Line::getWorkshopId, id)
        );
        if (lineCount > 0) {
            throw new BusinessException("该车间下存在产线，无法删除");
        }

        // 逻辑删除车间
        workshopMapper.deleteById(id);
        log.info("删除车间成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteWorkshops(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 检查是否有关联的产线
        Long lineCount = lineMapper.selectCount(
                new LambdaQueryWrapper<Line>().in(Line::getWorkshopId, ids)
        );
        if (lineCount > 0) {
            throw new BusinessException("部分车间下存在产线，无法删除");
        }

        // 批量逻辑删除车间
        workshopMapper.deleteBatchIds(ids);
        log.info("批量删除车间成功: count={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Workshop workshop = workshopMapper.selectById(id);
        if (workshop == null) {
            throw new BusinessException("车间不存在");
        }

        workshop.setStatus(status);
        workshopMapper.updateById(workshop);

        log.info("更新车间状态成功: id={}, status={}", id, status);
    }

    @Override
    public List<WorkshopDTO> listAllActiveWorkshops() {
        LambdaQueryWrapper<Workshop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Workshop::getStatus, 1)
                .orderByAsc(Workshop::getWorkshopCode);

        List<Workshop> workshops = workshopMapper.selectList(wrapper);

        return workshops.stream()
                .map(workshop -> convertToDTO(workshop, 0))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkshopDTO> listWorkshopsByProcessType(Integer processType) {
        LambdaQueryWrapper<Workshop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Workshop::getProcessType, processType)
                .eq(Workshop::getStatus, 1)
                .orderByAsc(Workshop::getWorkshopCode);

        List<Workshop> workshops = workshopMapper.selectList(wrapper);

        return workshops.stream()
                .map(workshop -> convertToDTO(workshop, 0))
                .collect(Collectors.toList());
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Workshop> buildQueryWrapper(WorkshopQueryRequest request) {
        LambdaQueryWrapper<Workshop> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getWorkshopCode())) {
            wrapper.like(Workshop::getWorkshopCode, request.getWorkshopCode());
        }

        if (StringUtils.hasText(request.getWorkshopName())) {
            wrapper.like(Workshop::getWorkshopName, request.getWorkshopName());
        }

        if (request.getProcessType() != null) {
            wrapper.eq(Workshop::getProcessType, request.getProcessType());
        }

        if (request.getStatus() != null) {
            wrapper.eq(Workshop::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Workshop::getCreatedTime);

        return wrapper;
    }

    /**
     * 转换为WorkshopDTO
     */
    private WorkshopDTO convertToDTO(Workshop workshop, Integer lineCount) {
        WorkshopDTO dto = new WorkshopDTO();
        dto.setId(workshop.getId());
        dto.setWorkshopCode(workshop.getWorkshopCode());
        dto.setWorkshopName(workshop.getWorkshopName());
        dto.setProcessType(workshop.getProcessType());
        dto.setProcessTypeText(workshop.getProcessType() != null ?
                PROCESS_TYPE_MAP.getOrDefault(workshop.getProcessType(), "未知") : null);
        dto.setStatus(workshop.getStatus());
        dto.setStatusText(workshop.getStatus() == 1 ? "启用" : "禁用");
        dto.setLineCount(lineCount);
        dto.setRemark(workshop.getRemark());
        dto.setCreatedTime(workshop.getCreatedTime());
        dto.setCreatedBy(workshop.getCreatedBy());
        dto.setUpdatedTime(workshop.getUpdatedTime());
        dto.setUpdatedBy(workshop.getUpdatedBy());

        return dto;
    }
}
