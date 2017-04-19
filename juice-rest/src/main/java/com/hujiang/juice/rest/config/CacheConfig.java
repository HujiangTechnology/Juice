package com.hujiang.juice.rest.config;

import com.hujiang.juice.common.utils.cache.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Created by xujia on 17/2/12.
 */
public class CacheConfig {

    @Value("${spring.redis.host}")
    private String redisIp;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RedisUtil getRedisUtil() {
        return new RedisUtil(redisIp, port, password);
    }

}
