package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.framework.models.comment.IGoodsCommentService;
import com.cloudmall.framework.models.comment.dto.ReplyCommentDTO;
import com.cloudmall.framework.models.comment.vo.GoodsCommentCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/11
 */
@RestController
@RequestMapping("/merchant/comments")
@RequiredArgsConstructor
public class CommentMerchantController {
    private final IGoodsCommentService goodsCommentService;

    @GetMapping("/page")
    public IPage<GoodsCommentCardVO> pageComments(
            @RequestParam int page,
            @RequestParam("pageSize") int size,
            @RequestParam(required = false) Boolean hasReply
    ) {
        return goodsCommentService.pageCommentsForMerchant(page, size, hasReply);
    }

    @PostMapping("/reply")
    public boolean replyComment(ReplyCommentDTO replyCommentDTO) {
        return goodsCommentService.replyComment(replyCommentDTO);
    }
}
