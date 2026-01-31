package com.aps.plan;

import com.aps.dto.request.plan.MrpQueryRequest;
import com.aps.dto.request.plan.MrpRunRequest;
import com.aps.dto.response.plan.MrpDTO;
import com.aps.dto.response.plan.MrpItemDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * MRP服务接口
 *
 * @author APS System
 * @since 2024-01-30
 */
public interface MrpService {

    /**
     * 运行MRP
     *
     * @param request 运行请求
     * @return MRP ID
     */
    Long run(MrpRunRequest request);

    /**
     * 删除MRP
     *
     * @param id MRP ID
     */
    void delete(Long id);

    /**
     * 查询MRP详情
     *
     * @param id MRP ID
     * @return MRP详情
     */
    MrpDTO getById(Long id);

    /**
     * 分页查询MRP列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<MrpDTO> page(MrpQueryRequest request);

    /**
     * 查询MRP明细列表
     *
     * @param id MRP ID
     * @return 明细列表
     */
    List<MrpItemDTO> listItems(Long id);
}

