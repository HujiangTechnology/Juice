package com.hujiang.juice.service.service;

import com.google.common.collect.Lists;
import com.hujiang.jooq.juice.tables.pojos.JuiceTask;
import com.hujiang.juice.common.exception.CacheException;
import com.hujiang.juice.common.exception.DataBaseException;
import com.hujiang.juice.common.model.*;
import com.hujiang.juice.common.utils.rest.ParameterTypeReference;
import com.hujiang.juice.common.vo.TaskResult;
import com.hujiang.juice.service.driver.SchedulerDriver;
import com.hujiang.juice.service.exception.DriverException;
import com.hujiang.juice.service.model.SchedulerCalls;
import com.hujiang.juice.service.model.SendErrors;
import com.hujiang.juice.service.utils.SendUtils;
import com.hujiang.juice.service.utils.protocol.Protocol;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.mesos.v1.scheduler.Protos;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import static com.hujiang.juice.common.config.COMMON.KILL;
import static com.hujiang.juice.common.config.COMMON.RECONCILE;
import static com.hujiang.juice.common.utils.CommonUtils.currentTimeSeconds;
import static com.hujiang.juice.service.config.JUICE.*;

import static org.apache.mesos.v1.Protos.*;

/**
 * Created by xujia on 17/1/18.
 */

@Slf4j
public class AuxiliaryService {

    private static final String SANDBOX = "/mnt/mesos/sandbox";

    private static final ExecutorService fixedSendPool = Executors.newFixedThreadPool(OFFER_SEND_POOL);
    private static final ExecutorService fixedAuxiliaryPool = Executors.newFixedThreadPool(AUXILIARY_POOL);
    private static final LinkedBlockingDeque<SendErrors> sendErrors = new LinkedBlockingDeque<>(100000);
    private static final LinkedBlockingDeque<TaskResult> taskErrors = new LinkedBlockingDeque<>(100000);

    public static LinkedBlockingDeque<SendErrors> getSendErrors() {
        return sendErrors;
    }

    public static LinkedBlockingDeque<TaskResult> getTaskErrors() {
        return taskErrors;
    }

    public static void start(SchedulerDriver schedulerDriver) {

        //  one thread to consumer
        fixedAuxiliaryPool.submit(() -> consumer(schedulerDriver));

        //  one thread to schedule update freamework id
        fixedAuxiliaryPool.submit(() -> scheduledFramework(schedulerDriver));


        //one thread to take care of un-handle-task-list
        fixedAuxiliaryPool.submit(AuxiliaryService::handleTaskErrors);

        //one thread to take care of offer-task send errors
        fixedAuxiliaryPool.submit(() -> handleSendErrors(schedulerDriver));
    }

    public static void updateTask(@NotNull Map<Long, String> killMap, long taskId, String agentId, boolean isToKilled, Address address) {
        fixedAuxiliaryPool.submit(() -> {
            try {
                String ipWithPort = "";
                if (null != address && StringUtils.isNotBlank(address.getIp()) && address.getPort() > 0) {

                    if (!address.getIp().startsWith("http")) {
                        ipWithPort = "http://";
                    }
                    ipWithPort += address.getIp() + ":" + address.getPort();
                }
                if (!isToKilled) {
                    daoUtils.updateTaskWithIP(taskId, agentId, ipWithPort);
                } else {
                    daoUtils.finishTaskWithIP(taskId, TaskResult.Result.KILLED.getType(), "task not run due to killed", ipWithPort);
                    killMap.remove(taskId);
                }
            } catch (DataBaseException e) {
                log.error("database operation error, update task agent rel in db failed, it will influence kill task model, taskId : " + taskId + ", agentId :" + agentId);
            }
        });
    }

    public static void update(TaskResult.Result result, String data, long taskId, String message) {
        if (null != result) {
            fixedAuxiliaryPool.submit(() -> {
                TaskResult taskResult = new TaskResult(taskId, result, message);

                try {
                    if (result == TaskResult.Result.RUNNING && StringUtils.isNotBlank(data)) {
                        List<TaskResult.Mounts> mounts = gson.fromJson(data, new ParameterTypeReference<List<TaskResult.Mounts>>() {
                        }.getType());
                        if (null != mounts) {
                            List<TaskResult.Mount> mount = mounts.get(0).getMounts();
                            if (null != mount) {
                                mount.stream().filter(m -> m.getDestination().equals(SANDBOX) && StringUtils.isNotBlank(m.getSource())).findFirst().ifPresent(m -> {
                                    taskResult.setSource(m.getSource());
                                });
                            }
                        }
                    }

                    cacheUtils.pushToQueue(TASK_RESULT_QUEUE, gson.toJson(taskResult));
                    log.debug("update --> task result, taskResult [taskId : "
                            + taskResult.getTaskId()
                            + " status : "
                            + taskResult.getResult().name()
                            + " ]");
                } catch (CacheException e) {
                    AuxiliaryService.getTaskErrors().push(taskResult);
                    log.error("cache not available, push task result error, { taskId : " + taskId + " }");
                }
            });
        }

    }

