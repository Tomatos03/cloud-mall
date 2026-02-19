package com.onlineshop.framework.models.seckill.exception;

/**
 * 秒杀限流异常
 */
public class SeckillRateLimitException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private String code;
    private String message;

    public SeckillRateLimitException(String message) {
        super(message);
        this.message = message;
        this.code = "SECKILL_RATE_LIMIT";
    }

    public SeckillRateLimitException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}