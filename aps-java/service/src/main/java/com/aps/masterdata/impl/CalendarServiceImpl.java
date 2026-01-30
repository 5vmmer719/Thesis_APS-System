package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.HolidayCreateRequest;
import com.aps.dto.request.masterdata.MaintenanceWindowCreateRequest;
import com.aps.dto.response.masterdata.*;
import com.aps.entity.masterdata.CalDay;
import com.aps.entity.masterdata.CalHoliday;
import com.aps.entity.masterdata.CalMaintenanceWindow;
import com.aps.entity.masterdata.CalShift;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.CalDayMapper;
import com.aps.mapper.masterdata.CalHolidayMapper;
import com.aps.mapper.masterdata.CalMaintenanceWindowMapper;
import com.aps.mapper.masterdata.CalShiftMapper;
import com.aps.masterdata.CalendarService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日历服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final CalDayMapper calDayMapper;
    private final CalShiftMapper calShiftMapper;
    private final CalMaintenanceWindowMapper maintenanceWindowMapper;
    private final CalHolidayMapper holidayMapper;

    // ==================== 资源日历（日）====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetWorkdays(Integer resourceType, Long resourceId,
                                 LocalDate startDate, LocalDate endDate,
                                 Integer isWorkday, List<LocalDate> excludeDates) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }

        // 查询节假日
        List<LocalDate> holidays = excludeDates != null ? excludeDates : new ArrayList<>();

        // 删除已存在的日历记录
        calDayMapper.delete(new LambdaQueryWrapper<CalDay>()
                .eq(CalDay::getResourceType, resourceType)
                .eq(CalDay::getResourceId, resourceId)
                .between(CalDay::getBizDate, startDate, endDate));

        // 批量插入日历记录
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 跳过排除日期
            if (!holidays.contains(currentDate)) {
                CalDay calDay = new CalDay();
                calDay.setResourceType(resourceType);
                calDay.setResourceId(resourceId);
                calDay.setBizDate(currentDate);
                calDay.setIsWorkday(isWorkday);

                calDayMapper.insert(calDay);
            }

            currentDate = currentDate.plusDays(1);
        }

        log.info("批量设置资源工作日成功: resourceType={}, resourceId={}, startDate={}, endDate={}",
                resourceType, resourceId, startDate, endDate);
    }

    @Override
    public List<CalDayDTO> listCalDays(Integer resourceType, Long resourceId,
                                       LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<CalDay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalDay::getResourceType, resourceType)
                .eq(CalDay::getResourceId, resourceId)
                .between(CalDay::getBizDate, startDate, endDate)
                .orderByAsc(CalDay::getBizDate);

        List<CalDay> calDays = calDayMapper.selectList(wrapper);

        return calDays.stream()
                .map(this::convertCalDayToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCalDay(Long id) {
        CalDay calDay = calDayMapper.selectById(id);
        if (calDay == null) {
            throw new BusinessException("日历记录不存在");
        }

        // 删除关联的班次
        calShiftMapper.delete(new LambdaQueryWrapper<CalShift>().eq(CalShift::getDayId, id));

        // 删除日历记录
        calDayMapper.deleteById(id);

        log.info("删除资源日历成功: id={}", id);
    }

    // ==================== 资源日历（班次）====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetShifts(Long dayId, List<ShiftConfigDTO> shifts) {
        // 校验日历日是否存在
        CalDay calDay = calDayMapper.selectById(dayId);
        if (calDay == null) {
            throw new BusinessException("日历记录不存在");
        }

        // 删除已存在的班次
        calShiftMapper.delete(new LambdaQueryWrapper<CalShift>().eq(CalShift::getDayId, dayId));

        // 批量插入班次
        for (ShiftConfigDTO shiftConfig : shifts) {
            CalShift calShift = new CalShift();
            calShift.setDayId(dayId);
            calShift.setShiftCode(shiftConfig.getShiftCode());
            calShift.setStartTime(shiftConfig.getStartTime());
            calShift.setEndTime(shiftConfig.getEndTime());
            calShift.setCapacityQty(shiftConfig.getCapacityQty());
            calShift.setStatus(1);

            calShiftMapper.insert(calShift);
        }

        log.info("批量设置班次成功: dayId={}, count={}", dayId, shifts.size());
    }

    @Override
    public List<CalShiftDTO> listShiftsByDayId(Long dayId) {
        LambdaQueryWrapper<CalShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalShift::getDayId, dayId)
                .orderByAsc(CalShift::getShiftCode);

        List<CalShift> shifts = calShiftMapper.selectList(wrapper);

        return shifts.stream()
                .map(this::convertCalShiftToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CalShiftDTO> listShiftsByResource(Integer resourceType, Long resourceId,
                                                  LocalDate startDate, LocalDate endDate) {
        // 查询日历日
        List<CalDay> calDays = calDayMapper.selectList(new LambdaQueryWrapper<CalDay>()
                .eq(CalDay::getResourceType, resourceType)
                .eq(CalDay::getResourceId, resourceId)
                .between(CalDay::getBizDate, startDate, endDate));

        if (calDays.isEmpty()) {
            return List.of();
        }

        List<Long> dayIds = calDays.stream().map(CalDay::getId).collect(Collectors.toList());

        // 查询班次
        List<CalShift> shifts = calShiftMapper.selectList(new LambdaQueryWrapper<CalShift>()
                .in(CalShift::getDayId, dayIds)
                .orderByAsc(CalShift::getDayId)
                .orderByAsc(CalShift::getShiftCode));

        return shifts.stream()
                .map(this::convertCalShiftToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShiftCapacity(Long shiftId, Integer capacityQty) {
        CalShift calShift = calShiftMapper.selectById(shiftId);
        if (calShift == null) {
            throw new BusinessException("班次不存在");
        }

        calShift.setCapacityQty(capacityQty);
        calShiftMapper.updateById(calShift);

        log.info("更新班次产能成功: shiftId={}, capacityQty={}", shiftId, capacityQty);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShift(Long id) {
        CalShift calShift = calShiftMapper.selectById(id);
        if (calShift == null) {
            throw new BusinessException("班次不存在");
        }

        calShiftMapper.deleteById(id);

        log.info("删除班次成功: id={}", id);
    }

    // ==================== 维护窗口 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMaintenanceWindow(MaintenanceWindowCreateRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        CalMaintenanceWindow window = new CalMaintenanceWindow();
        BeanUtils.copyProperties(request, window);

        maintenanceWindowMapper.insert(window);

        log.info("创建维护窗口成功: id={}, resourceType={}, resourceId={}",
                window.getId(), window.getResourceType(), window.getResourceId());

        return window.getId();
    }

    @Override
    public List<MaintenanceWindowDTO> listMaintenanceWindows(Integer resourceType, Long resourceId,
                                                             LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<CalMaintenanceWindow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalMaintenanceWindow::getResourceType, resourceType)
                .eq(CalMaintenanceWindow::getResourceId, resourceId)
                .ge(CalMaintenanceWindow::getStartTime, startDate.atStartOfDay())
                .le(CalMaintenanceWindow::getEndTime, endDate.atTime(23, 59, 59))
                .orderByAsc(CalMaintenanceWindow::getStartTime);

        List<CalMaintenanceWindow> windows = maintenanceWindowMapper.selectList(wrapper);

        return windows.stream()
                .map(this::convertMaintenanceWindowToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaintenanceWindow(Long id) {
        CalMaintenanceWindow window = maintenanceWindowMapper.selectById(id);
        if (window == null) {
            throw new BusinessException("维护窗口不存在");
        }

        maintenanceWindowMapper.deleteById(id);

        log.info("删除维护窗口成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaintenanceWindowStatus(Long id, Integer status) {
        CalMaintenanceWindow window = maintenanceWindowMapper.selectById(id);
        if (window == null) {
            throw new BusinessException("维护窗口不存在");
        }

        window.setStatus(status);
        maintenanceWindowMapper.updateById(window);

        log.info("更新维护窗口状态成功: id={}, status={}", id, status);
    }

    // ==================== 节假日 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddHolidays(List<HolidayCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        // 查询已存在的节假日
        List<LocalDate> dates = requests.stream()
                .map(HolidayCreateRequest::getBizDate)
                .collect(Collectors.toList());

        List<LocalDate> existingDates = holidayMapper.selectList(
                        new LambdaQueryWrapper<CalHoliday>().in(CalHoliday::getBizDate, dates))
                .stream()
                .map(CalHoliday::getBizDate)
                .collect(Collectors.toList());

        // 过滤出需要新增的节假日
        List<HolidayCreateRequest> newHolidays = requests.stream()
                .filter(req -> !existingDates.contains(req.getBizDate()))
                .collect(Collectors.toList());

        // 批量插入节假日
        for (HolidayCreateRequest request : newHolidays) {
            CalHoliday holiday = new CalHoliday();
            holiday.setBizDate(request.getBizDate());
            holiday.setName(request.getName());

            holidayMapper.insert(holiday);
        }

        log.info("批量添加节假日成功: count={}", newHolidays.size());
    }

    @Override
    public List<HolidayDTO> listHolidays(Integer year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        LambdaQueryWrapper<CalHoliday> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(CalHoliday::getBizDate, startDate, endDate)
                .orderByAsc(CalHoliday::getBizDate);

        List<CalHoliday> holidays = holidayMapper.selectList(wrapper);

        return holidays.stream()
                .map(this::convertHolidayToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHoliday(Long id) {
        CalHoliday holiday = holidayMapper.selectById(id);
        if (holiday == null) {
            throw new BusinessException("节假日不存在");
        }

        holidayMapper.deleteById(id);

        log.info("删除节假日成功: id={}", id);
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        Long count = holidayMapper.selectCount(
                new LambdaQueryWrapper<CalHoliday>().eq(CalHoliday::getBizDate, date)
        );

        return count > 0;
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为CalDayDTO
     */
    private CalDayDTO convertCalDayToDTO(CalDay calDay) {
        CalDayDTO dto = new CalDayDTO();
        dto.setId(calDay.getId());
        dto.setResourceType(calDay.getResourceType());
        dto.setResourceId(calDay.getResourceId());
        dto.setBizDate(calDay.getBizDate());
        dto.setIsWorkday(calDay.getIsWorkday());
        dto.setRemark(calDay.getRemark());

        return dto;
    }

    /**
     * 转换为CalShiftDTO
     */
    private CalShiftDTO convertCalShiftToDTO(CalShift calShift) {
        CalShiftDTO dto = new CalShiftDTO();
        dto.setId(calShift.getId());
        dto.setDayId(calShift.getDayId());
        dto.setShiftCode(calShift.getShiftCode());
        dto.setStartTime(calShift.getStartTime());
        dto.setEndTime(calShift.getEndTime());
        dto.setCapacityQty(calShift.getCapacityQty());
        dto.setStatus(calShift.getStatus());
        dto.setRemark(calShift.getRemark());

        return dto;
    }

    /**
     * 转换为MaintenanceWindowDTO
     */
    private MaintenanceWindowDTO convertMaintenanceWindowToDTO(CalMaintenanceWindow window) {
        MaintenanceWindowDTO dto = new MaintenanceWindowDTO();
        dto.setId(window.getId());
        dto.setResourceType(window.getResourceType());
        dto.setResourceId(window.getResourceId());
        dto.setStartTime(window.getStartTime());
        dto.setEndTime(window.getEndTime());
        dto.setReason(window.getReason());
        dto.setStatus(window.getStatus());

        return dto;
    }

    /**
     * 转换为HolidayDTO
     */
    private HolidayDTO convertHolidayToDTO(CalHoliday holiday) {
        HolidayDTO dto = new HolidayDTO();
        dto.setId(holiday.getId());
        dto.setBizDate(holiday.getBizDate());
        dto.setName(holiday.getName());

        return dto;
    }
}
