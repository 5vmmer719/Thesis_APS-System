// model/src/main/java/com/aps/entity/execution/ExeException.java
package com.aps.entity.execution;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 执行异常实体
 * 对应表: exe_exception
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exe_exception")
@Schema(description = "执行异常")
public class ExeException extends BaseEntity {

    @TableId
    @Schema(description = "异常ID")
    private Long id;

    @Schema(description = "工单ID")
    private Long woId;

    @Schema(description = "异常类型")
    private String type;

    @Schema(description = "异常等级")
    private Integer level;

    @Schema(description = "状态：0-新建，1-处理中，2-已关闭")
    private Integer status;

    @TableField("`desc`")  // desc 是 MySQL 关键字，需要加反引号
    @Schema(description = "异常描述")
    private String desc;

    @TableField("payload")  // 明确指定数据库字段名
    @Schema(description = "负载JSON")
    private String payload;  // ✅ 确保有这个字段，Lombok会自动生成 getPayload() 和 setPayload()
}
