package com.onlineshop.framework.models.seckill.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.onlineshop.framework.models.seckill.application.vo.SeckillActivityGoodsPageVO;
import com.onlineshop.framework.models.seckill.application.vo.SeckillGoodsWebDetailVO;
import com.onlineshop.framework.models.seckill.application.vo.SeckillParticipateResultVO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillGoodsParamsDTO;

/**
 * 秒杀应用服务
 * 协调各个service，完成完整的秒杀业务流程
 * 负责事务边界和流程编排
 */
public interface SeckillAppService {
    
    /**
     * 参与秒杀（完整业务流程）
     * 用户购买指定秒杀商品，协调限流、库存检查、订单生成等操作
     *
     * @param seckillGoodsId 秒杀商品ID
     * @param quantity 购买数量
     * @return 秒杀参与结果
     */
    SeckillParticipateResultVO participateSeckill(Long seckillGoodsId, Integer quantity);

    /**
     * 查询活动中的秒杀商品（分页）
     * 支持按 merchantId 可选过滤
     *
     * @param params 秒杀商品查询参数
     * @return 秒杀商品分页结果
     */
    IPage<SeckillGoodsDTO> pageSeckillActivityGoods(SeckillGoodsParamsDTO params);

    /**
     * 分页查询指定时间对应整点活动及其商品
     *
     * @param params     秒杀商品查询参数（分页参数沿用 SeckillGoodsParamsDTO）
     * @param targetTime 指定时间
     * @return 指定时间整点活动与商品分页结果
     */
    SeckillActivityGoodsPageVO pageHourActivityGoods(SeckillGoodsParamsDTO params, LocalDateTime targetTime);

    /**
     * 查询指定日期全部场次活动及其商品
     *
     * @param params     秒杀商品查询参数（分页参数沿用 SeckillGoodsParamsDTO）
     * @param targetDate 指定日期
     * @return 指定日期全部场次活动与商品分页结果
     */
    List<SeckillActivityGoodsPageVO> listDayActivityGoods(SeckillGoodsParamsDTO params, LocalDate targetDate);

    /**
     * 获取秒杀商品详情（秒杀信息 + SPU详情）
     *
     * @param seckillGoodsId 秒杀商品ID
     * @return 秒杀商品聚合详情
     */
    SeckillGoodsWebDetailVO getSeckillGoodsWebDetail(Long seckillGoodsId);
}
