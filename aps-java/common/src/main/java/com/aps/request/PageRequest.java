package com.aps.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页请求")
public class PageRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    /**
     * 获取页码（最小为1）
     */
    public Integer getPageNum() {
        if (pageNum == null || pageNum < 1) {
            return 1;
        }
        return pageNum;
    }

    /**
     * 获取每页大小（范围 1-200）
     */
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        if (pageSize > 200) {
            return 200;
        }
        return pageSize;
    }
}
