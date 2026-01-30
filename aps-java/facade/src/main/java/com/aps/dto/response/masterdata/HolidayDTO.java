package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 节假日DTO
 */
@Data
@Schema(description = "节假日")
public class HolidayDTO {

    @Schema(description = "节假日ID")
    private Long id;

    @Schema(description = "业务日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bizDate;

    @Schema(description = "节假日名称")
    private String name;
}
