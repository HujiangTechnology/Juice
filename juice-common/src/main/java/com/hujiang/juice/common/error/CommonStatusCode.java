package com.hujiang.juice.common.error;


import com.hujiang.juice.common.exception.InternalServiceException;
import com.hujiang.juice.common.exception.UnauthorizedException;

/**
 * Created by xujia on 16/12/29.
 */
public enum CommonStatusCode {
    SERVICE_OK(0, "success"),
    SERVICE_INTERNAL_ERROR(0x81300000,"internal service error"),
    NO_PERMISSION(0x81300050, "no permission, auth failed"),
    CONSUMED_OUT_CODE(0x81300051, "consumed out error"),
    EXPIRED_CODE(0x81300052, "user siqn expired error"),
    METHOD_NOT_NULL(0x81300053, "method not null"),
    REQUEST_PARAMS_IS_NULL(0x81300054, "request param is null"),
    REMOTE_SERVER_RESPONSE_ERROR(0x81300055, "third part remote service not response"),
    DATA_ACCESS_ERROR(0x81300056, "db access error"),
    NO_RESPONSE_VALUE(0x81300057, "no response"),
    RETURN_VALUE_CHECKED_ERROR(0x81300058, "return value not validate"),
    QUERY_RECORD_EMPTY(0x81300060, "query error, no record equal in db"),
    INVALID_ID_ERRORR(0x81300062, "invalid id error"),
    RMQ_PUSH_ERROR(0x81300063, "rabbit mq push value error"),
    NO_DATA_ROWS_EXCUTE(0x81300064, "no record in db"),
    STAT_NOT_FOUND(0x81300065, "stat not found"),
    HEATH_CHECK_ERROR(0x81300066,"health check error"),
    INTERFACE_NOT_IMPLEMENT_ERROR(0x81300067,"interface not implement error"),
    REQUEST_FOR_REMOTE_SERVICE_ERROR(0x81300068,"request for remote service error"),
    OBJECT_INIT_ERROR(0x81300069, "object init error"),
    REQUEST_PARAMS_INVALID_ERROR(0x81300070, "request params invalid error"),
    CACHE_OPERATING_ERROR(0x81300071,"cache operating error"),
    CONFIGURATION_EXCEPTION_ERROR(0x81300072,"configuration exception error"),
    REST_EXCEPTION_ERROR(0x81300073,"rest exception error"),
    DATABASE_EXCEPTION_ERROR(0x81300074,"database exception error"),
    REDIS_OPERATION_ERROR(0x81300075, "redis operation error"),
    REDIS_CONNECTION_ERROR(0x81300076,"redis connection error"),
    REDIS_CONNECTION_RESOURCE_NOT_NULL(0x81300077,"redis connection resource not null error");


    public Integer status;
    public String message;

    CommonStatusCode(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UnauthorizedException Error2UnauthorizedException(String message) {

        return new UnauthorizedException(this.status, this.message + ", " + message);
    }

    public UnauthorizedException Error2UnauthorizedException() {

        return new UnauthorizedException(this.status, this.message);
    }

    public InternalServiceException Error2InternalServiceException() {

        return new InternalServiceException(this.status, this.message);
    }
}
