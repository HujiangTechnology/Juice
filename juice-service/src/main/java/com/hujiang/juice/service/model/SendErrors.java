package com.hujiang.juice.service.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.v1.Protos;


/**
 * Created by xujia on 17/3/4.
 */

@Slf4j
@Data
public class SendErrors {
    private Protos.FrameworkID frameworkId;
    private org.apache.mesos.v1.scheduler.Protos.Call call;

    public SendErrors(Protos.FrameworkID frameworkId,  org.apache.mesos.v1.scheduler.Protos.Call call) {
        this.frameworkId = frameworkId;
        this.call = call;
    }
}
