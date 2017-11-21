package com.hujiang.juice.common.utils.cache;

import com.hujiang.juice.common.exception.CacheException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;


/**
 * Created by xujia on 17/1/19.
 */

@Slf4j
@Data
public class RedisCacheUtils implements CacheUtils{

    private RedisUtil redisUtil;

    public RedisCacheUtils(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public long pushToQueue(@NotNull String queue, @NotNull String value){
        try {
            return lpush(queue, value);
        } catch (CacheException e) {
            String error = "cache operation error, push to " + queue + " failed, Task :" + value;
            log.error(error);
            throw e;
        }
    }

    public String popFromQueue(@NotNull String queue){
        try {
            return lpop(queue);
        } catch (CacheException e) {
            String error = "cache operation error, pop from " + queue + " failed";
            log.error(error);
            throw e;
        }
    }

    public long lengthOfQueue(@NotNull String queue){
        try {
            return length(queue);
        } catch (CacheException e) {
            String error = "cache operation error, get length" + queue + " failed";
            log.error(error);
            throw e;
        }
    }

    public boolean setExpired(String key, String value, int expired) {
        try {
            return setex(key, value, expired);
        } catch (CacheException e) {
            String error = "cache setex operation error, key : " + key + "， value ：" + value + ", expired : " + expired;
            log.error(error);
            throw e;
        }
    }

    public boolean existsKey(String key){
        try {
            return exists(key);
        } catch (CacheException e) {
            String error = "cache setex operation error, key : " + key;
            log.error(error);
            throw e;
        }
    }

    public long delete(String key) {
        try {
            return del(key);
        } catch (CacheException e) {
            String error = "cache delete operation error, key : " + key;
            log.error(error);
            throw e;
        }
    }

    public long rpushToQueue(@NotNull String queue, @NotNull String value) {
        try {
            return rPush(queue, value);
        } catch (CacheException e) {
            String error = "cache operation error, push to " + queue + " failed, Task :" + value;
            log.error(error);
            throw e;
        }
    }

    private boolean setex(String key, String value, int expireds) {
        return redisUtil.setex(key, value, expireds);
    }

    private boolean exists(String key) {
        return redisUtil.exists(key);
    }

    private long del(String key){
        return redisUtil.del(key);
    }

    private Long lpush(String lname, String value) {
        return redisUtil.lPush(lname, value);
    }
    private Long rPush(String lname, String value) {
        return redisUtil.rPush(lname, value);
    }

    private String lpop(String lname) {
        return redisUtil.rPop(lname);
    }
    private long length(String lname) {
        return redisUtil.llen(lname);
    }
}
