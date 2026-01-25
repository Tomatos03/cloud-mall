package com.onlineshop.framework.models.comment;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.enums.BizErrorCode;
import com.onlineshop.framework.exception.BusinessException;
import com.onlineshop.framework.models.comment.dto.CommentQueryDTO;
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
import com.onlineshop.framework.utils.context.UserContextHolder;
import com.onlineshop.framework.utils.image.ImageUtil;
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
        validateOrderItem(createCommentDTO.getOrderItemId());
        validateCommentNotExists(createCommentDTO.getOrderItemId());
        markOrderItemCommented(createCommentDTO);

        User user = userService.getById(UserContextHolder.getUserId());
        Long orderId = getCommentGoodsOrderId(createCommentDTO.getOrderNo());
        GoodsComment comment = buildGoodsComment(user, createCommentDTO, orderId);
        return this.save(comment);
    }

    @Override
    public IPage<GoodsCommentVO> pageGoodsComment(CommentQueryDTO queryDTO) {
        QueryWrapper<GoodsComment> wrapper = new QueryWrapper<>();
        wrapper.eq("goods_id", queryDTO.getGoodsId());
        wrapper.orderByDesc("create_time");

        Page<GoodsComment> pageObj = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        IPage<GoodsComment> page = this.page(pageObj, wrapper);

        List<GoodsComment> deduplicatedComments = deduplicateCommentsByGoodsAndUser(page.getRecords());
        page.setRecords(deduplicatedComments);
        page.setTotal(deduplicatedComments.size());

        return page.convert(this::buildCommentVO);
    }

    @Override
    public IPage<GoodsCommentCardVO> getCommentsForMerchant(int page, int size, Boolean hasReply) {
        List<Long> goodsIdList = goodsService.lambdaQuery()
                                             .eq(Goods::getStoreId, UserContextHolder.getStoreId())
                                             .list()
                                             .stream()
                                             .map(Goods::getId)
                                             .toList();
        if (CollectionUtil.isEmpty(goodsIdList)) {
            return new Page<>(0, 0);
        }

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
        Page<GoodsComment> result = this.page(pageObj, queryWrapper);

        List<GoodsComment> deduplicateComments = deduplicateCommentsByGoodsAndUser(pageObj.getRecords());
        result.setRecords(deduplicateComments);
        result.setTotal(deduplicateComments.size());

        return result.convert(this::createCommentCardVO);
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
        return GoodsCommentCardVO.builder()
                                 .commentId(comment.getId())
                                 .orderNo(order.getNo())
                                 .goodsName(orderItem.getGoodsName())
                                 .goodsMainImageUrl(orderItem.getGoodsMainImageUrl())
                                 .buyerName(
                                         getDisplayName(
                                                 comment.getIsAnonymous(),
                                                 comment.getUserNickname()
                                         )
                                 )
                                 .rate(comment.getRating())
                                 .comment(comment.getContent())
                                 .reply(comment.getReply())
                                 .createTime(comment.getCreateTime())
                                 .build();
    }

    /**
     * 根据是否匿名返回用户名称
     * @param isAnonymous 是否匿名
     * @param userNickname 用户昵称
     * @return 显示名称
     */
    private String getDisplayName(Boolean isAnonymous, String userNickname) {
        return Boolean.TRUE.equals(isAnonymous) ? "匿名用户" : userNickname;
    }

    /**
     * 根据goodId和userId进行去重，保留时间最新的数据
     */
    private List<GoodsComment> deduplicateCommentsByGoodsAndUser(List<GoodsComment> comments) {
        if (CollectionUtil.isEmpty(comments)) {
            return comments;
        }
        return comments.stream()
                       .collect(
                               Collectors.toMap(
                                       comment -> comment.getGoodsId() + "_" + comment.getUserId(),
                                       comment -> comment,
                                       (existing, newer) ->
                                               newer.getCreateTime()
                                                    .isAfter(existing.getCreateTime())
                                                       ? newer
                                                       : existing
                               )
                       )
                       .values()
                       .stream()
                       .sorted((c1, c2) -> c2.getCreateTime()
                                             .compareTo(c1.getCreateTime()))
                       .toList();
    }

    /**
     * 构建评论VO对象
     */
    private GoodsCommentVO buildCommentVO(GoodsComment comment) {
        if (comment == null) {
            return null;
        }

        GoodsCommentVO.GoodsCommentVOBuilder builder = GoodsCommentVO.builder()
                                                                     .id(comment.getId())
                                                                     .reply(comment.getReply())
                                                                     .orderItemId(
                                                                             comment.getOrderItemId())
                                                                     .orderId(comment.getOrderId())
                                                                     .goodsId(comment.getGoodsId())
                                                                     .userId(comment.getUserId())
                                                                     .rating(comment.getRating())
                                                                     .content(comment.getContent())
                                                                     .specSnapshot(
                                                                             comment.getSkuSpecSnapshot())
                                                                     .createTime(
                                                                             comment.getCreateTime());
        if (comment.getImageUrls() != null) {
            builder.imageUrls(ImageUtil.createImageUrlList(comment.getImageUrls()));
        }
        if (!comment.getIsAnonymous()) {
            builder.userNickname(comment.getUserNickname());
            builder.userAvatar(comment.getUserAvatar());
            builder.userId(comment.getUserId());
        }
        return builder.build();
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

    // ========== 私有验证方法 ==========

    /**
     * 验证订单明细是否存在
     */
    private void validateOrderItem(Long orderItemId) {
        OrderItem orderItem = orderItemService.getById(orderItemId);
        if (orderItem == null) {
            throw new BusinessException(BizErrorCode.COMMENT_ORDER_ITEM_NOT_EXIST);
        }
    }

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
        boolean isSuccess = orderItemService.lambdaUpdate()
                                            .eq(OrderItem::getId, createCommentDTO.getOrderItemId())
                                            .set(OrderItem::getCommentStatus,
                                                 CommentStatus.COMMENTED.getValue())
                                            .update();
        if (!isSuccess) {
            throw new BusinessException(BizErrorCode.ORDER_ITEM_NOT_EXIST);
        }
    }

    private Long getCommentGoodsOrderId(String orderNo) {
        return orderService.lambdaQuery()
                           .eq(Order::getNo, orderNo)
                           .eq(Order::getUserId, UserContextHolder.getUserId())
                           .one()
                           .getId();
    }

    /**
     * 构建GoodsComment对象
     */
    private GoodsComment buildGoodsComment(
            User user,
            CreateCommentDTO createCommentDTO,
            Long orderId
    ) {
        String imageUrls = ImageUtil.joinImageUrls(createCommentDTO.getImageUrls());
        if (imageUrls.isEmpty()) {
            imageUrls = null;
        }
        return GoodsComment.builder()
                           .orderItemId(createCommentDTO.getOrderItemId())
                           .orderId(orderId)
                           .goodsId(createCommentDTO.getGoodsId())
                           .userId(user.getId())
                           .imageUrls(imageUrls)
                           .skuSpecSnapshot(createCommentDTO.getSpecSnapshot())
                           .userAvatar(user.getAvatarUrl())
                           .userNickname(user.getNickname())
                           .rating(createCommentDTO.getRating())
                           .content(createCommentDTO.getContent())
                           .isAnonymous(createCommentDTO.getIsAnonymous())
                           .createTime(LocalDateTime.now())
                           .build();
    }
}
