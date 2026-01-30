// da/src/main/java/com/aps/mapper/execution/StatusHistoryMapper.java
package com.aps.mapper.execution;

import com.aps.entity.execution.StatusHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 状态历史Mapper
 */
@Mapper
public interface StatusHistoryMapper extends BaseMapper<StatusHistory> {
}
