package com.onlineshop.framework.models.comment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.comment.dto.CreateCommentDTO;
import com.onlineshop.framework.models.comment.dto.ReplyCommentDTO;
import com.onlineshop.framework.models.comment.vo.GoodsCommentCardVO;
import com.onlineshop.framework.models.comment.vo.GoodsCommentVO;
import com.onlineshop.framework.models.goods.spu.Goods;
import com.onlineshop.framework.models.goods.spu.IGoodsService;
import com.onlineshop.framework.models.order.entity.Order;
import com.onlineshop.framework.models.order.entity.OrderItem;
import com.onlineshop.framework.models.order.service.IOrderItemService;
import com.onlineshop.framework.models.order.service.IOrderService;
import com.onlineshop.framework.models.user.IUserService;
import com.onlineshop.framework.models.user.User;
import com.onlineshop.framework.models.user.dto.UserInfoDTO;
import com.onlineshop.framework.utils.context.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsCommentService extends ServiceImpl<GoodsCommentMapper, GoodsComment> implements IGoodsCommentService {

    @Autowired
    private IOrderItemService orderItemService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IGoodsService goodsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addComment(CreateCommentDTO createCommentDTO) {
        validateCommentInput(createCommentDTO);
        validateOrderItemExists(createCommentDTO.getOrderItemId());
        validateCommentNotExists(createCommentDTO.getOrderItemId());
        markOrderItemCommented(createCommentDTO);

        UserInfoDTO userInfoDTO = getUserInfo();
        Long orderId = getOrderId(createCommentDTO);
        GoodsComment comment = buildGoodsComment(userInfoDTO, createCommentDTO, orderId);
        return this.save(comment);
    }

    @Override
    public IPage<GoodsCommentVO> getCommentsByGoodsId(Long goodsId, int page, int size) {
        QueryWrapper<GoodsComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("goods_id", goodsId)
                    .orderByDesc("create_time");

        Page<GoodsComment> pageObj = new Page<>(page, size);
        IPage<GoodsComment> commentPage = this.page(pageObj, queryWrapper);

        // 转换为VO
        List<GoodsCommentVO> voList = commentPage.getRecords()
                                                 .stream()
                                                 .map(this::buildCommentVO)
                                                 .collect(Collectors.toList());

        Page<GoodsCommentVO> resultPage = new Page<>();
        resultPage.setRecords(voList);
        resultPage.setTotal(commentPage.getTotal());
        resultPage.setSize(commentPage.getSize());
        resultPage.setCurrent(commentPage.getCurrent());
        resultPage.setPages(commentPage.getPages());
        return resultPage;
    }

    @Override
    public IPage<GoodsCommentCardVO> getCommentsForMerchant(int page, int size, Boolean hasReply) {
        List<Long> goodsIdList = goodsService.lambdaQuery()
                                             .eq(Goods::getStoreId, UserContextHolder.getStoreId())
                                             .list()
                                             .stream()
                                             .map(Goods::getId)
                                             .toList();

        QueryWrapper<GoodsComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.in("goods_id", goodsIdList);

        if (hasReply != null) {
            if (hasReply) {
                queryWrapper.isNotNull("reply");
            } else {
                queryWrapper.isNull("reply");
            }
        }

        Page<GoodsComment> pageObj = new Page<>(page, size);
        IPage<GoodsComment> commentPage = this.page(pageObj, queryWrapper);
        return commentPage.convert(this::createCommentCardVO);
    }

    @Override
    public boolean replyComment(ReplyCommentDTO replyCommentDTO) {
        // TODO: 存在漏洞, 暂时不处理
        return lambdaUpdate().eq(GoodsComment::getId, replyCommentDTO.getCommentId())
                             .isNull(GoodsComment::getReply)
                             .set(GoodsComment::getReply, replyCommentDTO.getReplyContent())
                             .update();
    }

    /**
     * 构建评论卡片VO对象
     */
    private GoodsCommentCardVO createCommentCardVO(GoodsComment comment) {
        if (comment == null) {
            return null;
        }

        Order order = orderService.getById(comment.getOrderId());
        OrderItem orderItem = orderItemService.getById(comment.getOrderItemId());
        String buyerName = comment.getIsAnonymous() == 1 ? "匿名用户" : comment.getUserNickname();

        return GoodsCommentCardVO.builder()
                                 .commentId(comment.getId())
                                 .orderNo(order.getNo())
                                 .goodsName(orderItem.getGoodsName())
                                 .goodsImage(orderItem.getGoodsImg())
                                 .buyerName(buyerName)
                                 .rate(comment.getRating())
                                 .comment(comment.getContent())
                                 .reply(comment.getReply())
                                 .createTime(comment.getCreateTime())
                                 .build();
    }

    /**
     * 构建评论VO对象
     */
    private GoodsCommentVO buildCommentVO(GoodsComment comment) {
        if (comment == null) {
            return null;
        }

        // 根据匿名标记决定是否显示用户信息
        String displayNickname = "匿名用户";
        String displayAvatar = null;

        if (comment.getIsAnonymous() == 0) {
            displayNickname = comment.getUserNickname();
            displayAvatar = comment.getUserAvatar();
        }

        List<String> imageList = null;
        if (StringUtils.hasText(comment.getImages())) {
            imageList = List.of(comment.getImages()
                                       .split(","));
        }

        return GoodsCommentVO.builder()
                             .id(comment.getId())
                             .reply(comment.getReply())
                             .orderItemId(comment.getOrderItemId())
                             .orderId(comment.getOrderId())
                             .goodsId(comment.getGoodsId())
                             .userId(comment.getUserId())
                             .userNickname(displayNickname)
                             .userAvatar(displayAvatar)
                             .rating(comment.getRating())
                             .content(comment.getContent())
                             .images(imageList)
                             .isAnonymous(comment.getIsAnonymous())
                             .createTime(comment.getCreateTime())
                             .build();
    }

    /**
     * 验证评论输入（评分和内容）
     */
    private void validateCommentInput(CreateCommentDTO createCommentDTO) {
        if (createCommentDTO.getRating() == null || createCommentDTO.getRating() < 1 || createCommentDTO.getRating() > 5) {
            throw new BusinessException(BizErrorCode.COMMENT_RATING_INVALID);
        }

        if (!StringUtils.hasText(createCommentDTO.getContent())) {
            throw new BusinessException(BizErrorCode.COMMENT_CONTENT_EMPTY);
        }
    }

    /**
     * 验证订单明细是否存在
     */
    private void validateOrderItemExists(Long orderItemId) {
        OrderItem orderItem = orderItemService.getById(orderItemId);
        if (orderItem == null) {
            throw new BusinessException(BizErrorCode.COMMENT_ORDER_ITEM_NOT_EXIST);
        }
    }

    // ========== 私有验证方法 ==========

    /**
     * 验证是否已评论（订单明细唯一）
     */
    private void validateCommentNotExists(Long orderItemId) {
        GoodsComment existComment = this.lambdaQuery()
                                        .eq(GoodsComment::getOrderItemId, orderItemId)
                                        .one();
        if (existComment != null) {
            throw new BusinessException(BizErrorCode.COMMENT_ALREADY_EXISTS);
        }
    }

    private void markOrderItemCommented(CreateCommentDTO createCommentDTO) {
        boolean update = orderItemService.lambdaUpdate()
                                         .eq(OrderItem::getId, createCommentDTO.getOrderItemId())
                                         .set(OrderItem::getCommentStatus,
                                              CommentStatus.COMMENTED.getValue())
                                         .update();
        if (!update) {
            throw new BusinessException(BizErrorCode.ORDER_ITEM_NOT_EXIST);
        }
    }

    private UserInfoDTO getUserInfo() {
        Long userId = UserContextHolder.getUserId();

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(BizErrorCode.USER_NOT_EXISTS);
        }
        return UserInfoDTO.builder()
                          .nickname(user.getNickname())
                          .avatarUrl(user.getAvatarUrl())
                          .build();
    }

    private Long getOrderId(CreateCommentDTO createCommentDTO) {
        return orderService.lambdaQuery()
                           .eq(Order::getNo, createCommentDTO.getOrderNo())
                           .eq(Order::getUserId, createCommentDTO.getUserId())
                           .one()
                           .getId();
    }

    /**
     * 构建GoodsComment对象
     */
    private GoodsComment buildGoodsComment(
            UserInfoDTO userInfoDTO,
            CreateCommentDTO createCommentDTO,
            Long orderId
    ) {
        return GoodsComment.builder()
                           .orderItemId(createCommentDTO.getOrderItemId())
                           .orderId(orderId)
                           .goodsId(createCommentDTO.getGoodsId())
                           .userId(UserContextHolder.getUserId())
                           .images(createCommentDTO.getImages())
                           .userAvatar(userInfoDTO.getAvatarUrl())
                           .userNickname(userInfoDTO.getNickname())
                           .rating(createCommentDTO.getRating())
                           .content(createCommentDTO.getContent())
                           .isAnonymous(createCommentDTO.getIsAnonymous())
                           .createTime(LocalDateTime.now())
                           .build();
    }
}
