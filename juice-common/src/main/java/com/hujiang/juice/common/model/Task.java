package com.hujiang.juice.common.model;

import com.hujiang.juice.common.error.ErrorCode;
import com.hujiang.juice.common.exception.CommonException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.mesos.v1.Protos;
import org.jetbrains.annotations.NotNull;

/**
 * Created by xujia on 16/11/30.
 */

@Slf4j
@Data
public class Task {

    private Resources resources;
    private Container container;
    private Command command;
    private Constraints constraints;

    private String taskName;
    private Long taskId;
    private Expire expire;
    private Integer priority;
    private Integer retry;

    public Task(Resources resources, Container container, Command command, Constraints constraints, @NotNull String taskName, @NotNull Long taskId, Integer priority, Integer retry) {
        this.resources = resources;
        this.container = container;
        this.command = command;
        this.taskName = taskName;
        this.taskId = taskId;
        this.constraints = constraints;
        this.expire = new Expire();
        this.priority = (null == priority || priority <= 0) ? 0 : 1;
        this.retry = (null == retry || retry <= 0) ? 0 : 1;
    }

    public @NotNull static long splitTaskNameId(String value) {

        if(!StringUtils.isNotBlank(value)) {
            log.warn("taskId is null!");
            throw new CommonException(ErrorCode.OBJECT_NOT_NULL_ERROR.getCode(), "taskId is null!");
        }

        String[] parts = value.split("-");
        if(parts.length < 2) {
            log.warn("value length not reach min parts!");
            throw new CommonException(ErrorCode.VALUE_LENGTH_NOT_EQUAL.getCode(), "value length not reach min parts!");
        }
        return Long.parseLong(parts[1]);
    }

    public @NotNull static String generateTaskNameId(String name, long id, int retries) {
        StringBuilder sb = new StringBuilder();
        return sb.append(name.replaceAll("-", "_")).append("-").append(id).append("-").append(retries).toString();
    }

    public @NotNull
    Protos.TaskInfo getTask(
            final @NotNull Protos.AgentID agentID
    ) {
        Protos.TaskInfo.Builder taskInfoBuilder = Protos.TaskInfo.newBuilder();
        taskInfoBuilder.setName(taskName);
        taskInfoBuilder.setTaskId(Protos.TaskID.newBuilder().setValue(generateTaskNameId(taskName, taskId, retry)));
        taskInfoBuilder.setAgentId(agentID);
        taskInfoBuilder.addAllResources(resources.protos());

        if(null != command && StringUtils.isNotBlank(command.getValue())) {
            //  run shell
            return taskInfoBuilder.setCommand(command.protos(true)).build();
        } else {
            //  run docker
            return taskInfoBuilder.setContainer(container.protos()).setCommand(command.protos(false)).build();
        }
    }
}
