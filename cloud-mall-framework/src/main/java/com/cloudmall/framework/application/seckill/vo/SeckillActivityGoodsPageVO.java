package com.cloudmall.framework.application.seckill.vo;

import java.io.Serializable;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.framework.models.seckill.dto.SeckillGoodsDTO;
import com.cloudmall.framework.models.seckill.vo.SeckillActivityVO;

/**
 * 秒杀活动与商品分页信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillActivityGoodsPageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 秒杀活动详情
     */
    private SeckillActivityVO activity;

    /**
     * 活动商品分页数据
     */
    private IPage<SeckillGoodsDTO> goodsPage;
}
