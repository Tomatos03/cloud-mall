package com.cloudmall.framework.models.comment.dto;

import lombok.Data;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/28
 */
@Data
public class ReplyCommentDTO {
    private Long commentId;
    private String replyContent;
}
