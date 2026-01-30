package com.aps.masterdata;



import com.aps.dto.request.masterdata.LaborGroupCreateRequest;
import com.aps.dto.request.masterdata.LaborGroupQueryRequest;
import com.aps.dto.response.masterdata.LaborGroupDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 人力组服务接口
 */
public interface LaborGroupService {

    /**
     * 分页查询人力组列表
     */
    PageResult<LaborGroupDTO> listLaborGroups(LaborGroupQueryRequest request);

    /**
     * 根据ID获取人力组详情（包含关联工位）
     */
    LaborGroupDTO getLaborGroupById(Long id);

    /**
     * 创建人力组
     */
    Long createLaborGroup(LaborGroupCreateRequest request);

    /**
     * 更新人力组
     */
    void updateLaborGroup(Long id, LaborGroupCreateRequest request);

    /**
     * 删除人力组（逻辑删除）
     */
    void deleteLaborGroup(Long id);

    /**
     * 批量删除人力组
     */
    void batchDeleteLaborGroups(List<Long> ids);

    /**
     * 启用/禁用人力组
     */
    void updateStatus(Long id, Integer status);

    /**
     * 查询所有启用的人力组（不分页，用于下拉选择）
     */
    List<LaborGroupDTO> listAllActiveLaborGroups();

    /**
     * 为人力组添加工位
     */
    void addStationsToLaborGroup(Long laborId, List<Long> stationIds);

    /**
     * 从人力组移除工位
     */
    void removeStationsFromLaborGroup(Long laborId, List<Long> stationIds);
}
