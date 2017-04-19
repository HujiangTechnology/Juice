package com.hujiang.juice.common.vo;

import java.io.Serializable;

/**
 * Created by xujia on 2016/6/14.
 */
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 2120467584344923858L;
    private Integer status = Integer.valueOf(0);
    private String message = null;
    private T data = null;

    public Result() {
    }

    public Result(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
