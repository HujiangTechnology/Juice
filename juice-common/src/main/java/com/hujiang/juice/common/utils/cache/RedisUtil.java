package com.hujiang.juice.common.utils.cache;


import com.hujiang.juice.common.error.CommonStatusCode;
import com.hujiang.juice.common.exception.CacheException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by zhouxiang on 2016/8/1.
 */
@Slf4j
public class RedisUtil {
    private JedisPool pool = null;

    public Jedis getResource() {
        return pool.getResource();
    }

    /**
     * <p>传入ip和端口号构建redis 连接池</p>
     * @param ip ip
     * @param prot 端口
     */
    public RedisUtil(String ip, int prot) {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(500);
            config.setMaxIdle(5);
            config.setMaxWaitMillis(3000);
            config.setTestOnBorrow(true);
            pool = new JedisPool(config, ip, prot, 5000);
        }
    }


    /**
     * <p>传入ip和端口号构建redis 连接池</p>
     * @param ip ip
     * @param prot 端口
     */
    public RedisUtil(String ip, int prot, String password) {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(500);
            config.setMaxIdle(5);
            config.setMaxWaitMillis(3000);
            config.setTestOnBorrow(true);
            if(StringUtils.isNotBlank(password))
                pool = new JedisPool(config, ip, prot, 5000, password);
            else
                pool = new JedisPool(config, ip, prot, 5000);
        }
    }

    /**
     * <p>通过配置对象 ip 端口 构建连接池</p>
     * @param config 配置对象
     * @param ip ip
     * @param prot 端口
     */
    public RedisUtil(JedisPoolConfig config , String ip, int prot){
        if (pool == null) {
            pool = new JedisPool(config,ip,prot,5000);
        }
    }

    /**
     * <p>通过配置对象 ip 端口 超时时间 构建连接池</p>
     * @param config 配置对象
     * @param ip ip
     * @param prot 端口
     * @param timeout 超时时间
     */
    public RedisUtil(JedisPoolConfig config , String ip, int prot , int timeout){
        if (pool == null) {
            pool = new JedisPool(config,ip,prot,timeout);
        }
    }

    /**
     * <p>通过连接池对象 构建一个连接池</p>
     * @param pool 连接池对象
     */
    public RedisUtil(JedisPool pool){
        if (this.pool == null) {
            this.pool = pool;
        }
    }

    /**
     * <p>通过key获取储存在redis中的value</p>
     * <p>并释放连接</p>
     * @param key
     * @return 成功返回value 失败返回null
     */
    public String get(String key){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.get(key);
        } catch (Exception e) {
            log.error("redis set error, key : " + key);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * <p>向redis存入key和value,并释放连接资源</p>
     * <p>如果key已经存在 则覆盖</p>
     * @param key
     * @param value
     * @return 成功 返回OK 失败返回 0
     */
    public String set(String key,String value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.set(key, value);
        } catch (Exception e) {
            log.error("redis set error, key : " + key + " value : " + value);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }


    /**
     * <p>删除指定的key,也可以传入一个包含key的数组</p>
     * @param keys 一个key  也可以使 string 数组
     * @return 返回删除成功的个数
     */
    public Long del(String...keys){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.del(keys);
        } catch (Exception e) {
            log.error("redis set error, keys : " + Arrays.stream(keys).collect(Collectors.joining(",")));
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * <p>通过key向指定的value值追加值</p>
     * @param key
     * @param str
     * @return 成功返回 添加后value的长度 失败 返回 添加的 value 的长度  异常返回0L
     */
    public Long append(String key ,String str){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.append(key, str);
        } catch (Exception e) {
            log.error("redis set error, key : " + key + " value : " + str);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * <p>判断key是否存在</p>
     * @param key
     * @return true OR false
     */
    public Boolean exists(String key){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.exists(key);
        } catch (Exception e) {
            log.error("redis set error, key : " + key);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    public Boolean setex(String key, String value, int expireTime) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return null != jedis.setex(key, expireTime, value);
        } catch (Exception e) {
            log.error("redis set error, key : " + key + " value : " + value + " expireTime : " + expireTime);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * <p>队列左插入一个value</p>
     * @param  listName 队列名
     *         value 元素值
     * @return 插入后队列的元素个数
     */
    public Long lPush(String listName, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.lpush(listName, value);
        } catch (Exception e) {
            log.error("redis lPush error, ListName : " + listName + " value : " + value);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * <p>队列右弹出一个元素</p>
     * @param  listName 队列名
     *
     * @return 元素值
     */
    public String rPop(String listName) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.rpop(listName);
        } catch (Exception e) {
            log.error("redis rPop error, ListName : " + listName);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * <p>队列元素个数</p>
     * @param  listName 队列名
     *
     * @return 元素个数
     */
    public Long llen(String listName) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.llen(listName);
        } catch (Exception e) {
            log.error("redis llen error, ListName : " + listName);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * <p>在指定的Set中插入一个元素</p>
     * @param  setName set名
     *         value
     * @return Set中的元素个数
     */
    public Long sadd(String setName, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.sadd(setName, value);
        } catch (Exception e) {
            log.error("redis sadd error, setName : " + setName + " value : " + value);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * <p>在指定的Set中删除一个元素</p>
     * @param  setName set名
     *         value
     * @return Set中的元素个数
     */
    public Long srem(String setName, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.srem(setName, value);
        } catch (Exception e) {
            log.error("redis srem error, setName : " + setName + " value : " + value);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * <p>返回Set中的所有元素</p>
     * @param  setName set名
     *         value
     * @return 元素值
     */
    public Set<String> smembers(String setName) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.smembers(setName);
        } catch (Exception e) {
            log.error("redis smembers error, setName : " + setName);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    public void subscribe(JedisPubSub subscriber, String channel) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            jedis.subscribe(subscriber, channel);
        } catch (Exception e) {
            log.error("redis subscribe error, channel : " + channel);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    public Long publish(String channel, String message) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.publish(channel, message);
        } catch (Exception e) {
            log.error("redis publish error, channel : " + channel + ", message : " + message);
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }


    public boolean unLock(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }
            return jedis.del(key) > 0;
        } catch (Exception e) {
            log.error("set redis unlock error!");
            throw throwCacheException(e);
        } finally {
            returnResource(jedis);
        }
    }

    public boolean lock(String key, int seconds, String value) {
        Jedis jedis = null;
        Transaction tx;
        try {
            jedis = pool.getResource();
            if(jedis == null) {
                throw new CacheException(CommonStatusCode.REDIS_CONNECTION_RESOURCE_NOT_NULL);
            }

            jedis.watch(key);
            if (!jedis.exists(key)) {
                tx = jedis.multi();
                tx.setex(key, seconds, value);
                if (null != tx.exec()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("set redis lock key error!");
            throw throwCacheException(e);
        } finally {
            if (null != jedis) {
                jedis.unwatch();
            }
            returnResource(jedis);
        }
    }

    private CacheException throwCacheException(Exception e) {
        log.error("error due to : " + e.getMessage());
        if(e instanceof JedisConnectionException) {
            return new CacheException(CommonStatusCode.REDIS_CONNECTION_ERROR.getStatus(), "jedis connection exception");
        } else if(e instanceof CacheException) {
            return (CacheException)e;
        }
        return new CacheException(CommonStatusCode.REDIS_OPERATION_ERROR.getStatus(), e.getMessage());
    }

     /**
     * 返还到连接池
     *
     * @param jedis
     */
    public static void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

}
