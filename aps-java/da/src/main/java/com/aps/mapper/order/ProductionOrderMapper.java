package com.aps.mapper.order;


import com.aps.entity.order.ProductionOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 生产订单 Mapper
 */
@Mapper
public interface ProductionOrderMapper extends BaseMapper<ProductionOrder> {

    /**
     * 根据生产订单号查询（包含关联信息）
     */
    ProductionOrder selectByNoWithRelations(@Param("prodNo") String prodNo);

    /**
     * 检查生产订单号是否存在
     */
    boolean existsByProdNo(@Param("prodNo") String prodNo);

    /**
     * 根据ID查询（包含属性）
     */
    ProductionOrder selectByIdWithAttrs(@Param("id") Long id);
}

