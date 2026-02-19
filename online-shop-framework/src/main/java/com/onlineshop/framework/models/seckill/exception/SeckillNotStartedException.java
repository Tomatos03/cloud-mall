package com.onlineshop.framework.models.seckill.exception;

/**
 * 秒杀未开始异常
 */
public class SeckillNotStartedException extends RuntimeException {
    
    public SeckillNotStartedException() {
        super("秒杀活动未开始");
    }

    public SeckillNotStartedException(String message) {
        super(message);
    }

    public SeckillNotStartedException(String message, Throwable cause) {
        super(message, cause);
    }
}