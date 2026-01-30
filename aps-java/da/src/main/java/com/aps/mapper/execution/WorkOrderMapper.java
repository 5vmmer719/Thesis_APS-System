// da/src/main/java/com/aps/mapper/execution/WorkOrderMapper.java
package com.aps.mapper.execution;

import com.aps.entity.execution.WorkOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单Mapper
 */
@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {
}
