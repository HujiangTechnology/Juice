package com.hujiang.juice.common.config;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by xujia on 16-6-6.
 */
public class Configure {
//    public static final String DEFAULT_CONFIG_NAME_KEY = "config-name";

    private Map<String, String> params = new HashMap<String, String>();

    private String configName;

    public Configure(String configName) throws FileNotFoundException {
        this.configName = configName;
        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle(configName);
            Enumeration<String> allKeys = resourceBundle.getKeys();
//            params.put(DEFAULT_CONFIG_NAME_KEY, configName);
            while (allKeys.hasMoreElements()) {
                String key = allKeys.nextElement();
                params.put(key, resourceBundle.getString(key));
            }
        } catch (MissingResourceException e) {
            throw new FileNotFoundException("configure file [" + configName + "] not found!");
        }
    }

    public String getConfigName() {
        return configName;
    }

    public String getValue(String name) {
        if (params.containsKey(name)) {
            return params.get(name);
        }
        return null;
    }
    public String getValue(String name, String defaultValue) {
        if (params.containsKey(name)) {
            return params.get(name);
        }
        return defaultValue;
    }
    public int getValue(String name, int defaultValue) {
        String value = getValue(name);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getValue(String name, long defaultValue) {
        String value = getValue(name);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public float getValue(String name, float defaultValue) {
        String value = getValue(name);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getValue(String name, double defaultValue) {
        String value = getValue(name);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getValue(String name, boolean defaultValue) {
        String value = getValue(name);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Properties toProperties() {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }
}
