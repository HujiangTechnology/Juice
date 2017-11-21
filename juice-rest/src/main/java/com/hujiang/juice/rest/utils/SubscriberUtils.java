package com.hujiang.juice.rest.utils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.hujiang.jooq.juice.tables.pojos.JuiceTask;
import com.hujiang.juice.common.exception.CacheException;
import com.hujiang.juice.common.exception.DataBaseException;
import com.hujiang.juice.common.model.Task;
import com.hujiang.juice.common.utils.cache.CacheUtils;
import com.hujiang.juice.common.utils.db.DaoUtils;
import com.hujiang.juice.common.utils.rest.Restty;
import com.hujiang.juice.common.vo.TaskResult;
import com.hujiang.juice.rest.config.CachesBizConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hujiang.juice.common.config.COMMON.PRIORITY;
import static com.hujiang.juice.common.config.COMMON.RETRY;


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
                try {
                    JuiceTask task = daoUtils.queryTask(taskResult.getTaskId());
                    if (null != task) {
                        String callbackUrl = task.getCallbackUrl();
                        String host = task.getAgentHost();
                        if (StringUtils.isBlank(taskResult.getSource()) && StringUtils.isNotBlank(task.getLogPath())) {
                            taskResult.setSource(task.getLogPath());
                        }

                        //  failed an retry
                        if (taskResult.getResult().getType() > TaskResult.Result.FINISHED.getType() && RETRY == task.getRetry()) {
                            //  task failed and need retry
                            String taskInfo = task.getTaskInfoJson();
                            if (StringUtils.isBlank(taskInfo)) {
                                log.warn("can't retry due to empty taskInfo, taskId : " + task.getTaskId());
                                updateTask(callbackUrl, task.getTaskId(), taskResult, host);
                            } else {
                                try {
                                    Task t = gson.fromJson(taskInfo, Task.class);
                                    t.setRetry(RETRY + 1);
                                    //  retry task;
                                    daoUtils.getContext().transaction(
                                            configuration -> {
                                                if (daoUtils.retry(configuration, task.getTaskId(), taskResult.getMessage())) {
                                                    if (t.getPriority() == PRIORITY) {
                                                        cacheUtils.rpushToQueue(cachesBizConfig.getTaskRetryQueue(), gson.toJson(t));
                                                    } else {
                                                        cacheUtils.pushToQueue(cachesBizConfig.getTaskQueue(), gson.toJson(t));
                                                    }
                                                }
                                            }
                                    );
                                } catch (Exception e) {
                                    log.warn("can't retry due to empty taskInfo, taskId : " + task.getTaskId());
                                    updateTask(callbackUrl, task.getTaskId(), taskResult, host);
                                }
                            }
                        } else if (taskResult.getResult().getType() > TaskResult.Result.STAGING.getType()) {
                            updateTask(callbackUrl, task.getTaskId(), taskResult, host);
                        }
                    } else {
                        log.warn("scheduler service --> task : " + taskResult.getTaskId() + " not exist, can't update task status");
                    }
                } catch (Exception ex) {
                    if (ex instanceof DataBaseException) {
                        log.error("db operating error, cause : " + ex);
                        log.error("notice & check, taskResult : " + taskResult.toString());
                    } else if (ex instanceof IOException) {
                        log.error("call back failed, cause : " + ex);
//                        throw new RestException(ErrorCode.HTTP_REQUEST_ERROR.getCode(), ex.getMessage());
                    } else {
                        log.warn("handle subscriber error, : " + ex);
                    }
                }
            });
        }
    }

    private void updateTask(String callbackUrl, long taskId, TaskResult taskResult, String host) throws IOException {
        if (StringUtils.isNotBlank(callbackUrl)) {
            if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(taskResult.getSource())) {
                List<String> logs = Lists.newArrayList();
                String base = host + "/files/download?path=" + URLEncoder.encode(taskResult.getSource() + "/", "utf-8");
                logs.add(base + "stdout");
                logs.add(base + "stderr");
                taskResult.setLogs(logs);
            }

            //  task running ,then update status
            boolean isUpdate;
            if (taskResult.getResult().getType() <= TaskResult.Result.RUNNING.getType()) {
                isUpdate = daoUtils.updateTaskWithLogPath(taskResult.getTaskId(), taskResult.getResult().getType(), taskResult.getMessage(), taskResult.getSource());
            } else {
                isUpdate = daoUtils.finishTaskWithCallBack(taskId, taskResult.getResult().getType(), taskResult.getMessage(), taskResult.getSource());
            }
            if (isUpdate) {
                log.debug("url --> " + callbackUrl);
                log.debug("taskResult --> " + taskResult);
                Restty.create(callbackUrl)
                        .addMediaType(Restty.jsonBody())
                        .requestBody(taskResult)
                        .postNoResponse();
            }
        } else {
            daoUtils.finishTaskWithSource(taskId, taskResult.getResult().getType(), taskResult.getMessage(), taskResult.getSource());
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
