package com.aps.masterdata;



import com.aps.dto.request.masterdata.RouteCreateRequest;
import com.aps.dto.request.masterdata.RouteQueryRequest;
import com.aps.dto.request.masterdata.RouteUpdateRequest;
import com.aps.dto.response.masterdata.RouteDTO;
import com.aps.dto.response.masterdata.RouteDetailDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 工艺路线服务接口
 */
public interface RouteService {

    /**
     * 分页查询工艺路线列表
     */
    PageResult<RouteDTO> listRoutes(RouteQueryRequest request);

    /**
     * 根据ID获取工艺路线详情（包含工序）
     */
    RouteDetailDTO getRouteDetailById(Long id);

    /**
     * 根据ID获取工艺路线基本信息
     */
    RouteDTO getRouteById(Long id);

    /**
     * 创建工艺路线
     */
    Long createRoute(RouteCreateRequest request);

    /**
     * 更新工艺路线
     */
    void updateRoute(RouteUpdateRequest request);

    /**
     * 删除工艺路线（逻辑删除）
     */
    void deleteRoute(Long id);

    /**
     * 批量删除工艺路线
     */
    void batchDeleteRoutes(List<Long> ids);

    /**
     * 根据车型ID查询工艺路线列表
     */
    PageResult<RouteDTO> listRoutesByModelId(Long modelId, Integer pageNum, Integer pageSize);

    /**
     * 复制工艺路线（创建新版本）
     */
    Long copyRoute(Long routeId, String newVersion);

    /**
     * 启用/禁用工艺路线
     */
    void updateStatus(Long id, Integer status);
}
