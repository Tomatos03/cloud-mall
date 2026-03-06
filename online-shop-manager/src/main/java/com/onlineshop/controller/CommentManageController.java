package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.comment.IGoodsCommentService;
import com.onlineshop.framework.models.comment.dto.ReplyCommentDTO;
import com.onlineshop.framework.models.comment.vo.GoodsCommentCardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 评论管理控制器
 *
 * @author : Tomatos
 * @date : 2025/12/28
 */
@RestController
@RequestMapping("/comment")
@PreAuthorize("hasAuthority('comment:view')")
public class CommentManageController {
    @Autowired
    private IGoodsCommentService goodsCommentService;

    /**
     * 分页查询评论
     */
    @GetMapping
    public IPage<GoodsCommentCardVO> getComments(
            @RequestParam("page") Integer page,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "hasReply", required = false) Boolean hasReply
    ) {
        return goodsCommentService.pageCommentsForMerchant(page, pageSize, hasReply);
    }

    /**
     * 回复评论
     */
    @PostMapping("/reply")
    @PreAuthorize("hasAuthority('comment:edit')")
    public boolean replyComment(@RequestBody ReplyCommentDTO replyCommentDTO) {
        return goodsCommentService.replyComment(replyCommentDTO);
    }
}
