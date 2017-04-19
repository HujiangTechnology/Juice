package com.hujiang.juice.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by xujia on 17-3-22.
 */
@Slf4j
public class BeanUtil {

    public static <T> Map<String, Object> obj2Map(Class<T> tClass, T t) {
        return obj2Map(tClass, t, true);
    }

    public static <T> Map<String, Object> obj2Map(Class<T> tClass, T t, boolean isFilterNull) {
        Field[] fields = tClass.getDeclaredFields();
        List<String> fieldNameList = Arrays.stream(fields)
                .filter(f -> !f.getName().equalsIgnoreCase("serialVersionUID"))
                .map(f -> f.getName())
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<String, Object>();
        fieldNameList.forEach(field -> {
            String methodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
            Method method = null;
            try {
                method = tClass.getMethod(methodName);
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
            if (method != null) {
                try {
                    Object obj = method.invoke(t);
                    if (!isFilterNull || obj != null) {
                        map.put(field, obj);
                    }
                } catch (Exception e) {
                    log.info(e.getMessage(), e);
                }
            }
        });
        return map;
    }

    public static <T> T map2Obj(Class<T> tClass, Map<String, Object> map) throws Exception {
        return map2Obj(tClass, map, false);
    }

    public static <T> T map2Obj(Class<T> tClass, Map<String, Object> map, boolean isFilterNull) throws Exception {
        T t = null;
        try {
            t = tClass.newInstance();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new Exception("create Obect[" + tClass.getName() + "] fail.", e);
        }
        final T tmp = t;
        map.entrySet().forEach(entry -> {
            String field = entry.getKey();
            String methodName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
            Method method = null;
            try {
                method = tClass.getMethod(methodName);
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
            if (method != null) {
                Object value = entry.getValue();
                try {
                    if (!isFilterNull || value != null) {
                        method.invoke(tmp, value);
                    }
                } catch (Exception e) {
                    log.info(e.getMessage(), e);
                }
            }
        });
        return t;
    }
}
