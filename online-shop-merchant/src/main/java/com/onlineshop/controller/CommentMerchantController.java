package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.comment.IGoodsCommentService;
import com.onlineshop.framework.models.comment.dto.ReplyCommentDTO;
import com.onlineshop.framework.models.comment.vo.GoodsCommentCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/11
 */
@RestController
@RequestMapping("/comments")
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
