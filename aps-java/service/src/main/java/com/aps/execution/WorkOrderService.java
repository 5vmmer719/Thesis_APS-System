// service/src/main/java/com/aps/execution/WorkOrderService.java
package com.aps.execution;

import com.aps.dto.request.execution.WorkOrderActionRequest;
import com.aps.dto.request.execution.WorkOrderQueryRequest;
import com.aps.dto.request.execution.WorkOrderReportRequest;
import com.aps.dto.response.execution.WorkOrderDTO;
import com.aps.response.PageResult;

/**
 * 工单服务接口
 */
public interface WorkOrderService {

    /**
     * 分页查询工单列表
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<WorkOrderDTO> queryWorkOrders(WorkOrderQueryRequest request);

    /**
     * 获取工单详情
     * @param id 工单ID
     * @return 工单详情
     */
    WorkOrderDTO getWorkOrderDetail(Long id);

    /**
     * 下达工单（状态：待下达 -> 已下达）
     * @param id 工单ID
     * @param request 操作请求
     */
    void releaseWorkOrder(Long id, WorkOrderActionRequest request);

    /**
     * 开始工单（状态：已下达 -> 执行中）
     * @param id 工单ID
     * @param request 操作请求
     */
    void startWorkOrder(Long id, WorkOrderActionRequest request);

    /**
     * 暂停工单（状态：执行中 -> 暂停）
     * @param id 工单ID
     * @param request 操作请求
     */
    void pauseWorkOrder(Long id, WorkOrderActionRequest request);

    /**
     * 恢复工单（状态：暂停 -> 执行中）
     * @param id 工单ID
     * @param request 操作请求
     */
    void resumeWorkOrder(Long id, WorkOrderActionRequest request);

    /**
     * 完成工单（状态：执行中 -> 完成）
     * @param id 工单ID
     * @param request 操作请求
     */
    void completeWorkOrder(Long id, WorkOrderActionRequest request);

    /**
     * 作废工单（状态：任意 -> 作废）
     * @param id 工单ID
     * @param reason 作废原因
     */
    void voidWorkOrder(Long id, String reason);

    /**
     * 工单报工
     * @param id 工单ID
     * @param request 报工请求
     */
    void reportWorkOrder(Long id, WorkOrderReportRequest request);
}
