package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人力组信息DTO
 */
@Data
@Schema(description = "人力组信息")
public class LaborGroupDTO {

    @Schema(description = "人力组ID")
    private Long id;

    @Schema(description = "人力组编码")
    private String laborCode;

    @Schema(description = "人力组名称")
    private String laborName;

    @Schema(description = "人数")
    private Integer headcount;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "关联工位数量")
    private Integer stationCount;

    @Schema(description = "关联工位列表")
    private List<StationDTO> stations;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @Schema(description = "更新人")
    private String updatedBy;
}
