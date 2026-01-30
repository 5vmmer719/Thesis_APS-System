package com.aps.mapper.order;


import com.aps.entity.order.ProductionOrderAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 生产订单属性 Mapper
 */
@Mapper
public interface ProductionOrderAttrMapper extends BaseMapper<ProductionOrderAttr> {

    /**
     * 根据订单ID查询所有属性
     */
    List<ProductionOrderAttr> selectByOrdId(@Param("ordId") Long ordId);

    /**
     * 根据订单ID删除所有属性（逻辑删除）
     */
    int deleteByOrdId(@Param("ordId") Long ordId);

    /**
     * 批量插入属性
     */
    int batchInsert(@Param("attrs") List<ProductionOrderAttr> attrs);
}

