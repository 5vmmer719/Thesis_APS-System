package com.aps.controller.masterdata;


import com.aps.dto.request.masterdata.HolidayCreateRequest;
import com.aps.dto.request.masterdata.MaintenanceWindowCreateRequest;
import com.aps.dto.request.masterdata.ShiftBatchSetRequest;
import com.aps.dto.request.masterdata.WorkdayBatchSetRequest;
import com.aps.dto.response.masterdata.CalDayDTO;
import com.aps.dto.response.masterdata.CalShiftDTO;
import com.aps.dto.response.masterdata.HolidayDTO;
import com.aps.dto.response.masterdata.MaintenanceWindowDTO;
import com.aps.masterdata.CalendarService;
import com.aps.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 日历管理控制器
 */
@Slf4j
@Tag(name = "日历管理", description = "资源日历、班次、维护窗口、节假日管理")
@RestController
@RequestMapping("/api/masterdata/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    // ==================== 资源日历（日）====================

    @Operation(summary = "批量设置资源工作日")
    @PostMapping("/workdays/batch")
    public ApiResponse<Void> batchSetWorkdays(@Validated @RequestBody WorkdayBatchSetRequest request) {
        log.info("批量设置资源工作日: resourceType={}, resourceId={}, startDate={}, endDate={}",
                request.getResourceType(), request.getResourceId(),
                request.getStartDate(), request.getEndDate());
        calendarService.batchSetWorkdays(
                request.getResourceType(),
                request.getResourceId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getIsWorkday(),
                request.getExcludeDates()
        );
        return ApiResponse.success();
    }

    @Operation(summary = "查询资源日历（日）")
    @GetMapping("/days")
    public ApiResponse<List<CalDayDTO>> listCalDays(
            @Parameter(description = "资源类型：1-产线，2-工位，3-设备，4-模具，5-人力组") @RequestParam Integer resourceType,
            @Parameter(description = "资源ID") @RequestParam Long resourceId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("查询资源日历: resourceType={}, resourceId={}, startDate={}, endDate={}",
                resourceType, resourceId, startDate, endDate);
        List<CalDayDTO> calDays = calendarService.listCalDays(resourceType, resourceId, startDate, endDate);
        return ApiResponse.success(calDays);
    }

    @Operation(summary = "删除资源日历（日）")
    @DeleteMapping("/days/{id}")
    public ApiResponse<Void> deleteCalDay(
            @Parameter(description = "日历ID") @PathVariable Long id) {
        log.info("删除资源日历: id={}", id);
        calendarService.deleteCalDay(id);
        return ApiResponse.success();
    }

    // ==================== 资源日历（班次）====================

    @Operation(summary = "批量设置班次")
    @PostMapping("/shifts/batch")
    public ApiResponse<Void> batchSetShifts(@Validated @RequestBody ShiftBatchSetRequest request) {
        log.info("批量设置班次: dayId={}, shifts.size={}", request.getDayId(), request.getShifts().size());
        calendarService.batchSetShifts(request.getDayId(), request.getShifts());
        return ApiResponse.success();
    }

    @Operation(summary = "查询班次列表（按日历日ID）")
    @GetMapping("/shifts/day/{dayId}")
    public ApiResponse<List<CalShiftDTO>> listShiftsByDayId(
            @Parameter(description = "日历日ID") @PathVariable Long dayId) {
        log.info("查询班次列表: dayId={}", dayId);
        List<CalShiftDTO> shifts = calendarService.listShiftsByDayId(dayId);
        return ApiResponse.success(shifts);
    }

    @Operation(summary = "查询资源的班次列表（按日期范围）")
    @GetMapping("/shifts")
    public ApiResponse<List<CalShiftDTO>> listShiftsByResource(
            @Parameter(description = "资源类型：1-产线，2-工位，3-设备，4-模具，5-人力组") @RequestParam Integer resourceType,
            @Parameter(description = "资源ID") @RequestParam Long resourceId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("查询资源班次列表: resourceType={}, resourceId={}, startDate={}, endDate={}",
                resourceType, resourceId, startDate, endDate);
        List<CalShiftDTO> shifts = calendarService.listShiftsByResource(resourceType, resourceId, startDate, endDate);
        return ApiResponse.success(shifts);
    }

    @Operation(summary = "更新班次产能")
    @PatchMapping("/shifts/{shiftId}/capacity")
    public ApiResponse<Void> updateShiftCapacity(
            @Parameter(description = "班次ID") @PathVariable Long shiftId,
            @Parameter(description = "班次产能（辆/班）") @RequestParam Integer capacityQty) {
        log.info("更新班次产能: shiftId={}, capacityQty={}", shiftId, capacityQty);
        calendarService.updateShiftCapacity(shiftId, capacityQty);
        return ApiResponse.success();
    }

    @Operation(summary = "删除班次")
    @DeleteMapping("/shifts/{id}")
    public ApiResponse<Void> deleteShift(
            @Parameter(description = "班次ID") @PathVariable Long id) {
        log.info("删除班次: id={}", id);
        calendarService.deleteShift(id);
        return ApiResponse.success();
    }

    // ==================== 维护窗口 ====================

    @Operation(summary = "创建维护窗口")
    @PostMapping("/maintenance-windows")
    public ApiResponse<Long> createMaintenanceWindow(@Validated @RequestBody MaintenanceWindowCreateRequest request) {
        log.info("创建维护窗口: resourceType={}, resourceId={}, startTime={}, endTime={}",
                request.getResourceType(), request.getResourceId(),
                request.getStartTime(), request.getEndTime());
        Long windowId = calendarService.createMaintenanceWindow(request);
        return ApiResponse.success(windowId);
    }

    @Operation(summary = "查询维护窗口列表")
    @GetMapping("/maintenance-windows")
    public ApiResponse<List<MaintenanceWindowDTO>> listMaintenanceWindows(
            @Parameter(description = "资源类型：1-产线，2-工位，3-设备，4-模具，5-人力组") @RequestParam Integer resourceType,
            @Parameter(description = "资源ID") @RequestParam Long resourceId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("查询维护窗口列表: resourceType={}, resourceId={}, startDate={}, endDate={}",
                resourceType, resourceId, startDate, endDate);
        List<MaintenanceWindowDTO> windows = calendarService.listMaintenanceWindows(resourceType, resourceId, startDate, endDate);
        return ApiResponse.success(windows);
    }

    @Operation(summary = "删除维护窗口")
    @DeleteMapping("/maintenance-windows/{id}")
    public ApiResponse<Void> deleteMaintenanceWindow(
            @Parameter(description = "维护窗口ID") @PathVariable Long id) {
        log.info("删除维护窗口: id={}", id);
        calendarService.deleteMaintenanceWindow(id);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用维护窗口")
    @PatchMapping("/maintenance-windows/{id}/status")
    public ApiResponse<Void> updateMaintenanceWindowStatus(
            @Parameter(description = "维护窗口ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("更新维护窗口状态: id={}, status={}", id, status);
        calendarService.updateMaintenanceWindowStatus(id, status);
        return ApiResponse.success();
    }

    // ==================== 节假日 ====================

    @Operation(summary = "批量添加节假日")
    @PostMapping("/holidays/batch")
    public ApiResponse<Void> batchAddHolidays(@Validated @RequestBody List<HolidayCreateRequest> requests) {
        log.info("批量添加节假日: count={}", requests.size());
        calendarService.batchAddHolidays(requests);
        return ApiResponse.success();
    }

    @Operation(summary = "查询节假日列表")
    @GetMapping("/holidays")
    public ApiResponse<List<HolidayDTO>> listHolidays(
            @Parameter(description = "年份") @RequestParam Integer year) {
        log.info("查询节假日列表: year={}", year);
        List<HolidayDTO> holidays = calendarService.listHolidays(year);
        return ApiResponse.success(holidays);
    }

    @Operation(summary = "删除节假日")
    @DeleteMapping("/holidays/{id}")
    public ApiResponse<Void> deleteHoliday(
            @Parameter(description = "节假日ID") @PathVariable Long id) {
        log.info("删除节假日: id={}", id);
        calendarService.deleteHoliday(id);
        return ApiResponse.success();
    }

    @Operation(summary = "判断某日期是否为节假日")
    @GetMapping("/holidays/check")
    public ApiResponse<Boolean> isHoliday(
            @Parameter(description = "日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("判断是否为节假日: date={}", date);
        boolean isHoliday = calendarService.isHoliday(date);
        return ApiResponse.success(isHoliday);
    }
}
