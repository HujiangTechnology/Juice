package com.hujiang.juice.service.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hujiang.jooq.juice.tables.pojos.JuiceFramework;
import com.hujiang.juice.common.exception.CacheException;
import com.hujiang.juice.common.exception.DataBaseException;
import com.hujiang.juice.common.model.*;
import com.hujiang.juice.common.model.Task;
import com.hujiang.juice.service.utils.ResourcesUtils;
import com.hujiang.juice.common.vo.TaskResult;
import com.hujiang.juice.service.exception.UnrecoverException;
import com.hujiang.juice.service.support.Support;
import com.hujiang.juice.service.utils.protocol.Protocol;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.mesos.v1.Protos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.hujiang.juice.common.config.COMMON.CPUS;
import static com.hujiang.juice.common.config.COMMON.MEMS;
import static com.hujiang.juice.common.utils.CommonUtils.currentTimeSeconds;
import static com.hujiang.juice.service.config.JUICE.*;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.mesos.v1.Protos.*;
import static org.apache.mesos.v1.scheduler.Protos.Event;
import static org.apache.mesos.v1.scheduler.Protos.Event.Subscribed;


/**
 * Created by xujia on 16/11/22.
 */
@Slf4j
public class SchedulerService {

    public static void removeFrameworkId() {
        daoUtils.unActiveFramework(MESOS_FRAMEWORK_TAG);
    }

    public static FrameworkID getFrameworkId() {
        JuiceFramework frameworkInfo = null;
        try {
            frameworkInfo = daoUtils.queryFramework(MESOS_FRAMEWORK_TAG);
        } catch (DataBaseException ex) {
            log.error("get frameworkId from db failed {cause : db operation error}!");
            throw ex;
        }

        if (null == frameworkInfo || StringUtils.isBlank(frameworkInfo.getFrameworkId())) {
            return genFrameworkId();
        } else {
            log.info("get framework id from db, id : " + frameworkInfo.getFrameworkId());
            return Protos.FrameworkID.newBuilder().setValue(frameworkInfo.getFrameworkId()).build();
        }
    }

    public static FrameworkID genFrameworkId() {
        String frameworkId = MESOS_SCHEDULER_NAME + "-" + UUID.randomUUID();
        log.info("generator a new framework id : " + frameworkId);
        return Protos.FrameworkID.newBuilder().setValue(frameworkId).build();
    }

    public static void subscribed(Subscribed subscribed, FrameworkID frameworkID) {
        if (null != subscribed) {
            try {
                if (frameworkID != null) {
                    daoUtils.saveFrameworkId(MESOS_FRAMEWORK_TAG, frameworkID.getValue());
                }
            } catch (DataBaseException ex) {
                log.error("save frameworkId error due to db operation error, frameworkId : " + frameworkID);
            }
            log.info("[subscribed success] id: " + subscribed.getFrameworkId() + ", heartbeat : " + subscribed.getHeartbeatIntervalSeconds());
        }
    }

    public static boolean filterAndAddAttrSys(Offer offer, Map<String, Set<String>> attrMaps) {
        if (!attrMaps.containsKey(offer.getId().getValue())) {
            attrMaps.put(offer.getId().getValue(), Sets.newConcurrentHashSet());
        }
        Set<String> stringSet = attrMaps.get(offer.getId().getValue());
        BitSet bs = new BitSet();
        offer.getAttributesList().forEach(
                ar -> {
                    Integer v = MESOS_FRAMEWORK_ATTR_FILTER_MAP.get(ar.getText().getValue());
                    if (null != v && v >= 0) {
                        bs.set(v);
                    }
                    stringSet.add(ar.getText().getValue());
                }
        );
        return bs.equals(MESOS_FRAMEWORK_ATTR_FILTER_DEFAULT_BITSET);
    }

    public static void handleOffers(Map<Long, String> killMap, Support support, Offer offer, Set<String> attributes, List<OfferID> declines, List<TaskInfo> tasks) {
        final String desiredRole = support.getResourceRole();
        final AgentID agentId = offer.getAgentId();
        final OfferID offerId = offer.getId();

        Map<String, Set<String>> facts = generatorFacts(offer, attributes);

        final Map<String, List<Resource>> resources = offer.getResourcesList()
                .stream()
                .collect(groupingBy(Resource::getName));

        final List<Resource> cpuList = resources.get(CPUS);
        final List<Resource> memList = resources.get(MEMS);

        if (null != cpuList && !cpuList.isEmpty()
                && null != memList && !memList.isEmpty()
                && cpuList.size() == memList.size()) {

            for (int i = 0; i < cpuList.size(); i++) {
                final Resource cpus = cpuList.get(i);
                final Resource mem = memList.get(i);

                boolean isExhausted = false;
                if (desiredRole.equals(cpus.getRole()) && desiredRole.equals(mem.getRole())) {
                    ResourcesUtils resourcesUtils = new ResourcesUtils(cpus.getScalar().getValue(), mem.getScalar().getValue(), RESOURCES_USE_THRESHOLD, desiredRole);
                    isExhausted = allocatingUntilExhausted(killMap, agentId, facts, resourcesUtils, tasks);
                }
                if (isExhausted) {
                    break;
                }
            }

            if(tasks.isEmpty()) {
                declines.add(offerId);
            }
        } else {
            declines.add(offerId);
        }
    }

