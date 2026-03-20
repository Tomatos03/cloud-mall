package com.onlineshop.framework.models.search.enums;

import org.junit.jupiter.api.Test;

import com.onlineshop.framework.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortTypeTest {

    @Test
    void of_shouldParseFrontendLowercaseValues() {
        assertEquals(SortType.COMPREHENSIVE, SortType.of("comprehensive"));
        assertEquals(SortType.NEWEST, SortType.of("newest"));
        assertEquals(SortType.SALES, SortType.of("sales"));
        assertEquals(SortType.PRICE_ASC, SortType.of("price_asc"));
        assertEquals(SortType.PRICE_DESC, SortType.of("price_desc"));
    }

    @Test
    void of_shouldParseLegacyUppercaseValues() {
        assertEquals(SortType.COMPREHENSIVE, SortType.of("COMPREHENSIVE"));
        assertEquals(SortType.NEWEST, SortType.of("NEWEST"));
        assertEquals(SortType.SALES, SortType.of("SALES"));
        assertEquals(SortType.PRICE_ASC, SortType.of("PRICE_ASC"));
        assertEquals(SortType.PRICE_DESC, SortType.of("PRICE_DESC"));
    }

    @Test
    void of_shouldDefaultToComprehensiveWhenBlank() {
        assertEquals(SortType.COMPREHENSIVE, SortType.of(null));
        assertEquals(SortType.COMPREHENSIVE, SortType.of(""));
        assertEquals(SortType.COMPREHENSIVE, SortType.of("  "));
    }

    @Test
    void of_shouldThrowWhenUnknownType() {
        assertThrows(BizException.class, () -> SortType.of("unknown_type"));
    }
}
