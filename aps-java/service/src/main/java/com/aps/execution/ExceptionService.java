// service/src/main/java/com/aps/execution/ExceptionService.java
package com.aps.execution;

import com.aps.dto.request.execution.ExceptionCreateRequest;
import com.aps.dto.request.execution.ExceptionQueryRequest;
import com.aps.dto.response.execution.ExceptionDTO;
import com.aps.response.PageResult;

/**
 * 异常服务接口
 */
public interface ExceptionService {

    /**
     * 创建异常
     * @param request 创建请求
     * @return 异常ID
     */
    Long createException(ExceptionCreateRequest request);

    /**
     * 分页查询异常列表
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<ExceptionDTO> queryExceptions(ExceptionQueryRequest request);

    /**
     * 获取异常详情
     * @param id 异常ID
     * @return 异常详情
     */
    ExceptionDTO getExceptionDetail(Long id);

    /**
     * 接单处理异常（状态：新建 -> 处理中）
     * @param id 异常ID
     */
    void acceptException(Long id);

    /**
     * 关闭异常（状态：处理中 -> 已关闭）
     * @param id 异常ID
     * @param comment 关闭备注
     */
    void closeException(Long id, String comment);
}
