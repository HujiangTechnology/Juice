package com.hujiang.juice.common.exception;


import com.hujiang.juice.common.error.CommonStatusCode;

import static com.hujiang.juice.common.error.CommonStatusCode.DATABASE_EXCEPTION_ERROR;

/**
 * Created by xujia on 17/2/7.
 */

public class DataBaseException extends InternalServiceException {

    public DataBaseException(int code, String message){
        super(code, message);
    }

    public DataBaseException(String message) {
        super(DATABASE_EXCEPTION_ERROR.getStatus(), message);
    }

    public DataBaseException() {
        this(DATABASE_EXCEPTION_ERROR);
    }

    public DataBaseException(CommonStatusCode statusCode) {
        this(statusCode.status, statusCode.message);
    }
}
