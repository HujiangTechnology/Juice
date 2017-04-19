package com.hujiang.juice.service.factory;

import com.hujiang.juice.common.config.Configure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;

/**
 * Created by xujia on 16/11/3.
 */

@Slf4j
public enum JuiceFactory{

    INSTANCE;

    private Configure configure;
    private String env;

    JuiceFactory(){
        try {
            env = System.getProperty("system.environment");
            if(StringUtils.isBlank(env)) {
                throw new RuntimeException("can't get env, service stop!");
            }
            configure = new Configure("application-" + env);

        } catch (FileNotFoundException e) {

            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public String getEnv() {
        return env;
    }

    public Configure getConfigure() {
        return configure;
    }
}
