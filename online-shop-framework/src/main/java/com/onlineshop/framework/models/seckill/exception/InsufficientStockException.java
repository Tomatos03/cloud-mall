package com.onlineshop.framework.models.seckill.exception;

/**
 * 库存不足异常
 */
public class InsufficientStockException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private String code = "INSUFFICIENT_STOCK";
    
    public InsufficientStockException() {
        super("库存不足");
    }
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String message, String code) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}