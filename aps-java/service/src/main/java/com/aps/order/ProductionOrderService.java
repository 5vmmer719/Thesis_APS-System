package com.aps.order;


import com.aps.dto.request.order.ProdOrderAttrsSetRequest;
import com.aps.dto.request.order.ProdOrderCreateRequest;
import com.aps.dto.request.order.ProdOrderQueryRequest;
import com.aps.dto.request.order.ProdOrderUpdateRequest;
import com.aps.dto.response.order.ProdOrderDTO;
import com.aps.response.PageResult;

/**
 * 生产订单服务接口
 */
public interface ProductionOrderService {

    /**
     * 创建生产订单
     *
     * @param request 创建请求
     * @return 订单ID
     */
    Long create(ProdOrderCreateRequest request);

    /**
     * 分页查询生产订单
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<ProdOrderDTO> list(ProdOrderQueryRequest request);

    /**
     * 获取生产订单详情（含属性）
     *
     * @param id 订单ID
     * @return 订单详情
     */
    ProdOrderDTO getById(Long id);

    /**
     * 更新生产订单（仅草稿/待审批状态）
     *
     * @param id      订单ID
     * @param request 更新请求
     */
    void update(Long id, ProdOrderUpdateRequest request);

    /**
     * 设置订单属性
     *
     * @param id      订单ID
     * @param request 属性设置请求
     */
    void setAttrs(Long id, ProdOrderAttrsSetRequest request);

    /**
     * 提交审批
     *
     * @param id 订单ID
     */
    void submitApprove(Long id);

    /**
     * 审批通过
     *
     * @param id 订单ID
     */
    void approve(Long id);

    /**
     * 审批驳回
     *
     * @param id      订单ID
     * @param comment 驳回原因
     */
    void reject(Long id, String comment);

    /**
     * 取消订单
     *
     * @param id 订单ID
     */
    void cancel(Long id);

    /**
     * 更新订单状态（内部使用）
     *
     * @param id     订单ID
     * @param status 新状态
     */
    void updateStatus(Long id, Integer status);
}

