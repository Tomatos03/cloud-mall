package com.onlineshop.framework.models.comment;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.comment.dto.CommentParamsDTO;
import com.onlineshop.framework.models.comment.dto.CreateCommentDTO;
import com.onlineshop.framework.models.comment.dto.ReplyCommentDTO;
import com.onlineshop.framework.models.comment.vo.GoodsCommentCardVO;
import com.onlineshop.framework.models.comment.vo.GoodsCommentVO;

/**
 * 商品评论 Service 接口
 */
public interface IGoodsCommentService extends IService<GoodsComment> {

    /**
     * 创建商品评论
     *
     * @param createCommentDTO 评论信息
     * @return 是否创建成功
     */
    boolean addComment(CreateCommentDTO createCommentDTO);

    /**
     * 根据商品ID分页查询评论
     *
     * @param goodsId  商品ID
     * @param page     页码，从1开始
     * @param size     每页数量
     * @return 分页结果
     */
    IPage<GoodsCommentVO> pageGoodsComment(CommentParamsDTO queryDTO);

    /**
     * 管理员分页查询所有评论（卡片视图），支持按是否有回复进行过滤
     *
     * @param page    页码，从1开始
     * @param size    每页数量
     * @param hasReply 是否有回复，null表示不过滤
     * @return 分页结果
     */
    IPage<GoodsCommentCardVO> pageCommentsForMerchant(int page, int size, Boolean hasReply);

    boolean replyComment(ReplyCommentDTO replyContent);
}