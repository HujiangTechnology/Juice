package com.hujiang.juice.common.exception;


import com.hujiang.juice.common.error.CommonStatusCode;

import static com.hujiang.juice.common.error.CommonStatusCode.HEATH_CHECK_ERROR;

/**
 * Created by zhouxiang on 2016/9/6.
 */

public class HealthcheckException extends CommonException {
    private static final long serialVersionUID = 1L;

    public HealthcheckException(int code, String message){
        super(code, message);
    }

    public HealthcheckException(String message) {
        super(HEATH_CHECK_ERROR.getStatus(), message);
    }

    public HealthcheckException() {
        this(HEATH_CHECK_ERROR);
    }

    public HealthcheckException(CommonStatusCode statusCode) {
        this(statusCode.status, statusCode.message);
    }
}
