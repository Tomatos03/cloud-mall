package com.onlineshop.framework.models.seckill.exception;

/**
 * 秒杀业务异常基类
 */
public class SeckillException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private Integer code;
    
    private String message;

    public SeckillException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
    }

    public SeckillException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = 500;
    }

    public SeckillException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
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