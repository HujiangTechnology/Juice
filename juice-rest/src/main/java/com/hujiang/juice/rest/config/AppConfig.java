package com.hujiang.juice.rest.config;

import com.hujiang.juice.common.utils.cache.CacheUtils;
import com.hujiang.juice.common.utils.cache.RedisCacheUtils;
import com.hujiang.juice.common.utils.cache.RedisUtil;
import com.hujiang.juice.common.utils.db.DaoUtils;
import com.hujiang.juice.common.utils.db.JuiceDao;
import com.hujiang.juice.rest.utils.SubscriberUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by xujia on 17/2/12.
 */


@Slf4j
@Configuration
@ComponentScan("com.hujiang")
@SpringBootApplication
@EnableAutoConfiguration()
@EnableScheduling
@Import({CacheConfig.class})
public class AppConfig {

    @Value("${spring.profiles.active}")
    private String profileActive;

    @Value("${juice.task.queue:juice.task.queue.}")
    private String taskQueue;

    @Value("${juice.task.result.queue:juice.task.result.queue.}")
    private String taskResultQueue;

    @Value("${juice.management.queue:juice.management.queue.}")
    private String managementQueue;

    @Value("${juice.task.expired.of.seconds:86400}")
    private int expiredSeconds;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private RedisUtil redisUtil;

    @Bean
    public CacheUtils getCacheUtils() {
        return new RedisCacheUtils(redisUtil);
    }

    @Bean
    public SubscriberUtils getSubscriberUtils() {
        return new SubscriberUtils();
    }

    @Bean
    public DaoUtils getDaoService() {
        return new DaoUtils(new JuiceDao(dslContext));
    }

    @Bean
    public CachesBizConfig getCachesBizConfig() {
        log.info("profileActive : " + profileActive);
        return new CachesBizConfig(taskQueue + profileActive, null, taskResultQueue + profileActive, managementQueue + profileActive,  expiredSeconds);
    }
}
