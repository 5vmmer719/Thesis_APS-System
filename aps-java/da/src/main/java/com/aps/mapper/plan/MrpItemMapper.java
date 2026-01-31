package com.aps.mapper.plan;

import com.aps.entity.plan.MrpItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MRP明细 Mapper
 *
 * @author APS System
 * @since 2024-01-30
 */
@Mapper
public interface MrpItemMapper extends BaseMapper<MrpItem> {

    /**
     * 查询MRP明细列表（关联物料信息）
     *
     * @param mrpId MRP主表ID
     * @return 明细列表
     */
    List<MrpItem> selectListWithItem(@Param("mrpId") Long mrpId);
}

