package com.hujiang.juice.common.error;

/**
 * Created by xujia on 16/9/27.
 */

public enum ErrorCode {
    ENCRYPTION_ERROR(-2127551742, "encryption error"),
    SYSTEM_ENV_NOT_VALID(-2127551741, "system env not valid error"),
    OBJECT_NOT_EQUAL_ERROR(-2127552256, "object not equal error"),
    OBJECT_NOT_NULL_ERROR(-2127552257, "object not null error"),
    DATA_ACCESS_ERROR(-2127552258, "data access error"),
    NO_DATA_ERROR(-2127552259, "no data error"),
    BLANK_VALUE(-2127552260, "string value not blank error"),
    VALUE_LENGTH_NOT_EQUAL(-2127552261, "value length not equal error"),
    PARSE_OBJECT_ERROR(-2127552262, "parse string to object error"),

    DB_INIT_ERROR(-2127552005, "init db error"),
    REDIS_INIT_ERROR(-2127552006, "init db error"),
    OBJECT_INIT_ERROR(-2127552007, "init object error"),
    HEALTH_CHECK_ERROR(-2127552008, "health check error"),
    REDIS_KEY_EXPIRED(-2127552009, "redis key expired"),
    REDIS_OPERATION_ERROR(-2127552010, "redis operation error"),
    DB_OPERATION_ERROR(-2127552011, "db operation error"),
    OBJECT_REFLECT_ERROR(-2127552012, "object reflect error"),

    AUTH_TOKEN_NOT_NULL_ERROR(-2127552013, "auth token not null error"),
    AUTH_TOKEN_INVALID_ERROR(-2127552014, "auth token invalid error"),
    AUTH_TOKEN_EXPIRED_ERROR(-2127552015, "auth token expired error"),
    AUTH_REMOTE_REQUEST_ERROR(-2127552016, "request remote auth service error"),
    NOT_SUPPORTED_TYPE_ERROR(-2127552017, "not supported type error"),
    HTTP_REQUEST_ERROR(-2127552018, "http request error"),
    DATA_FORMAT_ERROR(-2127552019, "data format error");

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}