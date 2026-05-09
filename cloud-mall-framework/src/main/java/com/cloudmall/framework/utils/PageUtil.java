package com.cloudmall.framework.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/29
 */
public class PageUtil {

    /**
     * 将ES分页结果转换为MyBatis-Plus分页结果
     */
    public static <T> IPage<T> toIPage(SearchHits<T> searchHits, long pageNo, long pageSize) {
        List<T> records = searchHits.getSearchHits()
                                    .stream()
                                    .map(SearchHit::getContent)
                                    .collect(Collectors.toList());

        Page<T> page = new Page<>(pageNo, pageSize);
        page.setRecords(records);
        page.setTotal(searchHits.getTotalHits());
        return page;
    }
}
