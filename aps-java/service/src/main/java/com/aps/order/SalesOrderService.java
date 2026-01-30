
package com.aps.order;


import com.aps.dto.request.order.SalesOrderCreateRequest;
import com.aps.dto.request.order.SalesOrderQueryRequest;
import com.aps.dto.request.order.ToProdRequest;
import com.aps.dto.response.order.SalesOrderDTO;
import com.aps.response.PageResult;

/**
 * 销售订单服务接口
 */
public interface SalesOrderService {

    /**
     * 创建销售订单
     *
     * @param request 创建请求
     * @return 订单ID
     */
    Long create(SalesOrderCreateRequest request);

    /**
     * 分页查询销售订单
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<SalesOrderDTO> list(SalesOrderQueryRequest request);

    /**
     * 获取销售订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    SalesOrderDTO getById(Long id);

    /**
     * 审批销售订单
     *
     * @param id 订单ID
     */
    void approve(Long id);

    /**
     * 转换为生产订单
     *
     * @param id      销售订单ID
     * @param request 转换请求
     * @return 生产订单ID
     */
    Long toProdOrder(Long id, ToProdRequest request);

    /**
     * 取消销售订单
     *
     * @param id 订单ID
     */
    void cancel(Long id);
}
