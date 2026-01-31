package com.aps.plan;

import com.aps.dto.request.plan.*;
import com.aps.dto.response.plan.MpsDTO;
import com.aps.dto.response.plan.MpsItemDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * MPS服务接口
 *
 * @author APS System
 * @since 2024-01-30
 */
public interface MpsService {

    /**
     * 创建MPS
     *
     * @param request 创建请求
     * @return MPS ID
     */
    Long create(MpsCreateRequest request);

    /**
     * 更新MPS
     *
     * @param id MPS ID
     * @param request 更新请求
     */
    void update(Long id, MpsUpdateRequest request);

    /**
     * 删除MPS
     *
     * @param id MPS ID
     */
    void delete(Long id);

    /**
     * 查询MPS详情
     *
     * @param id MPS ID
     * @return MPS详情
     */
    MpsDTO getById(Long id);

    /**
     * 分页查询MPS列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<MpsDTO> page(MpsQueryRequest request);

    /**
     * 设置MPS明细
     *
     * @param id MPS ID
     * @param request 明细设置请求
     */
    void setItems(Long id, MpsItemSetRequest request);

    /**
     * 查询MPS明细列表
     *
     * @param id MPS ID
     * @return 明细列表
     */
    List<MpsItemDTO> listItems(Long id);

    /**
     * 删除MPS明细
     *
     * @param id MPS ID
     * @param itemId 明细ID
     */
    void deleteItem(Long id, Long itemId);

    /**
     * 提交审批
     *
     * @param id MPS ID
     */
    void submitApproval(Long id);

    /**
     * 批准MPS
     *
     * @param id MPS ID
     */
    void approve(Long id);

    /**
     * 关闭MPS
     *
     * @param id MPS ID
     */
    void close(Long id);
}

