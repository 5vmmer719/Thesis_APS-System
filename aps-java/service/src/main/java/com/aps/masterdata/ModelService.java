package com.aps.masterdata;


import com.aps.dto.request.masterdata.ModelCreateRequest;
import com.aps.dto.request.masterdata.ModelQueryRequest;
import com.aps.dto.request.masterdata.ModelUpdateRequest;
import com.aps.dto.response.masterdata.ModelDTO;
import com.aps.response.PageResult;

public interface ModelService {

    /**
     * 分页查询车型列表
     */
    PageResult<ModelDTO> listModels(ModelQueryRequest request);

    /**
     * 根据ID获取车型详情
     */
    ModelDTO getModelById(Long id);

    /**
     * 创建车型
     */
    Long createModel(ModelCreateRequest request);

    /**
     * 更新车型
     */
    void updateModel(ModelUpdateRequest request);

    /**
     * 删除车型（逻辑删除）
     */
    void deleteModel(Long id);
}
