package com.cloudmall.framework.models.search.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import co.elastic.clients.json.JsonData;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.cloudmall.framework.models.goods.spu.Goods;
import com.cloudmall.framework.models.goods.spu.IGoodsService;
import com.cloudmall.framework.models.goods.spu.dto.GoodsSearchDTO;
import com.cloudmall.framework.models.goods.spu.vo.GoodsCardVO;
import com.cloudmall.framework.models.search.enums.SortType;
import com.cloudmall.framework.models.search.index.GoodsIndex;
import com.cloudmall.framework.models.search.repository.GoodsIndexRepository;
import com.cloudmall.framework.models.search.service.IGoodsEsService;
import com.cloudmall.framework.utils.PageUtil;
import com.cloudmall.framework.utils.money.Money;

/**
 * 商品搜索服务实现
 * 基于Elasticsearch实现商品全文搜索功能
 *
 * @author : Tomatos
 * @date : 2025/1/1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsEsService implements IGoodsEsService {
    private final GoodsIndexRepository goodsIndexRepository;
    private final IGoodsService goodsService;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void saveGoodsIndex(@NotNull GoodsIndex goodsIndex) {
        try {
            goodsIndexRepository.save(goodsIndex);
            log.debug("商品索引保存成功，goodsId: {}", goodsIndex.getId());
        } catch (Exception e) {
            log.error("商品索引保存失败，goodsId: {}", goodsIndex.getId(), e);
        }
    }

    @Override
    public void saveGoodsIndexBatch(@NonNull Iterable<GoodsIndex> goodsIndexList) {
        try {
            goodsIndexRepository.saveAll(goodsIndexList);
            log.debug("商品索引批量保存成功");
        } catch (Exception e) {
            log.error("商品索引批量保存失败", e);
        }
    }

    @Override
    public void deleteGoodsIndex(@NonNull Long goodsId) {
        try {
            goodsIndexRepository.deleteById(goodsId);
            log.debug("商品索引删除成功，goodsId: {}", goodsId);
        } catch (Exception e) {
            log.error("商品索引删除失败，goodsId: {}", goodsId, e);
        }
    }

    @Override
    public void deleteGoodsIndexBatch(Iterable<Long> goodsIds) {
        if (goodsIds == null) {
            return;
        }
        try {
            goodsIndexRepository.deleteAllById(goodsIds);
            log.debug("商品索引批量删除成功");
        } catch (Exception e) {
            log.error("商品索引批量删除失败", e);
        }
    }

    @Override
    public IPage<GoodsCardVO> searchGoods(@NonNull GoodsSearchDTO searchDTO) {
        try {
            int pageNum = searchDTO.getPage();
            int pageSize = searchDTO.getPageSize();

            // es分页从0开始
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            Query query = buildQuery(searchDTO, pageable);
            setSortType(query, SortType.of(searchDTO.getSortType()));
            SearchHits<GoodsIndex> searchHits = elasticsearchOperations.search(query,
                                                                               GoodsIndex.class);

            IPage<GoodsIndex> page = PageUtil.toIPage(searchHits, searchDTO.getPage(),
                                                      searchDTO.getPageSize());
            log.debug("商品搜索成功，关键词: {}，结果数: {}", searchDTO.getKeyword(), page.getSize());
            return page.convert(GoodsCardVO::convertGoodsCardVO);
        } catch (Exception e) {
            log.error("商品搜索失败", e);
            return new Page<>(searchDTO.getPage(), searchDTO.getPageSize(), 0);
        }
    }

    @Override
    @Transactional
    public void rebuildAllGoodsIndex() {
        try {
            log.info("开始重建商品索引...");
            elasticsearchOperations.indexOps(GoodsIndex.class)
                                   .delete();
            elasticsearchOperations.indexOps(GoodsIndex.class)
                                   .create();

            // 从数据库获取所有商品
            List<Goods> allGoods = goodsService.list();
            log.info("从数据库获取商品总数: {}", allGoods.size());

            if (allGoods.isEmpty()) {
                log.info("数据库中没有商品数据");
                return;
            }

            // 转换为索引对象并批量保存
            List<GoodsIndex> indexList = allGoods.stream()
                                                 .map(GoodsIndex::convertToGoodsIndex)
                                                 .collect(Collectors.toList());

            saveGoodsIndexBatch(indexList);
            log.info("商品索引重建完成，总数: {}", indexList.size());
        } catch (Exception e) {
            log.error("商品索引重建失败", e);
            throw new RuntimeException("商品索引重建失败", e);
        }
    }

    private Query buildQuery(GoodsSearchDTO searchDTO, Pageable pageable) {
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder()
                .withPageable(pageable)
                .withFilter(builder -> builder
                        .term(termBuilder -> termBuilder
                                .field("status")
                                .value(true)
                        )
                );
        if (searchDTO.getCategoryId() != null) {
            queryBuilder = queryBuilder.withFilter(builder -> builder
                    .term(termBuilder -> termBuilder
                            .field("categoryPathIds")
                            .value(searchDTO.getCategoryId())
                    )
            );
        }
        if (searchDTO.getStoreId() != null) {
            queryBuilder = queryBuilder.withFilter(builder -> builder
                    .term(termBuilder -> termBuilder
                            .field("storeId")
                            .value(searchDTO.getStoreId())
                    )
            );
        }

        if (StringUtils.hasText(searchDTO.getKeyword())) {
            queryBuilder = queryBuilder.withQuery(builder -> builder
                    .multiMatch(multiMatchBuilder -> multiMatchBuilder
                            .fields("name^3", "sellPoint^2")
                            .query(searchDTO.getKeyword())
                    )
            );
        }

        Long minPriceCents = parsePriceToCents(searchDTO.getMinPrice());
        if (minPriceCents != null) {
            queryBuilder = queryBuilder.withFilter(builder -> builder
                    .range(rangeBuilder -> rangeBuilder
                            .untyped(untypedBuilder -> untypedBuilder
                                    .field("minPrice")
                                    .gte(JsonData.of(minPriceCents))
                            ))
            );
        }

        Long maxPriceCents = parsePriceToCents(searchDTO.getMaxPrice());
        if (maxPriceCents != null) {
            queryBuilder = queryBuilder.withFilter(builder -> builder
                    .range(rangeBuilder -> rangeBuilder
                            .untyped(untypedBuilder -> untypedBuilder
                                    .field("maxPrice")
                                    .lte(JsonData.of(maxPriceCents))
                            ))
            );
        }
        return queryBuilder.build();
    }

    private Long parsePriceToCents(String priceYuan) {
        if (!StringUtils.hasText(priceYuan)) {
            return null;
        }
        return Money.ofYuan(priceYuan.trim())
                    .getCents();
    }

    private void setSortType(@NonNull Query query, @NonNull SortType type) {
        if (type == SortType.COMPREHENSIVE) {
            return;
        }
        Sort sort = Sort.by(type.getDirection(), type.getField());
        query.addSort(sort);
    }

}
