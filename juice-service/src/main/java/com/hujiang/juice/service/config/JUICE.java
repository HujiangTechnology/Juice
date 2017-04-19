package com.hujiang.juice.service.config;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.hujiang.juice.common.utils.cache.CacheUtils;
import com.hujiang.juice.common.utils.cache.RedisCacheUtils;
import com.hujiang.juice.common.utils.db.DaoUtils;
import com.hujiang.juice.common.utils.db.JuiceDao;
import com.hujiang.juice.service.factory.JuiceFactory;
import com.hujiang.juice.service.factory.DataSourceFactory;
import com.hujiang.juice.service.factory.RedisFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by xujia on 16/9/27.
 */
@Slf4j
public class JUICE {

    public static final Gson gson = new Gson();
    //  retry queue every offer
    public static final int CACHE_TRIES = 5;

    //  stream-id
    public static final String STREAM_ID = "Mesos-Stream-Id";
    //  framework failover timeout
    public static final double FRAMEWORK_FAILOVER_TIMEOUT = 30 * 24 * 60 * 60;

    public static final String HTTP_SEPERATOR = "/";

    public static final String ZKLOCKS = "/mesos/zklocks";


    //  for curator use
    private static final String CONFIG_ZOOKEEPER_DISTRIBUTE_LOCK_HA = "zookeeper.distribute.lock.ha";
    private static final boolean DEFAULT_ZOOKEEPER_DISTRIBUTE_LOCK_HA = true;
    public static final boolean ZOOKEEPER_DISTRIBUTE_LOCK_HA = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_ZOOKEEPER_DISTRIBUTE_LOCK_HA, DEFAULT_ZOOKEEPER_DISTRIBUTE_LOCK_HA);

    //  for curator use
    private static final String CONFIG_MESOS_ROOT_PATH = "mesos.root.path";
    private static final String DEFAULT_MESOS_ROOT_PATH = "/mesos";
    public static final String MESOS_ROOT_PATH = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_MESOS_ROOT_PATH, DEFAULT_MESOS_ROOT_PATH);

    private static final String CONFIG_DEFAULT_PATH_NAME = "mesos.default.path";
    public static final String DEFAULT_PATH_NAME = "json.info";
    public static final String PATH_NAME = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_DEFAULT_PATH_NAME, DEFAULT_PATH_NAME);

    //  mesos scheduler name
    private static final String CONFIG_MESOS_SCHEDULER_NAME = "mesos.scheduler.name";
    private static final String DEFAULT_MESOS_SCHEDULER_NAME = "juice-service-" + JuiceFactory.INSTANCE.getEnv();
    public static final String MESOS_SCHEDULER_NAME = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_MESOS_SCHEDULER_NAME, DEFAULT_MESOS_SCHEDULER_NAME);

