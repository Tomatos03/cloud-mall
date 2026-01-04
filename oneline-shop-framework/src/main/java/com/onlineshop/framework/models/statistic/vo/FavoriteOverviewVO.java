package com.onlineshop.framework.models.statistic.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteOverviewVO implements Serializable {
    private Integer todayFavoriteAdd;
    private Integer todayFavoriteCancel;
    // TODO: 因为数据库结构原因, 下面的数据暂时统计不了
    private Integer todayFavoriteNetIncrease;
    private Integer totalFavoriteCount;
}