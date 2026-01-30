package com.aps.masterdata;



import com.aps.dto.request.masterdata.LineCreateRequest;
import com.aps.dto.request.masterdata.LineQueryRequest;
import com.aps.dto.request.masterdata.LineUpdateRequest;
import com.aps.dto.response.masterdata.LineDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 产线服务接口
 */
public interface LineService {

    /**
     * 分页查询产线列表
     */
    PageResult<LineDTO> listLines(LineQueryRequest request);

    /**
     * 根据ID获取产线详情
     */
    LineDTO getLineById(Long id);

    /**
     * 创建产线
     */
    Long createLine(LineCreateRequest request);

    /**
     * 更新产线
     */
    void updateLine(LineUpdateRequest request);

    /**
     * 删除产线（逻辑删除）
     */
    void deleteLine(Long id);

    /**
     * 批量删除产线
     */
    void batchDeleteLines(List<Long> ids);

    /**
     * 启用/禁用产线
     */
    void updateStatus(Long id, Integer status);

    /**
     * 根据车间ID查询产线列表
     */
    List<LineDTO> listLinesByWorkshopId(Long workshopId);

    /**
     * 根据工艺类型查询产线列表
     */
    List<LineDTO> listLinesByProcessType(Integer processType);


    List<LineDTO> listAllActiveLines();
}
