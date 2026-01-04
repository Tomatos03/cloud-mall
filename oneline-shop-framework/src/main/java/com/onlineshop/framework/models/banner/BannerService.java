package com.onlineshop.framework.models.banner;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineshop.framework.models.banner.vo.BannerVO;
import com.onlineshop.framework.models.banner.vo.HomeBannerVO;
import com.onlineshop.framework.models.goods.Goods;
import com.onlineshop.framework.models.goods.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public boolean toggleRecommend(Long id, Boolean isRecommend) {
        Banner banner = this.getById(id);
        if (banner != null && isRecommend != null) {
            banner.setIsRecommend(isRecommend);
            return this.updateById(banner);
        }
        return false;
    }

    @Override
    public List<HomeBannerVO> getRecommendBanner() {
        return lambdaQuery().eq(Banner::getIsRecommend, 1)
                            .list()
                            .stream()
                            .map(banner ->
                                         BeanUtil.copyProperties(banner, HomeBannerVO.class))
                            .toList();
    }

    @Override
    public IPage<BannerVO> pageBannerVO(int pageNum, int pageSize, String keyword, Boolean status) {
        Page<Banner> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(keyword != null && !keyword.isEmpty(), Banner::getTitle, keyword);
        queryWrapper.eq(status != null, Banner::getIsRecommend, status);
        IPage<Banner> bannerPage = this.page(page, queryWrapper);

        List<Banner> records = bannerPage.getRecords();
        Map<Long, String> goodsIdToNameMap = getGoodsIdToNameMap(
                records.stream()
                       .map(Banner::getGoodsId)
                       .toList()
        );
        List<BannerVO> voList = records.stream()
                                       .map(banner -> {
                                           BannerVO vo = BeanUtil.copyProperties(banner,
                                                                                 BannerVO.class);
                                           vo.setGoodsName(
                                                   goodsIdToNameMap.get(banner.getGoodsId()));
                                           return vo;
                                       })
                                       .toList();
        Page<BannerVO> voPage = new Page<>(pageNum, pageSize, bannerPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 根据商品ID列表获取商品ID到商品名称的映射
     */
    private Map<Long, String> getGoodsIdToNameMap(List<Long> goodsIds) {
        if (goodsIds == null || goodsIds.isEmpty()) {
            return new HashMap<>();
        }
        List<Goods> goodsList = goodsService.queryByIds(goodsIds);
        Map<Long, String> goodsIdToNameMap = new HashMap<>(goodsList.size());
        for (Goods goods : goodsList) {
            goodsIdToNameMap.put(goods.getId(), goods.getName());
        }
        return goodsIdToNameMap;
    }
}