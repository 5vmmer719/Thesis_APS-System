// da/src/main/java/com/aps/mapper/execution/ExeExceptionMapper.java
package com.aps.mapper.execution;

import com.aps.entity.execution.ExeException;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异常Mapper
 */
@Mapper
public interface ExeExceptionMapper extends BaseMapper<ExeException> {
}
