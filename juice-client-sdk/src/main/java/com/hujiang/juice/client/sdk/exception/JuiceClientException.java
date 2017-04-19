package com.hujiang.juice.client.sdk.exception;


import com.hujiang.juice.common.exception.CommonException;

/**
 * Created by xujia on 17/2/6.
 */

public class JuiceClientException extends CommonException {

    public JuiceClientException(int code, String message){
        super(code, message);
    }

}
