package com.onlineshop.framework.models.search.enums;

import com.onlineshop.framework.common.enums.BizErrorCode;
import com.onlineshop.framework.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import java.util.Arrays;

/**
 * 搜索排序类型枚举
 * 支持：新品优先、新品、销量最高以及综合（默认）
 *
 * 说明：
 * - NEWEST 表示按创建时间降序（新品优先）
 * - SALES 表示按销量降序（销量最高）
 * - COMPREHENSIVE 表示综合排序（不指定具体字段）
 */
@Getter
@AllArgsConstructor
public enum SortType {
    COMPREHENSIVE("comprehensive", "综合", null, null),

    /** 新品优先（按 createTime 降序） */
    NEWEST("newest", "新品优先", "createTime", Sort.Direction.DESC),

    /** 销量最高（按 sales 降序） */
    SALES("sales", "销量最高", "sales", Sort.Direction.DESC),

    /** 价格升序（按最小价格升序） */
    PRICE_ASC("price_asc", "价格升序", "minPrice", Sort.Direction.ASC),

    /** 价格降序（按最小价格降序） */
    PRICE_DESC("price_desc", "价格降序", "minPrice", Sort.Direction.DESC);

    private final String code;
    private final String description;
    private final String field;
    private final Sort.Direction direction;

    /**
     * 根据 code 获取排序类型
     *
     * @param code 排序代码
     * @return 对应的 SortType
     */
    public static SortType of(String code) {
        if (code == null || code.isBlank()) {
            return COMPREHENSIVE;
        }
        String normalizedCode = code.trim();
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(normalizedCode)
                        || type.name()
                                .equalsIgnoreCase(normalizedCode))
                .findFirst()
                .orElseThrow(() -> new BizException(BizErrorCode.UNKNOWN_SEARCH_ORDER_TYPE));
    }
}
