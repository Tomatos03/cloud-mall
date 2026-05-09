package com.cloudmall.framework.models.banner;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudmall.framework.common.enums.BizErrorCode;
import com.cloudmall.framework.exception.BizException;
import com.cloudmall.framework.models.banner.dto.BannerDTO;
import com.cloudmall.framework.models.banner.vo.BannerVO;
import com.cloudmall.framework.models.banner.vo.HomeBannerVO;
import com.cloudmall.framework.models.goods.spu.Goods;
import com.cloudmall.framework.models.goods.spu.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 轮播图服务实现类
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@Service
public class BannerService extends ServiceImpl<BannerMapper, Banner> implements IBannerService {
    @Autowired
    private IGoodsService goodsService;

    @CacheEvict(value = "banner", allEntries = true)
    @Override
    public boolean saveOrUpdateBanner(BannerDTO dto) {
        // 验证关联的商品是否存在
        Goods goods = goodsService.getById(dto.getGoodsId());
        if (goods == null) {
            throw new BizException(BizErrorCode.BANNER_GOODS_NOT_EXIST);
        }

        // 将DTO转换为Banner对象
        Banner banner = BeanUtil.copyProperties(dto, Banner.class);
        banner.setGoodsName(goods.getName());
        banner.setGoodsId(goods.getId());
        return this.saveOrUpdate(banner);
    }

    @Override
    public boolean toggleRecommend(Long id, Boolean isRecommend) {
        return lambdaUpdate().eq(Banner::getIsRecommend, isRecommend)
                             .eq(Banner::getGoodsId, id)
                             .update();
    }

    @Cacheable(value = "banner", key = "'recommend'")
    @Override
    public List<HomeBannerVO> getRecommendBanner() {
        return lambdaQuery().eq(Banner::getIsRecommend, true)
                            .list()
                            .stream()
                            .map(this::convertHomeBannerVO)
                            .collect(Collectors.toList());
    }

    private HomeBannerVO convertHomeBannerVO(Banner banner) {
        return HomeBannerVO.builder()
                           .imageUrl(banner.getImageUrl())
                           .goodsId(banner.getGoodsId())
                           .build();
    }

    @Override
    public IPage<BannerVO> pageBannerVO(int pageNum, int pageSize, Boolean status) {
        IPage<Banner> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status != null, Banner::getIsRecommend, status);

        IPage<Banner> bannerPage = this.page(page, queryWrapper);
        return bannerPage.convert(this::convertBannerVO);
    }

    private BannerVO convertBannerVO(Banner banner) {
        return BeanUtil.copyProperties(banner, BannerVO.class);
    }
}