package com.hujiang.juice.service.utils.zookeeper;

import com.hujiang.juice.service.driver.SchedulerDriver;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hujiang.juice.service.config.JUICE.*;

/**
 * Created by xujia on 17/3/31.
 */
@Slf4j
public class LeaderSelectorClient extends LeaderSelectorListenerAdapter implements Closeable {
    private final LeaderSelector leaderSelector;
    private final SchedulerDriver schedulerDriver;
    private final AtomicInteger leaderCount = new AtomicInteger();

    public LeaderSelectorClient(SchedulerDriver schedulerDriver) {
        leaderSelector = new LeaderSelector(schedulerDriver.getCuratorUtils().getClient(), ZKLOCKS + HTTP_SEPERATOR + MESOS_FRAMEWORK_TAG, this);
        leaderSelector.autoRequeue();
        this.schedulerDriver = schedulerDriver;

    }

    public void start() throws IOException {
        leaderSelector.start();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {
        log.info("now the leader is " + (InetAddress.getLocalHost()).getHostName() + " , has been leader " + leaderCount.getAndIncrement() + " time(s) before.");
        try {
            schedulerDriver.run();
        } finally {
            log.error("relinquishing leadership.");
        }
    }

    @Override
    public void close() throws IOException {
        leaderSelector.close();
    }
}
