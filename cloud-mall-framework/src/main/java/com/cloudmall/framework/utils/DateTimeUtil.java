package com.cloudmall.framework.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 日期时间工具类
 * 提供日期时间的常用操作方法
 *
 * @author : Tomatos
 * @date : 2026/01/27
 */
public class DateTimeUtil {

    private DateTimeUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 判断当前时间是否在指定时间范围内（包含边界）
     *
     * @param startDateTime 开始时间
     * @param endDateTime 结束时间
     * @return true 表示当前时间在范围内
     */
    public static boolean isInTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startDateTime) && !now.isAfter(endDateTime);
    }

    /**
     * 判断指定时间是否已过期（当前时间 >= 目标时间）
     *
     * @param targetDateTime 目标时间
     * @return true 表示已过期
     */
    public static boolean isExpired(LocalDateTime targetDateTime) {
        if (targetDateTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(targetDateTime) || LocalDateTime.now().isEqual(targetDateTime);
    }

    /**
     * 计算两个时间之间相差的天数
     *
     * @param start 开始时间
     * @param end 结束时间
     * @return 相差天数
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个时间之间相差的小时数
     *
     * @param start 开始时间
     * @param end 结束时间
     * @return 相差小时数
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个时间之间相差的分钟数
     *
     * @param start 开始时间
     * @param end 结束时间
     * @return 相差分钟数
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 判断目标时间是否在当前时间之后
     *
     * @param targetDateTime 目标时间
     * @return true 表示目标时间在当前时间之后
     */
    public static boolean isInFuture(LocalDateTime targetDateTime) {
        if (targetDateTime == null) {
            return false;
        }
        return targetDateTime.isAfter(LocalDateTime.now());
    }
}
