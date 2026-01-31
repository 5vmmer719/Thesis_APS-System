package com.aps.masterdata;

import com.aps.dto.request.masterdata.SetupMatrixBatchImportRequest;
import com.aps.dto.request.masterdata.SetupMatrixCreateRequest;
import com.aps.dto.request.masterdata.SetupMatrixQueryRequest;
import com.aps.dto.request.masterdata.SetupMatrixUpdateRequest;
import com.aps.dto.response.masterdata.SetupMatrixDTO;
import com.aps.response.PageResult;

import java.util.List;

/**
 * 换型矩阵Service接口
 *
 * @author APS System
 * @since 2024-01-30
 */
public interface SetupMatrixService {

    /**
     * 分页查询换型矩阵列表
     */
    PageResult<SetupMatrixDTO> listSetupMatrices(SetupMatrixQueryRequest request);

    /**
     * 根据ID获取换型矩阵详情
     */
    SetupMatrixDTO getSetupMatrixById(Long id);

    /**
     * 创建换型矩阵
     */
    Long createSetupMatrix(SetupMatrixCreateRequest request);

    /**
     * 更新换型矩阵
     */
    void updateSetupMatrix(SetupMatrixUpdateRequest request);

    /**
     * 删除换型矩阵
     */
    void deleteSetupMatrix(Long id);

    /**
     * 批量导入换型矩阵
     */
    void batchImportSetupMatrix(SetupMatrixBatchImportRequest request);

    /**
     * 查询指定工艺的换型时间（供排产引擎调用）
     * 
     * @param processType 工艺类型
     * @param fromKey 源换型键
     * @param toKey 目标换型键
     * @return 换型时间（分钟），未找到返回0
     */
    Integer getSetupMinutes(Integer processType, String fromKey, String toKey);

    /**
     * 获取指定工艺的所有启用的换型矩阵（供排产引擎调用）
     */
    List<SetupMatrixDTO> getActiveSetupMatricesByProcess(Integer processType);
}

