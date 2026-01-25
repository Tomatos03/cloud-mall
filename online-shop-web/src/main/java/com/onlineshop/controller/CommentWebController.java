package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.comment.IGoodsCommentService;
import com.onlineshop.framework.models.comment.dto.CommentQueryDTO;
import com.onlineshop.framework.models.comment.dto.CreateCommentDTO;
import com.onlineshop.framework.models.comment.vo.GoodsCommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品评论 Web 控制器
 *
 * @author : Tomatos
 * @date : 2025/12/25
 */
@RestController
@RequestMapping("/web/comments")
@Validated
public class CommentWebController {

    @Autowired
    private IGoodsCommentService goodsCommentService;

    /**
     * 创建商品评论
     *
     * @param createCommentDTO 评论信息
     * @return 是否创建成功
     */
    @PostMapping("/add")
    public boolean addComment(@RequestBody CreateCommentDTO createCommentDTO) {
        return goodsCommentService.addComment(createCommentDTO);
    }

    /**
     * 根据商品ID分页查询评论
     *
     * @param goodsId 商品ID
     * @param page    页码，从1开始
     * @param size    每页数量
     * @return 分页评论结果
     */
    @GetMapping
    public IPage<GoodsCommentVO> pageQueryComment(CommentQueryDTO queryDTO) {
        return goodsCommentService.pageGoodsComment(queryDTO);
    }
}
