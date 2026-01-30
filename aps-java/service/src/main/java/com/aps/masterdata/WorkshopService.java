package com.aps.masterdata;


import com.aps.dto.request.masterdata.WorkshopCreateRequest;
import com.aps.dto.request.masterdata.WorkshopQueryRequest;
import com.aps.dto.request.masterdata.WorkshopUpdateRequest;
import com.aps.dto.response.masterdata.WorkshopDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 车间服务接口
 */
public interface WorkshopService {

    /**
     * 分页查询车间列表
     */
    PageResult<WorkshopDTO> listWorkshops(WorkshopQueryRequest request);

    /**
     * 根据ID获取车间详情
     */
    WorkshopDTO getWorkshopById(Long id);

    /**
     * 创建车间
     */
    Long createWorkshop(WorkshopCreateRequest request);

    /**
     * 更新车间
     */
    void updateWorkshop(WorkshopUpdateRequest request);

    /**
     * 删除车间（逻辑删除）
     */
    void deleteWorkshop(Long id);

    /**
     * 批量删除车间
     */
    void batchDeleteWorkshops(List<Long> ids);

    /**
     * 启用/禁用车间
     */
    void updateStatus(Long id, Integer status);

    /**
     * 查询所有启用的车间（不分页，用于下拉选择）
     */
    List<WorkshopDTO> listAllActiveWorkshops();

    /**
     * 根据工艺类型查询车间列表
     */
    List<WorkshopDTO> listWorkshopsByProcessType(Integer processType);
}

