package com.hujiang.juice.service;

import com.hujiang.juice.service.driver.SchedulerDriver;
import com.hujiang.juice.service.utils.LogInitUtil;
import com.hujiang.juice.service.utils.zookeeper.LeaderSelectorClient;
import lombok.extern.slf4j.Slf4j;

import static com.hujiang.juice.service.config.JUICE.*;

/**
 * Created by xujia on 16/8/9.
 */

@Slf4j
public class Startup {

    public static void main(String[] args) {
        try {
            LogInitUtil.initLog();
            log.info("init juice service");
            log.info("taskQueue : " + TASK_QUEUE);
            log.info("taskRetryQueue : " + TASK_RETRY_QUEUE);
            log.info("resultQueue : " + TASK_RESULT_QUEUE);
            log.info("managementQueue : " + MANAGEMENT_QUEUE);
            SchedulerDriver schedulerDriver = new SchedulerDriver();
            if(ZOOKEEPER_DISTRIBUTE_LOCK_HA) {
                LeaderSelectorClient leaderSelectorClient = null;
                try {
                    leaderSelectorClient = new LeaderSelectorClient(schedulerDriver);
                    leaderSelectorClient.start();
                } catch (Exception e) {
                    if(null != leaderSelectorClient) {
                        leaderSelectorClient.close();
                    }
                    throw e;
                }
            } else {
                schedulerDriver.run();
            }
        } catch (Exception e) {
            log.error("start juice service error");
            System.exit(-1);
        }
    }
}
