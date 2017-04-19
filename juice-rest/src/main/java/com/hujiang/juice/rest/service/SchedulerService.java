package com.hujiang.juice.rest.service;

import com.hujiang.juice.rest.utils.SubscriberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by xujia on 17/2/12.
 */

@Service
public class SchedulerService {

    @Autowired
    private SubscriberUtils subscriberUtils;

    @Scheduled(fixedDelay = 1000)
    public void handler() {
        subscriberUtils.handleResult();
    }
}
