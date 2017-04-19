package com.hujiang.juice.rest.utils;

import com.google.gson.Gson;
import com.hujiang.jooq.juice.tables.pojos.JuiceTask;
import com.hujiang.juice.common.error.ErrorCode;
import com.hujiang.juice.common.exception.CacheException;
import com.hujiang.juice.common.exception.DataBaseException;
import com.hujiang.juice.common.exception.RestException;
import com.hujiang.juice.common.utils.cache.CacheUtils;
import com.hujiang.juice.common.utils.db.DaoUtils;
import com.hujiang.juice.common.utils.rest.Restty;
import com.hujiang.juice.common.vo.TaskResult;
import com.hujiang.juice.rest.config.CachesBizConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by xujia on 16/12/8.
 */
@Slf4j
public class SubscriberUtils {

    @Autowired
    private DaoUtils daoUtils;

    @Autowired
    private CacheUtils cacheUtils;

    @Autowired
    private CachesBizConfig cachesBizConfig;

    private ExecutorService fixedThreadPool;

    private Gson gson = new Gson();

    public SubscriberUtils() {
        fixedThreadPool = Executors.newFixedThreadPool(10);
        log.info("init SubscriberUtils success!");
    }

    public void handleResult() {
        while (true) {
            TaskResult taskResult = getTaskResult();
            if (null == taskResult) {
                break;
            }
            log.debug("taskResult : " + taskResult.toString());
            fixedThreadPool.execute(() -> {
                String callbackUrl = "";
                try {
                    JuiceTask task = daoUtils.queryTask(taskResult.getTaskId());
                    if(null != task) {
                        callbackUrl = task.getCallbackUrl();
                        if(StringUtils.isNotBlank(callbackUrl)) {
                            boolean isUpdate;
                            if (taskResult.getResult().getType() > TaskResult.Result.RUNNING.getType()) {
                                isUpdate = daoUtils.finishTaskWithCallBack(taskResult.getTaskId(), taskResult.getResult().getType(), taskResult.getMessage());
                            } else {
                                isUpdate = daoUtils.updateTask(taskResult.getTaskId(), taskResult.getResult().getType(), taskResult.getMessage());
                            }
                            if(isUpdate) {
                                log.debug("url --> " + callbackUrl);
                                log.debug("taskResult --> " + gson.toJson(taskResult));
                                Restty.create(task.getCallbackUrl())
                                        .addHeader("X-Tenant-ID", task.getTenantId())
                                        .addMediaType(Restty.jsonBody())
                                        .requestBody(taskResult)
                                        .postNoResponse();
                            }
                        } else {
                            log.debug("not call back due to url is null, taskId : " + task.getTaskId());
                            if(taskResult.getResult().getType() > TaskResult.Result.RUNNING.getType()) {
                                daoUtils.finishTask(taskResult.getTaskId(), taskResult.getResult().getType(), taskResult.getMessage());
                            } else {
                                daoUtils.updateTask(taskResult.getTaskId(), taskResult.getResult().getType(), taskResult.getMessage());
                            }
                        }
                    } else {
                        log.warn("scheduler service --> task : " + taskResult.getTaskId() + " not exist, can't update task status");
                    }
                } catch (Exception ex) {
                    if (ex instanceof DataBaseException) {
                        log.error("db operating error, cause : " + ex);
                        log.error("notice & check, taskResult : " + taskResult.toString());
                    } else if (ex instanceof IOException) {
                        log.error("call back failed, url : " + callbackUrl);
                        throw new RestException(ErrorCode.HTTP_REQUEST_ERROR.getCode(), ex.getMessage());
                    } else {
                        log.warn("handle subscriber error, : " + ex);
                    }
                    try {
                        throw ex;
                    } catch (IOException ex1) {
                        ex1.printStackTrace();
                    }
                }
            });
        }
    }

    private TaskResult getTaskResult() {
        String message = "";
        try {
            message = cacheUtils.popFromQueue(cachesBizConfig.getResultQueue());
            if (StringUtils.isBlank(message)) {
                return null;
            }
            log.debug(String.format("Message: %s", message));
            return gson.fromJson(message, TaskResult.class);
        } catch (Exception e) {

            if (e instanceof CacheException) {
                log.warn("get message from cache exception!");
                try {
                    Thread.sleep(10 * 1000L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } else {
                log.warn("serialize TaskResult error, message : " + message);
            }
            return null;
        }
    }
}
