package com.hujiang.juice.rest;

import lombok.extern.slf4j.Slf4j;
import com.hujiang.juice.rest.config.AppConfig;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Created by xujia on 16/12/5.
 */

@Slf4j
public class Startup {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(AppConfig.class)
                .build()
                .run(args);
    }
}
