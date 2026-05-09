package com.cloudmall.framework.models.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单创建结果DTO
 *
 * @author : Tomatos
 * @date : 2025/12/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateResultDTO {
    public static final String MOCK_PAY_QR_CODE =
            "data:image/png;base64,"
                    + "iVBORw0KGgoAAAANSUhEUgAAAGMAAABjAQMAAAC19SzWAAAABlBMVEUAAAD///+l2Z/dAAAAAnRSTlP//8i138cAAAAJcEhZ"
                    + "cwAACxIAAAsSAdLdfvwAAADvSURBVDiNzdSxjcQgEAXQsQjIjgaQaMMZLZkGvEcD65bIaAOJBkxGYO3cIK90e4EZgl3pJvILMM"
                    + "xnBOBrwT/WDuAwuQATp4KHq+AifXAKerFaRVo9IKndmNZB4bHIXH5PdqnWX9Sv3V6pVfiT4JV2C7cofE0Tp4LZU2zRFE4PCas8V"
                    + "DTIyuLdmo0OzqlUurG0Stw47VL4lsS5riesSVWYwjOXnkIuNT+kvnHarZ5QfIPYOFEVmlY8p6Anulv6t39m1lOb6xaYVqwog5AW"
                    + "EH5AqzV3aYY0w9ecFCua6zn7cLCi/hYptnDu0NMnXpv36wd7xLLS6ysBOgAAAABJRU5ErkJggg==";

    /**
     * 订单编号/父订单号
     */
    private String orderNo;
    private LocalDateTime expireTime;
    private String payQrCode;
}
