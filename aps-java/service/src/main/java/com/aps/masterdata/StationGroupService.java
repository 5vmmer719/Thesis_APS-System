package com.aps.masterdata;



import com.aps.dto.request.masterdata.StationGroupCreateRequest;
import com.aps.dto.request.masterdata.StationGroupQueryRequest;
import com.aps.dto.request.masterdata.StationGroupUpdateRequest;
import com.aps.dto.response.masterdata.StationGroupDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 工位组服务接口
 */
public interface StationGroupService {

    /**
     * 分页查询工位组列表
     */
    PageResult<StationGroupDTO> listStationGroups(StationGroupQueryRequest request);

    /**
     * 根据ID获取工位组详情（包含工位列表）
     */
    StationGroupDTO getStationGroupById(Long id);

    /**
     * 创建工位组
     */
    Long createStationGroup(StationGroupCreateRequest request);

    /**
     * 更新工位组
     */
    void updateStationGroup(StationGroupUpdateRequest request);

    /**
     * 删除工位组（逻辑删除）
     */
    void deleteStationGroup(Long id);

    /**
     * 批量删除工位组
     */
    void batchDeleteStationGroups(List<Long> ids);

    /**
     * 启用/禁用工位组
     */
    void updateStatus(Long id, Integer status);

    /**
     * 查询所有启用的工位组（不分页，用于下拉选择）
     */
    List<StationGroupDTO> listAllActiveStationGroups();

    /**
     * 为工位组添加工位
     */
    void addStationsToGroup(Long groupId, List<Long> stationIds);

    /**
     * 从工位组移除工位
     */
    void removeStationsFromGroup(Long groupId, List<Long> stationIds);

    /**
     * 根据工位组编码查询工位列表
     */
    List<Long> getStationIdsByGroupCode(String groupCode);
}
