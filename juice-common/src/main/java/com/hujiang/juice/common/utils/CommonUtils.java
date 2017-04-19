package com.hujiang.juice.common.utils;

/**
 * Created by xujia on 16/11/21.
 */
public class CommonUtils {

    public static String fixUrl(String url) {
        if (!url.startsWith("http://")) url = "http://" + url;
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        return url;
    }

    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
