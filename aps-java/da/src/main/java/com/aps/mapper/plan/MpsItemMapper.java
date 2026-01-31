package com.aps.mapper.plan;

import com.aps.entity.plan.MpsItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MPS明细 Mapper
 *
 * @author APS System
 * @since 2024-01-30
 */
@Mapper
public interface MpsItemMapper extends BaseMapper<MpsItem> {

    /**
     * 查询MPS明细列表（关联车型信息）
     *
     * @param mpsId MPS主表ID
     * @return 明细列表
     */
    List<MpsItem> selectListWithModel(@Param("mpsId") Long mpsId);
}

