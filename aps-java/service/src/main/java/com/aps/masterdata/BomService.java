package com.aps.masterdata;


import com.aps.dto.request.masterdata.BomCreateRequest;
import com.aps.dto.request.masterdata.BomQueryRequest;
import com.aps.dto.request.masterdata.BomUpdateRequest;
import com.aps.dto.response.masterdata.BomDTO;
import com.aps.dto.response.masterdata.BomDetailDTO;
import com.aps.response.PageResult;

/**
 * BOM服务接口
 */
public interface BomService {

    /**
     * 分页查询BOM列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<BomDTO> listBoms(BomQueryRequest request);

    /**
     * 根据ID获取BOM详情（包含明细）
     *
     * @param id BOM ID
     * @return BOM详情
     */
    BomDetailDTO getBomDetailById(Long id);

    /**
     * 根据ID获取BOM基本信息
     *
     * @param id BOM ID
     * @return BOM信息
     */
    BomDTO getBomById(Long id);

    /**
     * 创建BOM
     *
     * @param request 创建请求
     * @return BOM ID
     */
    Long createBom(BomCreateRequest request);

    /**
     * 更新BOM
     *
     * @param request 更新请求
     */
    void updateBom(BomUpdateRequest request);

    /**
     * 删除BOM（逻辑删除）
     *
     * @param id BOM ID
     */
    void deleteBom(Long id);

    /**
     * 根据车型ID查询BOM列表
     *
     * @param modelId 车型ID
     * @return BOM列表
     */
    PageResult<BomDTO> listBomsByModelId(Long modelId, Integer pageNum, Integer pageSize);

    /**
     * 复制BOM（创建新版本）
     *
     * @param bomId 源BOM ID
     * @param newVersion 新版本号
     * @return 新BOM ID
     */
    Long copyBom(Long bomId, String newVersion);
}