    public static void update(Map<Long, String> killmap, TaskStatus taskStatus, @NotNull Protocol protocol, @NotNull FrameworkID frameworkID, @NotNull  String streamId, @NotNull  String url) {
        long taskId = com.hujiang.juice.common.model.Task.splitTaskNameId(taskStatus.getTaskId().getValue());
        String message = taskStatus.getMessage();
        TaskResult.Result result = null;
        log.debug("update --> taskId : " + taskId + ", task status : " + taskStatus.getState());
        switch (taskStatus.getState()) {
            case TASK_STAGING:
                message = StringUtils.isBlank(message) ? "task staging" : message;
                result = TaskResult.Result.STAGING;
                AuxiliaryService.kill(killmap, taskStatus.getAgentId().getValue(), protocol, streamId, frameworkID, url, taskId);
                break;
            case TASK_RUNNING:
                message = StringUtils.isBlank(message) ? "task running" : message;
                result = TaskResult.Result.RUNNING;
                AuxiliaryService.kill(killmap, taskStatus.getAgentId().getValue(), protocol, streamId, frameworkID, url, taskId);
                break;
            case TASK_FINISHED:
                message = StringUtils.isBlank(message) ? "task finished" : message;
                result = TaskResult.Result.FINISHED;
                break;
            case TASK_FAILED:
                message = StringUtils.isBlank(message) ? "task failed" : message;
                result = TaskResult.Result.FAILED;
                break;
            case TASK_LOST:
                message = StringUtils.isBlank(message) ? "task lost" : message;
                result = TaskResult.Result.LOST;
                break;
            case TASK_ERROR:
                message = StringUtils.isBlank(message) ? "task error" : message;
                result = TaskResult.Result.ERROR;
                break;
            case TASK_KILLED:
                message = StringUtils.isBlank(message) ? "task killed" : message;
                result = TaskResult.Result.KILLED;
                if(killmap.containsKey(taskId)) {
                    killmap.remove(taskId);
                }
                break;
            case TASK_UNREACHABLE:
                message = StringUtils.isBlank(message) ? "task unreachable" : message;
                result = TaskResult.Result.UNREACHABLE;
                break;
            case TASK_DROPPED:
                message = StringUtils.isBlank(message) ? "task dropped" : message;
                result = TaskResult.Result.DROPPED;
                break;
            case TASK_GONE:
                message = StringUtils.isBlank(message) ? "task gone" : message;
                result = TaskResult.Result.GONE;
                break;
            case TASK_GONE_BY_OPERATOR:
                message = StringUtils.isBlank(message) ? "task gone by operator" : message;
                result = TaskResult.Result.GONE_BY_OPERATOR;
                break;
            case TASK_UNKNOWN:
                message = StringUtils.isBlank(message) ? "task gone by unknown" : message;
                result = TaskResult.Result.UNKNOWN;
                break;
        }
        if (null != result) {
            TaskResult taskResult = new TaskResult(taskId, result, message);
            try {
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
        }
    }

    public static void error(Event event) {
        log.warn("event error --> " + event);

        String message = event.getError().getMessage();
        log.error("event error --> " + message);
        if (message.toLowerCase().contains("framework has been removed") || message.toLowerCase().contains("framework is not authorized")) {
            throw  new UnrecoverException(message, true);
        } else {
            throw new UnrecoverException(message, false);
        }
    }

    public static void message(AgentID agentID, byte[] data) {
        log.info("message, agentID : " + agentID + ", data : " + data);
    }

    private static Map<String, Set<String>> generatorFacts(@NotNull Protos.Offer offer, Set<String> attributes) {
        Map<String, Set<String>> facts = Maps.newHashMap();
        //  add host
        if (StringUtils.isNotBlank(offer.getHostname())) {
            facts.put(Constraints.FIELD.HOSTNAME.getField(), Sets.newHashSet());
            facts.get(Constraints.FIELD.HOSTNAME.getField()).add(offer.getHostname());
        }
        //  add attr
        if (null != attributes && !attributes.isEmpty()) {
            facts.put(Constraints.FIELD.RACK_ID.getField(), Sets.newHashSet());
            facts.get(Constraints.FIELD.RACK_ID.getField()).addAll(attributes);
        }

        return facts;
    }

    private static boolean allocatingUntilExhausted(Map<Long, String> killMap, Protos.AgentID agentId, Map<String, Set<String>> facts, ResourcesUtils hardware, List<Protos.TaskInfo> tasks) {

        long cacheTries = CACHE_TRIES;
        //  when either cpu or memory reach the picket line, will stop allocation task
        while (hardware.isAvailable()) {
            if (cacheTries > 0) {
                cacheTries = taskOrResourceExhausted(killMap, agentId, facts, hardware, tasks, true) ? 0 : cacheTries - 1;
            } else {
                if (taskOrResourceExhausted(killMap, agentId, facts, hardware, tasks, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean taskOrResourceExhausted(@NotNull Map<Long, String> killMap, Protos.AgentID agentId, Map<String, Set<String>> facts, ResourcesUtils hardware, List<Protos.TaskInfo> tasks, boolean isRetryTask) {

        //  is task in cache exhausted ?
        String tskStr = isRetryTask ? cacheUtils.popFromQueue(TASK_RETRY_QUEUE) : cacheUtils.popFromQueue(TASK_QUEUE);
        if (StringUtils.isBlank(tskStr)) {
            return true;
        }

        //  is resource exhausted ?
        com.hujiang.juice.common.model.Task task = gson.fromJson(tskStr, Task.class);

        //  current offer not match task, try next
        if (!availableConstraints(task.getConstraints(), facts)) {
            task.getExpire().incrementOfferLack();
            taskReserve(task, isRetryTask);
        } else {
            if (hardware.allocating(task.getResources())) {
                //  accept task
                addTask(killMap, agentId, task, tasks, isRetryTask);
            } else {
                //  resources is exhausted
                task.getExpire().incrementResourceLack();
                taskReserve(task, isRetryTask);
                return true;
            }
        }
        return false;

    }

    private static boolean availableConstraints(Constraints constraints, Map<String, Set<String>> facts) {
        if (null == constraints) {
            return true;
        }
        return constraints.isAvailable(facts);
    }

    private static void addTask(@NotNull Map<Long, String> killMap, @NotNull Protos.AgentID agentId, @NotNull Task task, @NotNull List<Protos.TaskInfo> tasks, boolean isRetryTask) {
        boolean isToKilled = false;
        try {
            isToKilled = killMap.containsKey(task.getTaskId());
            //update db set taskAgentRel
            AuxiliaryService.updateTask(killMap, task.getTaskId(), agentId.getValue(), isToKilled);
            if(!isToKilled){
                tasks.add(task.getTask(agentId));
                log.info("resourceAllocation --> add task : " + task.getTaskId());
            }
        } catch (Exception e) {
            if(!isToKilled) {
                taskReserve(task, isRetryTask);
            }
        }
    }

    private static void taskReserve(Task task, boolean isRetryTask) {
        if (isRetryTask) {
            if (currentTimeSeconds() - task.getExpire().getFirstReservedTimes() > TASK_RETRY_EXPIRE_TIME) {
                addToTaskErrors(task.getTaskId(), TaskResult.Result.EXPIRED, "task keep in queue's time > " + TASK_RETRY_EXPIRE_TIME + " seconds");
            } else if (task.getExpire().getOfferLack() > MAX_RESERVED) {
                addToTaskErrors(task.getTaskId(), TaskResult.Result.ERROR, "lack of offer to handle task, constraints : " + task.getConstraints().toString());
            } else if (task.getExpire().getResourceLack() > MAX_RESERVED) {
                addToTaskErrors(task.getTaskId(), TaskResult.Result.ERROR, "lack of resource to handle task, resources : " + task.getResources().toString());
            } else {
                pushRetryTask(task);
            }
        } else {
            task.getExpire().setFirstReservedTimes(currentTimeSeconds());
            pushRetryTask(task);
        }
    }

    private static void pushRetryTask(Task task) {
        try {
            cacheUtils.pushToQueue(TASK_RETRY_QUEUE, gson.toJson(task));
        } catch (CacheException e) {
            log.error("cache exception, push task to retry-queue error, { taskId : " + task.getTaskId() + " }");
            addToTaskErrors(task.getTaskId(), TaskResult.Result.ERROR, "cache exception, push task to retry-queue error");
            throw e;
        }
    }

    private static void addToTaskErrors(long taskId, TaskResult.Result result, String message) {
        AuxiliaryService.getTaskErrors().push(new TaskResult(taskId, result, message));
    }
}
