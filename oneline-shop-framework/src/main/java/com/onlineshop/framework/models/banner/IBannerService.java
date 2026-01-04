package com.onlineshop.framework.models.banner;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.banner.vo.BannerVO;
import com.onlineshop.framework.models.banner.vo.HomeBannerVO;

import java.util.List;

/**
 * 轮播图服务接口
 *
 * @author Tomatos
 * @date 2025/12/17
 */
public interface IBannerService extends IService<Banner> {
    boolean toggleRecommend(Long id, Boolean status);

    List<HomeBannerVO> getRecommendBanner();

    /**
     * 分页查询BannerVO，包含商品名称
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键词
     * @param status
     * @return BannerVO分页结果
     */
    IPage<BannerVO> pageBannerVO(int pageNum, int pageSize, String keyword, Boolean status);
}