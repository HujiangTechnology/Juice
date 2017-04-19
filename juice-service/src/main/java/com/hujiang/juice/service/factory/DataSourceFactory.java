package com.hujiang.juice.service.factory;

import com.alibaba.druid.pool.DruidDataSource;
import com.hujiang.juice.common.config.Configure;
import com.hujiang.juice.common.error.ErrorCode;
import com.hujiang.juice.common.exception.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;

import javax.sql.DataSource;
import java.io.FileNotFoundException;

/**
 * Created by xujia on 17/3/4.
 */
public enum DataSourceFactory {

    INSTANCE;

    private Configure configure;

    DataSourceFactory() {
        try {
            String env = System.getProperty("system.environment");
            if(StringUtils.isBlank(env)) {
                throw new ConfigurationException(ErrorCode.SYSTEM_ENV_NOT_VALID.getCode(), "can't get env, service stop!");
            }
            configure = new Configure("application-" + env);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    //database config
    public static final String CONFIG_DATABASE_DRIVER = "db.driver";
    public static final String CONFIG_DATABASE_URL = "db.url";
    public static final String CONFIG_DATABASE_USER = "db.user";
    public static final String CONFIG_DATABASE_PASSWORD = "db.password";
    public static final String CONFIG_DATABASE_POOL_INITIAL_SIZE = "db.pool.initial.size";
    public static final String CONFIG_DATABASE_POOL_MAX_ACTIVE = "db.pool.max.active";
    public static final String CONFIG_DATABASE_POOL_VALIDATION_QUERY = "db.pool.validation.query";
    public static final String CONFIG_DATABASE_POOL_TEST_WHILE_IDLE = "db.pool.test.while.idle";
    public static final String CONFIG_DATABASE_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLS = "db.pool.time.between.eviction.runs.millis";

    //database default value
    public static final int DEFAULT_DATABASE_POOL_INITIAL_SIZE = 5;
    public static final int DEFAULT_DATABASE_POOL_MAX_ACTIVE = 10;
    public static final String DEFAULT_DATABASE_POOL_VALIDATION_QUERY = "select 1";
    public static final boolean DEFAULT_DATABASE_POOL_TEST_WHILE_IDLE = true;
    public static final int DEFAULT_DATABASE_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLS = 300000;

    public String getValue(String name) {
        return configure.getValue(name);
    }

    public String getValue(String name, String defaultValue) {
        String value = getValue(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public int getValue(String name, int defaultValue) {
        return configure.getValue(name, defaultValue);
    }

    public long getValue(String name, long defaultValue) {
        return configure.getValue(name, defaultValue);
    }

    public float getValue(String name, float defaultValue) {
        return configure.getValue(name, defaultValue);
    }

    public double getValue(String name, double defaultValue) {
        return configure.getValue(name, defaultValue);
    }

    public boolean getValue(String name, boolean defaultValue) {
        return configure.getValue(name, defaultValue);
    }

    public Configure getConfigure(){
        return configure;
    }

    public enum Database {
        INSTANCE(DataSourceFactory.INSTANCE);

        private DataSource dataSource;

        Database(DataSourceFactory configure) {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName(configure.getValue(CONFIG_DATABASE_DRIVER));
            dataSource.setUrl(configure.getValue(CONFIG_DATABASE_URL));
            dataSource.setUsername(configure.getValue(CONFIG_DATABASE_USER));
            dataSource.setPassword(configure.getValue(CONFIG_DATABASE_PASSWORD));

            dataSource.setInitialSize(configure.getValue(CONFIG_DATABASE_POOL_INITIAL_SIZE, DEFAULT_DATABASE_POOL_INITIAL_SIZE));
            dataSource.setMaxActive(configure.getValue(CONFIG_DATABASE_POOL_MAX_ACTIVE, DEFAULT_DATABASE_POOL_MAX_ACTIVE));
            dataSource.setValidationQuery(configure.getValue(CONFIG_DATABASE_POOL_VALIDATION_QUERY, DEFAULT_DATABASE_POOL_VALIDATION_QUERY));
            dataSource.setTestWhileIdle(configure.getValue(CONFIG_DATABASE_POOL_TEST_WHILE_IDLE, DEFAULT_DATABASE_POOL_TEST_WHILE_IDLE));
            dataSource.setTimeBetweenEvictionRunsMillis(configure.getValue(CONFIG_DATABASE_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLS, DEFAULT_DATABASE_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLS));
            this.dataSource = dataSource;
        }

        public DataSource getDataSource() {
            return dataSource;
        }
    }

    public enum JOOQ{
        INSTANCE(Database.INSTANCE.getDataSource());

        private DSLContext context;
        JOOQ(DataSource dataSource){
            context = new DefaultDSLContext(dataSource, SQLDialect.MYSQL);
        }

        public DSLContext getContext() {
            return context;
        }
    }
}
