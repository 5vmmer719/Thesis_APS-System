package com.aps.dto.request.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评论请求
 */
@Data
@Schema(description = "评论请求")
public class CommentRequest {

    @Schema(description = "评论内容")
    private String comment;
}
