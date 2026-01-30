package com.aps.mapper.order;


import com.aps.entity.order.SalesOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 销售订单 Mapper
 */
@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {

    /**
     * 根据销售订单号查询（包含车型信息）
     */
    SalesOrder selectByNoWithModel(@Param("salesNo") String salesNo);

    /**
     * 检查销售订单号是否存在
     */
    boolean existsBySalesNo(@Param("salesNo") String salesNo);
}

