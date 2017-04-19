package com.hujiang.juice.service.utils.protocol;

import com.google.protobuf.GeneratedMessage;
import com.hujiang.juice.common.utils.rest.Restty;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.v1.scheduler.Protos;

import static org.apache.mesos.v1.executor.Protos.Event;

/**
 * Created by xujia on 16/11/28.
 */
@Slf4j
public class Protobuf extends Protocol {

    @Override
    public byte[] getSendBytes(GeneratedMessage call) {
        return call.toByteArray();
    }

    @Override
    public String mediaType(){
        return Restty.protoBody();
    }

    @Override
    public Object getEvent(byte[] buffer, Class<?> classz) throws Exception {
        if(classz.equals(Protos.Event.class)) {
            return Protos.Event.parseFrom(buffer);
        }

        return Event.parseFrom(buffer);
    }
}
