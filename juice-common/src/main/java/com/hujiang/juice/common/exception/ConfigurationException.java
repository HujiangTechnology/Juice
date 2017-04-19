package com.hujiang.juice.common.exception;


import com.hujiang.juice.common.error.CommonStatusCode;
import static com.hujiang.juice.common.error.CommonStatusCode.CONFIGURATION_EXCEPTION_ERROR;

/**
 * Created by xujia on 17/2/7.
 */

public class ConfigurationException extends InternalServiceException {

    public ConfigurationException(int code, String message){
        super(code, message);
    }

    public ConfigurationException(String message) {
        super(CONFIGURATION_EXCEPTION_ERROR.getStatus(), message);
    }

    public ConfigurationException() {
        this(CONFIGURATION_EXCEPTION_ERROR);
    }

    public ConfigurationException(CommonStatusCode statusCode) {
        this(statusCode.status, statusCode.message);
    }
}
