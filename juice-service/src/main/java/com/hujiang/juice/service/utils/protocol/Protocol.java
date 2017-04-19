package com.hujiang.juice.service.utils.protocol;

import com.google.protobuf.GeneratedMessage;

/**
 * Created by xujia on 16/11/28.
 */
public abstract class Protocol {

    public abstract byte[] getSendBytes(GeneratedMessage call);
    public abstract String mediaType();

    public abstract Object getEvent(byte[] buffer, Class<?> classz) throws Exception;
}
