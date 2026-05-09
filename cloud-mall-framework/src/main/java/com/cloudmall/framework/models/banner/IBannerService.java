package com.cloudmall.framework.models.banner;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.framework.models.banner.dto.BannerDTO;
import com.cloudmall.framework.models.banner.vo.BannerVO;
import com.cloudmall.framework.models.banner.vo.HomeBannerVO;

import java.util.List;

/**
 * 轮播图服务接口
 *
 * @author Tomatos
 * @date 2025/12/17
 */
public interface IBannerService extends IService<Banner> {
    /**
     * 创建或更新轮播图
     * 保存前会验证关联商品是否存在
     *
     * @param dto 轮播图创建/更新DTO
     * @return 是否成功
     */
    boolean saveOrUpdateBanner(BannerDTO dto);

    boolean toggleRecommend(Long id, Boolean status);

    List<HomeBannerVO> getRecommendBanner();

    /**
     * 分页查询BannerVO，包含商品名称
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   推荐状态
     * @return BannerVO分页结果
     */
    IPage<BannerVO> pageBannerVO(int pageNum, int pageSize, Boolean status);
}