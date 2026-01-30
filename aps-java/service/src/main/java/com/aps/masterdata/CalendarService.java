package com.aps.masterdata;



import com.aps.dto.request.masterdata.HolidayCreateRequest;
import com.aps.dto.request.masterdata.MaintenanceWindowCreateRequest;
import com.aps.dto.response.masterdata.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 日历服务接口
 */
public interface CalendarService {

    // ==================== 资源日历（日）====================

    /**
     * 批量设置资源工作日
     * @param resourceType 资源类型：1-产线，2-工位，3-设备，4-模具，5-人力组
     * @param resourceId 资源ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isWorkday 是否工作日
     * @param excludeDates 排除日期列表（如节假日）
     */
    void batchSetWorkdays(Integer resourceType, Long resourceId,
                          LocalDate startDate, LocalDate endDate,
                          Integer isWorkday, List<LocalDate> excludeDates);

    /**
     * 查询资源日历（日）
     */
    List<CalDayDTO> listCalDays(Integer resourceType, Long resourceId,
                                LocalDate startDate, LocalDate endDate);

    /**
     * 删除资源日历（日）
     */
    void deleteCalDay(Long id);

    // ==================== 资源日历（班次）====================

    /**
     * 批量设置班次
     * @param dayId 日历日ID
     * @param shifts 班次配置列表
     */
    void batchSetShifts(Long dayId, List<ShiftConfigDTO> shifts);

    /**
     * 查询班次列表
     */
    List<CalShiftDTO> listShiftsByDayId(Long dayId);

    /**
     * 查询资源的班次列表（按日期范围）
     */
    List<CalShiftDTO> listShiftsByResource(Integer resourceType, Long resourceId,
                                           LocalDate startDate, LocalDate endDate);

    /**
     * 更新班次产能
     */
    void updateShiftCapacity(Long shiftId, Integer capacityQty);

    /**
     * 删除班次
     */
    void deleteShift(Long id);

    // ==================== 维护窗口 ====================

    /**
     * 创建维护窗口
     */
    Long createMaintenanceWindow(MaintenanceWindowCreateRequest request);

    /**
     * 查询维护窗口列表
     */
    List<MaintenanceWindowDTO> listMaintenanceWindows(Integer resourceType, Long resourceId,
                                                      LocalDate startDate, LocalDate endDate);

    /**
     * 删除维护窗口
     */
    void deleteMaintenanceWindow(Long id);

    /**
     * 启用/禁用维护窗口
     */
    void updateMaintenanceWindowStatus(Long id, Integer status);

    // ==================== 节假日 ====================

    /**
     * 批量添加节假日
     */
    void batchAddHolidays(List<HolidayCreateRequest> requests);

    /**
     * 查询节假日列表
     */
    List<HolidayDTO> listHolidays(Integer year);

    /**
     * 删除节假日
     */
    void deleteHoliday(Long id);

    /**
     * 判断某日期是否为节假日
     */
    boolean isHoliday(LocalDate date);
}
