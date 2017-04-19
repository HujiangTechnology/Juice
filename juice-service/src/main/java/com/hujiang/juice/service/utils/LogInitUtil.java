package com.hujiang.juice.service.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.hujiang.juice.service.Startup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Created by xujia on 17/2/7.
 */
@Slf4j
public class LogInitUtil {

    public static void initLog() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        context.reset();

        String env = System.getProperty("system.environment");
        if(StringUtils.isBlank(env)) {
            System.err.println("get system.environment error");
            throw new RuntimeException("can't get env, service stop!");
        }
        URL tmpConfigFIleStr = Startup.class.getResource("/logback-" + env + ".xml");
        try {
            System.out.println("start with configFile : " + tmpConfigFIleStr);
            jc.doConfigure(tmpConfigFIleStr);
            log.info("load logback config --> " + tmpConfigFIleStr.getFile());
        } catch (JoranException e) {
            System.err.println(tmpConfigFIleStr + " not exist");
            throw new RuntimeException(e);
        }
    }
}
