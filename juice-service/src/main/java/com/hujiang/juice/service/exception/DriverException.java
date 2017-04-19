package com.hujiang.juice.service.exception;

/**
 * Created by xujia on 16/12/19.
 */
public class DriverException extends RuntimeException{
    public DriverException(String message) {
        super(message);
    }

    public DriverException(Throwable cause) {
        super(cause);
    }

    public DriverException(String message, Throwable cause) {
        super(message, cause);
    }
}
