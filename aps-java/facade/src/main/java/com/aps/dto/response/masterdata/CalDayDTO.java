package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 资源日历（日）DTO
 */
@Data
@Schema(description = "资源日历（日）")
public class CalDayDTO {

    @Schema(description = "日历ID")
    private Long id;

    @Schema(description = "资源类型：1-产线，2-工位，3-设备，4-模具，5-人力组")
    private Integer resourceType;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "业务日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bizDate;

    @Schema(description = "是否工作日：0-否，1-是")
    private Integer isWorkday;

    @Schema(description = "备注")
    private String remark;
}
