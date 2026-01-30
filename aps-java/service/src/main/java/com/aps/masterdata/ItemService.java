package com.aps.masterdata;


import com.aps.dto.request.masterdata.ItemCreateRequest;
import com.aps.dto.request.masterdata.ItemQueryRequest;
import com.aps.dto.request.masterdata.ItemUpdateRequest;
import com.aps.dto.response.masterdata.ItemDTO;
import com.aps.response.PageResult;

public interface ItemService {

    /**
     * 分页查询物料列表
     */
    PageResult<ItemDTO> listItems(ItemQueryRequest request);

    /**
     * 根据ID获取物料详情
     */
    ItemDTO getItemById(Long id);

    /**
     * 创建物料
     */
    Long createItem(ItemCreateRequest request);

    /**
     * 更新物料
     */
    void updateItem(ItemUpdateRequest request);

    /**
     * 删除物料（逻辑删除）
     */
    void deleteItem(Long id);
}
