package com.hujiang.juice.rest.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by xujia on 17/2/12.
 */

@Data
@Slf4j
public class CachesBizConfig {
    private String taskQueue;
    private String taskRetryQueue;
    private String resultQueue;
    private String managementQueue;
    private int expiredSeconds;



    public CachesBizConfig(String taskQueue, String taskRetryQueue, String resultQueue, String managementQueue, int expiredSeconds) {
        this.taskQueue = taskQueue;
        this.taskRetryQueue = taskRetryQueue;
        this.resultQueue = resultQueue;
        this.managementQueue = managementQueue;
        this.expiredSeconds = expiredSeconds;

        log.info("taskQueue : " + taskQueue);
        log.info("taskRetryQueue : " + taskRetryQueue);
        log.info("resultQueue : " + resultQueue);
        log.info("managementQueue : " + managementQueue);
        log.info("expiredOfSeconds : " + expiredSeconds);
    }
}
