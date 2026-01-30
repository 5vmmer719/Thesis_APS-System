package com.aps.mapper.schedule;


import com.aps.entity.schedule.SchJob;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排产任务 Mapper接口
 *
 * @author APS System
 * @since 2024-01-01
 */
@Mapper
public interface SchJobMapper extends BaseMapper<SchJob> {
}

