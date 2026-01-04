package com.onlineshop.controller.merchant;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.comment.IGoodsCommentService;
import com.onlineshop.framework.models.comment.dto.ReplyCommentDTO;
import com.onlineshop.framework.models.comment.vo.GoodsCommentCardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/28
 */
@RestController
@RequestMapping("/manager/merchant/comments")
public class MerchantCommentController {
    @Autowired
    private IGoodsCommentService goodsCommentService;

    @GetMapping
    public IPage<GoodsCommentCardVO> getComments(
            @RequestParam("page") Integer page,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "hasReply", required = false) Boolean hasReply
    ) {
        return goodsCommentService.getCommentsForMerchant(page, pageSize, hasReply);
    }

    @PostMapping("reply")
    public boolean replyComment(@RequestBody ReplyCommentDTO replyCommentDTO) {
        return goodsCommentService.replyComment(replyCommentDTO);
    }
}
