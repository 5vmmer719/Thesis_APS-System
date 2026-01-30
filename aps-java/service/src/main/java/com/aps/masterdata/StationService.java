package com.aps.masterdata;



import com.aps.dto.request.masterdata.StationCreateRequest;
import com.aps.dto.request.masterdata.StationQueryRequest;
import com.aps.dto.request.masterdata.StationUpdateRequest;

import com.aps.dto.response.masterdata.StationDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 工位服务接口
 */
public interface StationService {

    /**
     * 分页查询工位列表
     */
    PageResult<StationDTO> listStations(StationQueryRequest request);

    /**
     * 根据ID获取工位详情
     */
    StationDTO getStationById(Long id);

    /**
     * 创建工位
     */
    Long createStation(StationCreateRequest request);

    /**
     * 更新工位
     */
    void updateStation(StationUpdateRequest request);

    /**
     * 删除工位（逻辑删除）
     */
    void deleteStation(Long id);

    /**
     * 批量删除工位
     */
    void batchDeleteStations(List<Long> ids);

    /**
     * 启用/禁用工位
     */
    void updateStatus(Long id, Integer status);

    /**
     * 根据产线ID查询工位列表
     */
    List<StationDTO> listStationsByLineId(Long lineId);

    /**
     * 查询所有启用的工位（不分页，用于下拉选择）
     */
    List<StationDTO> listAllActiveStations();

    /**
     * 根据工位ID列表批量查询工位信息
     */
    List<StationDTO> listStationsByIds(List<Long> stationIds);
}
