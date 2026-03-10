package com.onlineshop.framework.models.seckill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityDTO;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityParamsDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;

/**
 * 秒杀活动服务接口
 * 
 * 提供秒杀活动的各类业务操作：
 * - 活动CRUD（创建、查询、更新、删除）
 * - 活动启动和状态管理
 * - 审核申请管理（管理端）
 * 
 * @author Tomatos
 * @date 2025-01-10
 */
public interface SeckillActivityService extends IService<SeckillActivity> {

    /**
     * 获取秒杀活动详情，返回VO对象（包含实时库存和状态）
     *
     * @param id 秒杀活动ID
     * @return 秒杀活动VO
     */
    SeckillActivityVO getSeckillActivityVO(Long id);
    
    /**
     * 分页查询秒杀活动列表（返回VO对象，包含实时库存和状态）
     *
     * @param params 秒杀活动查询参数
     * @return 秒杀活动分页数据
     */
    IPage<SeckillActivityVO> listActivities(SeckillActivityParamsDTO params);
    

    /**
     * 创建秒杀活动
     *
     * @param dto 秒杀活动数据
     * @return 创建成功的秒杀活动VO
     */
    SeckillActivityVO createActivity(SeckillActivityDTO dto);
    
    /**
     * 更新秒杀活动
     *
     * @param id  秒杀活动ID
     * @param dto 秒杀活动数据
     * @return 更新后的秒杀活动VO
     */
    SeckillActivityVO updateActivity(Long id, SeckillActivityDTO dto);
    
    /**
     * 删除秒杀活动
     *
     * @param id 秒杀活动ID
     * @return 是否删除成功
     */
    boolean deleteActivity(Long id);
    
    /**
     * 启动秒杀活动
     * 注意：现在只负责更新活动状态，库存初始化由SeckillAppService处理
     *
     * @param id 秒杀活动ID
     * @return 是否启动成功
     */
    boolean startActivity(Long id);

    /**
     * 查询秒杀审核申请列表
     *
     * @param params 分页参数
     * @return 审核申请列表
     */
    IPage<?> listAuditApplies(SeckillActivityParamsDTO params);
}
