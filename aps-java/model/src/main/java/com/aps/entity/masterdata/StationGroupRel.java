package com.aps.entity.masterdata;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工位组关系实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_station_group_rel")
@Schema(description = "工位组关系")
public class StationGroupRel extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "工位组ID")
    private Long groupId;

    @Schema(description = "工位ID")
    private Long stationId;
}
