package com.hujiang.juice.common.exception;


import com.hujiang.juice.common.error.CommonStatusCode;

import static com.hujiang.juice.common.error.CommonStatusCode.REST_EXCEPTION_ERROR;

/**
 * Created by xujia on 16/12/5.
 */

public class RestException extends CommonException {

    public RestException(int code, String message){
        super(code, message);
    }

    public RestException(String message) {
        super(REST_EXCEPTION_ERROR.getStatus(), message);
    }

    public RestException() {
        this(REST_EXCEPTION_ERROR);
    }

    public RestException(CommonStatusCode statusCode) {
        this(statusCode.status, statusCode.message);
    }
}
