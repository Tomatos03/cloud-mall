package com.onlineshop.framework.models.seckill.exception;

/**
 * 秒杀活动已结束异常
 */
public class SeckillEndedException extends RuntimeException {
    
    public SeckillEndedException() {
        super("秒杀活动已结束");
    }

    public SeckillEndedException(String message) {
        super(message);
    }

    public SeckillEndedException(String message, Throwable cause) {
        super(message, cause);
    }
}