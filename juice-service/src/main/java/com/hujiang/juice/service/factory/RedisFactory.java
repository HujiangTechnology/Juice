package com.hujiang.juice.service.factory;


import com.hujiang.juice.common.config.Configure;
import com.hujiang.juice.common.error.ErrorCode;
import com.hujiang.juice.common.exception.CacheException;
import com.hujiang.juice.common.exception.ConfigurationException;
import com.hujiang.juice.common.utils.cache.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;



/**
 * Created by xujia on 16/12/5.
 */

@Slf4j
public enum RedisFactory {

    INSTANCE;

    private Configure configure;

    RedisFactory() {
        try {
            String env = System.getProperty("system.environment");
            if (StringUtils.isBlank(env)) {
                throw new ConfigurationException(ErrorCode.SYSTEM_ENV_NOT_VALID.getCode(), "can't get env, service stop!");
            }
            configure = new Configure("application-" + env);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String getValue(String name) {
        return configure.getValue(name);
    }

    public Configure getConfigure() {
        return configure;
    }

    //  redis connection config
    public static final String CONFIG_REDIS_HOST = "redis.host";
    public static final String CONFIG_REDIS_PORT = "redis.port";
    public static final String CONFIG_REDIS_PASSWD = "redis.password";

    public enum Redis {
        INSTANCE(RedisFactory.INSTANCE);

        private RedisUtil redisUtil;

        Redis(RedisFactory redisFactory) {
            redisUtil = new RedisUtil(redisFactory.getValue(CONFIG_REDIS_HOST),
                    Integer.parseInt(redisFactory.getValue(CONFIG_REDIS_PORT)),
                    StringUtils.isNotBlank(redisFactory.getValue(CONFIG_REDIS_PASSWD)) ? redisFactory.getValue(CONFIG_REDIS_PASSWD) : null);
        }

        public RedisUtil redisUtil() {
            if (null == redisUtil) {
                throw new CacheException(ErrorCode.REDIS_INIT_ERROR.getCode(), "init redisUtil failed");
            }
            return redisUtil;
        }
    }
}

