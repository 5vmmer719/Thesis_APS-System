package com.aps.masterdata;



import com.aps.dto.request.masterdata.EquipmentCreateRequest;
import com.aps.dto.request.masterdata.EquipmentQueryRequest;
import com.aps.dto.response.masterdata.EquipmentDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 设备服务接口
 */
public interface EquipmentService {

    /**
     * 分页查询设备列表
     */
    PageResult<EquipmentDTO> listEquipments(EquipmentQueryRequest request);

    /**
     * 根据ID获取设备详情
     */
    EquipmentDTO getEquipmentById(Long id);

    /**
     * 创建设备
     */
    Long createEquipment(EquipmentCreateRequest request);

    /**
     * 更新设备
     */
    void updateEquipment(Long id, EquipmentCreateRequest request);

    /**
     * 删除设备（逻辑删除）
     */
    void deleteEquipment(Long id);

    /**
     * 批量删除设备
     */
    void batchDeleteEquipments(List<Long> ids);

    /**
     * 启用/禁用设备
     */
    void updateStatus(Long id, Integer status);

    /**
     * 根据工位ID查询设备列表
     */
    List<EquipmentDTO> listEquipmentsByStationId(Long stationId);

    /**
     * 查询所有启用的设备（不分页，用于下拉选择）
     */
    List<EquipmentDTO> listAllActiveEquipments();
}
