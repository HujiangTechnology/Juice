package com.hujiang.juice.common.exception;


import com.hujiang.juice.common.error.CommonStatusCode;
import static com.hujiang.juice.common.error.CommonStatusCode.CACHE_OPERATING_ERROR;

/**
 * Created by xujia on 17/2/6.
 */

public class CacheException extends InternalServiceException {

    public CacheException(int code, String message){
        super(code, message);
    }

    public CacheException(String message) {
        super(CACHE_OPERATING_ERROR.getStatus(), message);
    }

    public CacheException() {
        this(CACHE_OPERATING_ERROR);
    }

    public CacheException(CommonStatusCode statusCode) {
        this(statusCode.status, statusCode.message);
    }

}
