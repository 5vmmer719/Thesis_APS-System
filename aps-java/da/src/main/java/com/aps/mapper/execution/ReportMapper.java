// da/src/main/java/com/aps/mapper/execution/ReportMapper.java
package com.aps.mapper.execution;

import com.aps.entity.execution.Report;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报工记录Mapper
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