//  only support zk start mode, so do not use  MESOS_SCHEDULER_END_POINT to start service
//    //  mesos framework end point by uri (only start with uri mode should be configured）
//    private static final String CONFIG_MESOS_SCHEDULER_END_POINT = "mesos.framework.uri";
//    public static final String MESOS_SCHEDULER_END_POINT = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_MESOS_SCHEDULER_END_POINT, "");

    //  discover mesos framework end point by zk (general use)
    private static final String CONFIG_MESOS_SCHEDULER_END_POINT_ZK = "mesos.framework.zk";
    public static final String MESOS_SCHEDULER_END_POINT_ZK = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_MESOS_SCHEDULER_END_POINT_ZK, "");

    //  mesos framework role
    private static final String CONFIG_MESOS_FRAMEWORK_ROLE = "mesos.framework.role";
    public static final String MESOS_FRAMEWORK_ROLE = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_MESOS_FRAMEWORK_ROLE, "*");

    //  resources can only use threshold
    private static final String CONFIG_RESOURCES_USE_THRESHOLD = "resources.use.threshold";
    private static final double DEFAULT_RESOURCES_USE_THRESHOLD = 0.8;
    public static final double RESOURCES_USE_THRESHOLD = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_RESOURCES_USE_THRESHOLD, DEFAULT_RESOURCES_USE_THRESHOLD);

    //  mesos framework attrs
    private static final String CONFIG_MESOS_FRAMEWORK_ATTR_FILTER = "mesos.framework.attr";
    private static final String MESOS_FRAMEWORK_ATTR_FILTER = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_MESOS_FRAMEWORK_ATTR_FILTER);
    public static final Map<String, Integer> MESOS_FRAMEWORK_ATTR_FILTER_MAP = getAttrFilterMap(",", "\\|");
    public static final BitSet MESOS_FRAMEWORK_ATTR_FILTER_DEFAULT_BITSET = getdefaultBitSet(",");

    //  offer allocation send pool
    private static final String CONFIG_OFFER_SEND_POOL = "send.pool.size";
    private static final int DEFAULT_OFFER_SEND_POOL = 20;
    public static final int OFFER_SEND_POOL = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_OFFER_SEND_POOL, DEFAULT_OFFER_SEND_POOL);

    //  offer auxiliary handler pool
    private static final String CONFIG_AUXILIARY_POOL = "auxiliary.pool.size";
    private static final int DEFAULT_AUXILIARY_POOL = 20;
    public static final int AUXILIARY_POOL = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_AUXILIARY_POOL, DEFAULT_AUXILIARY_POOL);

    //  task max reserved times
    private static final String CONFIG_MAX_RESERVED = "max.reserved";
    private static final int DEFAULT_MAX_RESERVED = 1024;
    public static final int MAX_RESERVED = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_MAX_RESERVED,  DEFAULT_MAX_RESERVED);

    //  framework tag
    private static final String CONFIG_MESOS_FRAMEWORK_TAG = "mesos.framework.tag";
    private static final String DEFAULT_MESOS_FRAMEWORK_TAG = "mesos.framework.tag." + JuiceFactory.INSTANCE.getEnv();
    public static final String MESOS_FRAMEWORK_TAG = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_MESOS_FRAMEWORK_TAG, DEFAULT_MESOS_FRAMEWORK_TAG);

    //  juice task queue name
    private static final String CONFIG_TASK_QUEUE = "juice.task.queue";
    private static final String DEFAULT_TASK_QUEUE = "juice.task.queue." + JuiceFactory.INSTANCE.getEnv();
    public static final String TASK_QUEUE = RedisFactory.INSTANCE.getConfigure().getValue(CONFIG_TASK_QUEUE, DEFAULT_TASK_QUEUE);

    //  juice task retry queue name
    private static final String CONFIG_TASK_RETRY_QUEUE = "juice.task.retry.queue";
    private static final String DEFAULT_TASK_RETRY_QUEUE = "juice.task.retry.queue." + JuiceFactory.INSTANCE.getEnv();
    public static final String TASK_RETRY_QUEUE = RedisFactory.INSTANCE.getConfigure().getValue(CONFIG_TASK_RETRY_QUEUE, DEFAULT_TASK_RETRY_QUEUE);

    //  juice task result queue
    private static final String CONFIG_RESULT_QUEUE = "juice.task.result.queue";
    private static final String DEFAULT_RESULT_QUEUE = "juice.task.result.queue." + JuiceFactory.INSTANCE.getEnv();
    public static final String TASK_RESULT_QUEUE = RedisFactory.INSTANCE.getConfigure().getValue(CONFIG_RESULT_QUEUE, DEFAULT_RESULT_QUEUE);

    //  juice management queue
    private static final String CONFIG_MANAGEMENT_QUEUE = "juice.management.queue";
    private static final String DEFAULT_MANAGEMENT_QUEUE = "juice.management.queue." + JuiceFactory.INSTANCE.getEnv();
    public static String MANAGEMENT_QUEUE = RedisFactory.INSTANCE.getConfigure().getValue(CONFIG_MANAGEMENT_QUEUE, DEFAULT_MANAGEMENT_QUEUE);

    //  juice task expire seconds in queue
    private static final String CONFIG_TASK_RETRY_EXPIRE_TIME = "task.retry.expire.time";
    private static final int DEFAULT_TASK_RETRY_EXPIRE_TIME = 86400;
    public static final long TASK_RETRY_EXPIRE_TIME = JuiceFactory.INSTANCE.getConfigure().getValue(CONFIG_TASK_RETRY_EXPIRE_TIME, DEFAULT_TASK_RETRY_EXPIRE_TIME);

    //  cache
    public static final CacheUtils cacheUtils = new RedisCacheUtils(RedisFactory.Redis.INSTANCE.redisUtil());
    //  dao
    public static final DaoUtils daoUtils = new DaoUtils(new JuiceDao(DataSourceFactory.JOOQ.INSTANCE.getContext()));

    private static Map<String, Integer> getAttrFilterMap(@NotNull String spilt, @NotNull String cut) {

        AtomicInteger i = new AtomicInteger(0);

        Map<String, Integer> valueMap = Maps.newHashMap();
        Arrays.stream(MESOS_FRAMEWORK_ATTR_FILTER.split(spilt)).forEach(
                v -> {
                    final int step = i.getAndIncrement();
                    Arrays.stream(v.split(cut)).forEach(
                            v1 -> {
                                valueMap.put(v1, step);
                            }
                    );
                }
        );

        return valueMap;
    }

    private static BitSet getdefaultBitSet(@NotNull String spilt) {
        BitSet bs = new BitSet();
        if(StringUtils.isNotBlank(MESOS_FRAMEWORK_ATTR_FILTER)) {
            int length = MESOS_FRAMEWORK_ATTR_FILTER.split(spilt).length;
            for (int i = 0; i < length; i++) {
                bs.set(i);
            }
        }
        return bs;
    }
}
