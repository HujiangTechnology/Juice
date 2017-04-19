package com.hujiang.juice.common.exception;


import com.hujiang.juice.common.error.CommonStatusCode;

/**
 * Created by xujia on 16/2/26.
 */

public class InternalServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public InternalServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public InternalServiceException() {
        this(CommonStatusCode.SERVICE_INTERNAL_ERROR);
    }

    public InternalServiceException(CommonStatusCode statusCode) {
        this(statusCode.status, statusCode.message);
    }

}
