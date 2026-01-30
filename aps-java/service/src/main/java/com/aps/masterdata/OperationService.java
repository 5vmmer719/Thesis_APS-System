package com.aps.masterdata;



import com.aps.dto.request.masterdata.OperationCreateRequest;
import com.aps.dto.request.masterdata.OperationQueryRequest;
import com.aps.dto.response.masterdata.OperationDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 工序服务接口
 */
public interface OperationService {

    /**
     * 分页查询工序列表
     */
    PageResult<OperationDTO> listOperations(OperationQueryRequest request);

    /**
     * 根据ID获取工序详情
     */
    OperationDTO getOperationById(Long id);

    /**
     * 根据路线ID查询工序列表（按序号排序）
     */
    List<OperationDTO> listOperationsByRouteId(Long routeId);

    /**
     * 创建工序
     */
    Long createOperation(Long routeId, OperationCreateRequest request);

    /**
     * 更新工序
     */
    void updateOperation(Long id, OperationCreateRequest request);

    /**
     * 删除工序（逻辑删除）
     */
    void deleteOperation(Long id);

    /**
     * 批量删除工序
     */
    void batchDeleteOperations(List<Long> ids);

    /**
     * 启用/禁用工序
     */
    void updateStatus(Long id, Integer status);
}

