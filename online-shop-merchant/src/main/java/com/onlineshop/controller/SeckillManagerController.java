package com.onlineshop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import com.onlineshop.framework.models.seckill.dto.SeckillActivityDTO;
import com.onlineshop.framework.models.seckill.entity.SeckillActivity;
import com.onlineshop.framework.models.seckill.manager.SeckillManager;
import com.onlineshop.framework.models.seckill.service.SeckillActivityService;
import com.onlineshop.framework.models.seckill.vo.SeckillActivityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 秒杀管理端控制器
 * 处理后台秒杀活动管理相关请求
 *
 * @author Tomatos
 * @date 2025-01-10
 */
@Slf4j
@RestController
@RequestMapping("/manager/seckill")
public class SeckillManagerController {

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Autowired
    private SeckillManager seckillManager;

    /**
     * 创建秒杀活动
     *
     * @param dto 秒杀活动数据
     * @return 创建成功的秒杀活动
     */
    @PostMapping("/activity/create")
    public SeckillActivityVO createSeckillActivity(@RequestBody SeckillActivityDTO dto) {
        log.info("创建秒杀活动，商品ID: {}, 秒杀价格: {}", dto.getProductId(), dto.getSeckillPrice());
        
        SeckillActivity activity = new SeckillActivity();
        BeanUtils.copyProperties(dto, activity);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        
        seckillActivityService.save(activity);
        
        // 初始化Redis库存
        seckillManager.initializeStock(activity.getId());
        
        return convertToVO(activity);
    }

    /**
     * 编辑秒杀活动
     *
     * @param id  秒杀活动ID
     * @param dto 秒杀活动数据
     * @return 编辑后的秒杀活动
     */
    @PutMapping("/activity/{id}")
    public SeckillActivityVO updateSeckillActivity(@PathVariable Long id, @RequestBody SeckillActivityDTO dto) {
        log.info("编辑秒杀活动，ID: {}", id);
        
        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }
        
        BeanUtils.copyProperties(dto, activity, "id", "createTime");
        activity.setUpdateTime(LocalDateTime.now());
        
        seckillActivityService.updateById(activity);
        
        return convertToVO(activity);
    }

    /**
     * 获取秒杀活动详情
     *
     * @param id 秒杀活动ID
     * @return 秒杀活动详情
     */
    @GetMapping("/activity/{id}")
    public SeckillActivityVO getSeckillActivity(@PathVariable Long id) {
        log.info("查询秒杀活动详情，ID: {}", id);
        
        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }
        
        SeckillActivityVO vo = convertToVO(activity);
        // 获取当前库存
        Long remainingStock = seckillManager.getRemainingStock(id);
        vo.setRemainingStock(remainingStock.intValue());
        
        return vo;
    }

    /**
     * 分页查询秒杀活动
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 秒杀活动列表
     */
    @GetMapping("/activity/page")
    public IPage<SeckillActivityVO> pageSeckillActivity(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.info("分页查询秒杀活动，页码: {}, 每页数量: {}", pageNum, pageSize);
        
        Page<SeckillActivity> page = new Page<>(pageNum, pageSize);
        IPage<SeckillActivity> result = seckillActivityService.page(page, 
            new LambdaQueryWrapper<SeckillActivity>()
                .orderByDesc(SeckillActivity::getCreateTime));
        
        return result.convert(this::convertToVO);
    }

    /**
     * 删除秒杀活动
     *
     * @param id 秒杀活动ID
     * @return 是否删除成功
     */
    @DeleteMapping("/activity/{id}")
    public boolean deleteSeckillActivity(@PathVariable Long id) {
        log.info("删除秒杀活动，ID: {}", id);
        
        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }
        
        // 清除缓存
        seckillManager.clearSeckillCache(id);
        
        return seckillActivityService.removeById(id);
    }

    /**
     * 初始化秒杀活动库存缓存
     *
     * @param id 秒杀活动ID
     * @return 是否初始化成功
     */
    @PostMapping("/activity/{id}/init-stock")
    public boolean initSeckillStock(@PathVariable Long id) {
        log.info("初始化秒杀活动库存缓存，ID: {}", id);
        
        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }
        
        seckillManager.initializeStock(id);
        return true;
    }

    /**
     * 同步秒杀库存到数据库
     * 通常在秒杀活动结束后调用
     *
     * @param id 秒杀活动ID
     * @return 是否同步成功
     */
    @PostMapping("/activity/{id}/sync-stock")
    public boolean syncSeckillStock(@PathVariable Long id) {
        log.info("同步秒杀活动库存到数据库，ID: {}", id);
        
        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }
        
        seckillManager.syncStockToDatabase(id);
        return true;
    }

    /**
     * 获取秒杀活动的当前库存
     *
     * @param id 秒杀活动ID
     * @return 当前库存数量
     */
    @GetMapping("/activity/{id}/stock")
    public Long getSeckillStock(@PathVariable Long id) {
        log.info("获取秒杀活动库存，ID: {}", id);
        
        SeckillActivity activity = seckillActivityService.getById(id);
        if (activity == null) {
            throw new BizException(BizErrorCode.SECKILL_ACTIVITY_NOT_EXIST);
        }
        
        return seckillManager.getRemainingStock(id);
    }

    /**
     * 将Entity转换为VO
     */
    private SeckillActivityVO convertToVO(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        BeanUtils.copyProperties(activity, vo);
        
        // 设置活动状态
        Integer status = seckillManager.checkSeckillStatus(activity.getId());
        vo.setStatus(status);
        
        // 获取剩余库存
        Long remainingStock = seckillManager.getRemainingStock(activity.getId());
        vo.setRemainingStock(remainingStock.intValue());
        
        return vo;
    }
}