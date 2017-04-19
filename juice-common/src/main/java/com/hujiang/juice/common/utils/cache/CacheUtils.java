package com.hujiang.juice.common.utils.cache;

import org.jetbrains.annotations.NotNull;

/**
 * Created by xujia on 17/3/9.
 */
public interface CacheUtils {
    long pushToQueue(@NotNull String queue, @NotNull String value);
    String popFromQueue(@NotNull String queue);
    long lengthOfQueue(@NotNull String queue);
    boolean setExpired(String key, String value, int expired);
    boolean existsKey(String key);
    long delete(String key);
}