    public static void acceptOffer(final @NotNull Protocol protocol, final @NotNull String streamId, final @NotNull OfferID offerID,
                                   final @NotNull FrameworkID frameworkID, final @NotNull List<TaskInfo> taskInfos,
                                   final @NotNull String url) {
        fixedSendPool.submit(() -> accept(protocol, streamId, offerID, frameworkID, taskInfos, url));
    }


    public static void declineOffer(final @NotNull Protocol protocol, final @NotNull String streamId, final @NotNull FrameworkID frameworkID,
                                    final @NotNull Protos.Call call, final @NotNull String url) {
        fixedSendPool.submit(() -> decline(protocol, streamId, frameworkID, call, url));
    }


    public static void loggedErrors() {
        if (taskErrors.size() > 0) {
            log.error("logged unhandle error list : ");
            taskErrors.stream().parallel().forEach(
                    un -> {
                        log.error("record : " + gson.toJson(un));
                    }
            );
            taskErrors.clear();
        }
    }

    public static void consumer(SchedulerDriver schedulerDriver) {
        while (true) {
            try {
                String message = cacheUtils.popFromQueue(MANAGEMENT_QUEUE);
                if (StringUtils.isBlank(message)) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                log.debug(String.format("Message: %s", message));
                fixedAuxiliaryPool.submit(() -> {
                    try {
                        handle(message, schedulerDriver);
                    } catch (Exception e) {
                        log.warn("handle message error, message : " + message + ", cause : " + e.getMessage());
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            } catch (CacheException e) {
                log.warn("cache exception : " + e);
                try {
                    Thread.sleep(10 * 1000L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void kill(@NotNull Map<Long, String> killmap, String agentId, @NotNull Protocol protocol, final @NotNull String streamId, final @NotNull FrameworkID frameworkID, @NotNull String url, final @NotNull long taskId) {
        if (killmap.containsKey(taskId)) {
            fixedAuxiliaryPool.submit(() -> {
                JuiceTask juiceTask = daoUtils.queryTask(taskId);
                if (null != juiceTask) {
                    if (juiceTask.getTaskStatus() > TaskResult.Result.RUNNING.getType()) {
                        killmap.remove(taskId);
                        return;
                    }
                    String value = killmap.get(taskId);
                    if (StringUtils.isBlank(value)) {
                        if (StringUtils.isNotBlank(juiceTask.getAgentId())) {
                            value = juiceTask.getAgentId();
                        } else if (StringUtils.isNotBlank(agentId)) {
                            value = agentId;
                        }
                    }
                    if (StringUtils.isNotBlank(value)) {
                        TaskManagement.TaskAgentRel taskAgentRel = new TaskManagement.TaskAgentRel(taskId, juiceTask.getTaskName(), juiceTask.getRetry(), value);
                        Protos.Call call = SchedulerCalls.kill(frameworkID, taskAgentRel);
                        try {
                            SendUtils.sendCall(call, protocol, streamId, url);
                            killmap.remove(taskId);
                        } catch (IOException e) {
                            log.warn("send kill task call error due to : " + e.getCause());
                        }
                    }
                }
            });
        }
    }

    private static void
    handle(String message, SchedulerDriver schedulerDriver) throws IOException {
        TaskManagement taskManagement = gson.fromJson(message, TaskManagement.class);
        Protos.Call call = null;
        if (null != taskManagement) {
            switch (taskManagement.getType()) {
                case KILL:
                    TaskManagement.TaskAgentRel taskAgentRel = taskManagement.getTaskAgentRels().get(0);
                    if (null == taskAgentRel) {
                        log.warn("object taskAgentRel is null");
                        throw new DriverException("object taskAgentRel is null");
                    }
                    JuiceTask juiceTask = daoUtils.queryTask(taskAgentRel.getTaskId());
                    if (null != juiceTask) {
                        if (juiceTask.getTaskStatus() < TaskResult.Result.FINISHED.getType()) {
                            if (StringUtils.isBlank(juiceTask.getAgentId())) {
                                returnManagement(schedulerDriver, taskManagement, juiceTask);
                                return;
                            }

                            taskAgentRel.setAgentId(juiceTask.getAgentId());
                            call = SchedulerCalls.kill(schedulerDriver.getFrameworkId(), taskAgentRel);
                            try {
                                SendUtils.sendCall(call, schedulerDriver.getProtocol(), schedulerDriver.getStreamId(), schedulerDriver.getUrl());

                            } catch (IOException e) {
                                returnManagement(schedulerDriver, taskManagement, juiceTask);
                                return;
                            }

                            if (schedulerDriver.getKillMap().containsKey(juiceTask.getTaskId())) {
                                schedulerDriver.getKillMap().remove(juiceTask.getTaskId());
                            }
                        }
                    }
                    break;
                case RECONCILE:
                    List<TaskManagement.TaskAgentRel> taskAgentRels = taskManagement.getTaskAgentRels();
                    if (null == taskAgentRels || taskAgentRels.size() == 0) {
                        log.warn("object taskAgentRel is null");
                        throw new DriverException("object taskAgentRel is null");
                    }
                    call = SchedulerCalls.reconcile(schedulerDriver.getFrameworkId(), taskAgentRels);
                    try {
                        SendUtils.sendCall(call, schedulerDriver.getProtocol(), schedulerDriver.getStreamId(), schedulerDriver.getUrl());
                    } catch (IOException e) {
                        cacheUtils.pushToQueue(MANAGEMENT_QUEUE, message);
                    }
                    break;
                default:
                    log.warn("unsupported type, type is : " + taskManagement.getType());
                    throw new DriverException("unsupported type, type is : " + taskManagement.getType());
            }
            return;
        }
        log.warn("object taskManagement is null");
        throw new DriverException("object taskManagement is null");
    }

    private static void returnManagement(SchedulerDriver schedulerDriver, TaskManagement taskManagement, JuiceTask juiceTask) {

        //  to reach TASK_RETRY_EXPIRE_TIME in seconds
        if (taskManagement.getRetries() + TASK_RETRY_EXPIRE_TIME > currentTimeSeconds()) {
            if (!schedulerDriver.getKillMap().containsKey(juiceTask.getTaskId())) {
                schedulerDriver.getKillMap().put(juiceTask.getTaskId(), "");
            }
            cacheUtils.pushToQueue(MANAGEMENT_QUEUE, gson.toJson(taskManagement));
        }
    }

    public static void scheduledFramework(SchedulerDriver schedulerDriver) {
        while (true) {
            try {
                //  cached gen framework every hour
                Thread.sleep(3600 * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String frameworkId = schedulerDriver.getFrameworkId().getValue();
            log.debug("framework_id --> " + frameworkId);
            daoUtils.saveFrameworkId(MESOS_FRAMEWORK_TAG, frameworkId);
        }
    }

    public static void handleTaskErrors() {
        while (true) {
            try {
                TaskResult taskResult = getTaskErrors().take();
                if (null != taskResult) {
                    try {
                        cacheUtils.pushToQueue(TASK_RESULT_QUEUE, gson.toJson(taskResult));
                    } catch (CacheException e) {
                        log.error("redis cache is not available, write error to database");
                        try {
                            daoUtils.finishTaskWithSource(taskResult.getTaskId(), taskResult.getResult().getType(), taskResult.getMessage(), "");
                        } catch (DataBaseException e1) {
                            getTaskErrors().push(taskResult);
                            log.error("redis cache and db all error, write after 30's!");
                            try {
                                Thread.sleep(30 * 1000L);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void handleSendErrors(SchedulerDriver schedulerDriver) {
        while (true) {
            try {
                SendErrors errors = getSendErrors().take();
                if (schedulerDriver.getFrameworkId().getValue().equals(errors.getFrameworkId().getValue())) {
                    decline(schedulerDriver.getProtocol(), schedulerDriver.getStreamId(), errors.getFrameworkId(), errors.getCall(), schedulerDriver.getUrl());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void decline(final @NotNull Protocol protocol, final @NotNull String streamId, final @NotNull org.apache.mesos.v1.Protos.FrameworkID frameworkId,
                               final @NotNull Protos.Call call, final @NotNull String url) {
        try {
            SendUtils.sendCall(call, protocol, streamId, url);
        } catch (IOException e) {
            log.error("send decline call to mesos error!");
            log.error("frameworkId : " + frameworkId.getValue());
            log.error("call : " + call);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            getSendErrors().push(new SendErrors(frameworkId, call));
        }
    }

    public static void accept(final @NotNull Protocol protocol, final @NotNull String streamId, final @NotNull org.apache.mesos.v1.Protos.OfferID offerID,
                              final @NotNull org.apache.mesos.v1.Protos.FrameworkID frameworkID, final @NotNull List<org.apache.mesos.v1.Protos.TaskInfo> taskInfos,
                              final @NotNull String url) {
        Protos.Call call = SchedulerCalls.accept(frameworkID, offerID, taskInfos);
        log.info("accept --> Launching {} tasks", taskInfos.size());

        try {
            SendUtils.sendCall(call, protocol, streamId, url);
        } catch (IOException e) {
            log.error("send accept call to mesos error!");
            log.error("frameworkId : " + frameworkID.getValue() + ", offerId : " + offerID.getValue());
            log.error("call : " + call);
            taskInfos.forEach(
                    taskInfo -> {
                        getTaskErrors().add(new TaskResult(com.hujiang.juice.common.model.Task.splitTaskNameId(taskInfo.getTaskId().getValue()), TaskResult.Result.ERROR, "send task to mesos error!"));
                    }
            );
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            List<org.apache.mesos.v1.Protos.OfferID> offerIDS = Lists.newArrayList();
            offerIDS.add(offerID);

            //  if accept error, then offer will be decline
            getSendErrors().push(new SendErrors(frameworkID, SchedulerCalls.decline(frameworkID, offerIDS)));
        }
    }
}
