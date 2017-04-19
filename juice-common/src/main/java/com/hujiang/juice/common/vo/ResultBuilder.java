package com.hujiang.juice.common.vo;


/**
 * Created by xujia on 2016/6/14.
 */
public class ResultBuilder<T> {
    private Integer status = Integer.valueOf(0);
    private String message;
    private T data;

    public ResultBuilder() {
    }

    public ResultBuilder<T> status(Integer status) {
        this.status = status;
        return this;
    }

    public ResultBuilder<T> message(String message) {
        this.message = message;
        return this;
    }

    public ResultBuilder<T> data(T data) {
        this.data = data;
        return this;
    }

    public Result<T> build() {
        return new Result(this.status, this.message, this.data);
    }
}

