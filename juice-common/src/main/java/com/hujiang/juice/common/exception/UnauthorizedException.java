package com.hujiang.juice.common.exception;


import com.hujiang.juice.common.error.CommonStatusCode;
import static com.hujiang.juice.common.error.CommonStatusCode.NO_PERMISSION;

/**
 * Created by xujia on 16/2/26.
 */
public class UnauthorizedException extends CommonException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(int code, String message) {
        super(code, message);
    }

    public UnauthorizedException(String message){
        super(NO_PERMISSION.getStatus(), message);
    }

    public UnauthorizedException() {
        this(NO_PERMISSION);
    }

    public UnauthorizedException(CommonStatusCode statusCode) {
        this(statusCode.status, statusCode.message);
    }
}
