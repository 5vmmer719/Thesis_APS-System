package com.aps.masterdata;



import com.aps.dto.request.masterdata.ToolingCreateRequest;
import com.aps.dto.request.masterdata.ToolingQueryRequest;
import com.aps.dto.response.masterdata.ToolingDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 工装服务接口
 */
public interface ToolingService {

    /**
     * 分页查询工装列表
     */
    PageResult<ToolingDTO> listToolings(ToolingQueryRequest request);

    /**
     * 根据ID获取工装详情
     */
    ToolingDTO getToolingById(Long id);

    /**
     * 创建工装
     */
    Long createTooling(ToolingCreateRequest request);

    /**
     * 更新工装
     */
    void updateTooling(Long id, ToolingCreateRequest request);

    /**
     * 删除工装（逻辑删除）
     */
    void deleteTooling(Long id);

    /**
     * 批量删除工装
     */
    void batchDeleteToolings(List<Long> ids);

    /**
     * 启用/禁用工装
     */
    void updateStatus(Long id, Integer status);

    /**
     * 根据工装类型查询工装列表
     */
    List<ToolingDTO> listToolingsByType(Integer toolingType);

    /**
     * 查询所有启用的工装（不分页，用于下拉选择）
     */
    List<ToolingDTO> listAllActiveToolings();

    /**
     * 绑定工装到资源（工位或设备）
     */
    void bindToolingToResource(Long toolingId, Integer resourceType, Long resourceId);

    /**
     * 解绑工装与资源
     */
    void unbindToolingFromResource(Long toolingId, Integer resourceType, Long resourceId);

    /**
     * 查询资源绑定的工装列表
     */
    List<ToolingDTO> listToolingsByResource(Integer resourceType, Long resourceId);
}
