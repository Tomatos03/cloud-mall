package com.onlineshop.framework.models.search.repository;

import com.onlineshop.framework.models.search.index.GoodsIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品索引Repository
 * 用于Elasticsearch的数据持久化操作
 *
 * @author : Tomatos
 * @date : 2025/1/1
 */
@Repository
public interface GoodsIndexRepository extends ElasticsearchRepository<GoodsIndex, Long> {
}