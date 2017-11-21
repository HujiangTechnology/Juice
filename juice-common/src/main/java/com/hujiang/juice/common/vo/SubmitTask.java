package com.hujiang.juice.common.vo;

import com.hujiang.juice.common.model.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * Created by xujia on 16/11/8.
 */

@Data
@Slf4j
public class SubmitTask {

    private Resources resources;
    private Container container;

    private String commands;
    private List<String> args;
    private List<String> uris;
    private List<Command.Environment> envs;

    private Constraints constraints;
    private String taskName;
    private RunModel runMode;
    private String callbackUrl;
    private Long taskId;
    private Integer priority;
    private Integer retry;

    public SubmitTask() {

    }

    public Task toTask() {
        if (runMode == RunModel.COMMAND) {
            return new Task(resources, container, new Command(commands, envs, null, uris), constraints, taskName, taskId, priority, retry);
        } else {
            return new Task(resources, container, new Command(null, envs, args, uris), constraints, taskName, taskId, priority, retry);
        }
    }

    public enum RunModel {
        COMMAND, CONTAINER
    }
}
