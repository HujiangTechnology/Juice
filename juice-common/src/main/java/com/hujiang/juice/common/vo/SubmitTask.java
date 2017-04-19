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
    private Command.Environment env;
    private List<String> args;
    private String commands;

    private Constraints constraints;
    private String taskName;
    private RunModel runMode;
    private String callbackUrl;
    private Long taskId;

    public SubmitTask() {

    }

    public SubmitTask(String taskName, String callbackUrl, Command.Environment env, List<String> args, Constraints constraints, Resources resources, Container container) {
        this.taskName = taskName;
        this.callbackUrl = callbackUrl;
        this.env = env;
        this.args = args;
        this.resources = resources;
        this.container = container;
        this.constraints = constraints;
    }

    public SubmitTask(String taskName, String callbackUrl, Command.Environment env, List<String> args, Resources resources, Constraints constraints, String commands) {
        this.taskName = taskName;
        this.callbackUrl = callbackUrl;
        this.env = env;
        this.args = args;
        this.args = args;
        this.resources = resources;
        this.commands = commands;
        this.constraints = constraints;
    }

    public Task toTask() {
        if (runMode == RunModel.COMMAND) {
            return new Task(resources, container, new Command(commands, env, null), constraints, taskName, taskId);
        } else {
            return new Task(resources, container, new Command(null, env, args), constraints, taskName, taskId);
        }
    }

    public enum RunModel {
        COMMAND, CONTAINER
    }
}
