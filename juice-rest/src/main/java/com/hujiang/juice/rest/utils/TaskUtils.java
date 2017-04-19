package com.hujiang.juice.rest.utils;

import com.hujiang.juice.common.exception.RestException;
import com.hujiang.juice.common.model.Command;
import com.hujiang.juice.common.model.Constraints;
import com.hujiang.juice.common.model.Container;
import com.hujiang.juice.common.model.Resources;
import com.hujiang.juice.common.vo.SubmitTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.hujiang.juice.common.error.ErrorCode.OBJECT_INIT_ERROR;
import static com.hujiang.juice.common.error.ErrorCode.OBJECT_NOT_EQUAL_ERROR;
import static com.hujiang.juice.common.error.ErrorCode.OBJECT_NOT_NULL_ERROR;
import static com.hujiang.juice.common.model.Docker.NetWork.BRIDGE;

/**
 * Created by xujia on 16/12/5.
 */
@Slf4j
public class TaskUtils {

    public static SubmitTask.RunModel checkRunMode(SubmitTask requestTask) {
        if (null == requestTask) {
            throw new RestException(OBJECT_NOT_NULL_ERROR.getCode(), "request task not null!");
        }

        //  check task name
        if (StringUtils.isBlank(requestTask.getTaskName())) {
            throw new RestException(OBJECT_NOT_NULL_ERROR.getCode(), "task name not null!");
        }

        //  check & set resource
        if (null == requestTask.getResources()) {
            requestTask.setResources(new Resources(Resources.CPU_LEVEL.DEFAULT.getLevel(), Resources.MEM_LEVEL.DEFAULT.getLevel()));
        } else {
            requestTask.getResources().checkSet();
        }

        checkEnv(requestTask);
        checkArgs(requestTask);

        checkConstraints(requestTask.getConstraints());

        //  check runner
        if(isRunContainer(requestTask.getContainer())) {

            if(null == requestTask.getContainer().getDocker().getForcePullImage()) {
                requestTask.getContainer().getDocker().setForcePullImage(true);
            }
            if(null == requestTask.getContainer().getDocker().getPrivileged()) {
                requestTask.getContainer().getDocker().setPrivileged(false);
            }

            if(StringUtils.isBlank(requestTask.getContainer().getDocker().getNet())) {
                requestTask.getContainer().getDocker().setNet(BRIDGE);
            }

            return SubmitTask.RunModel.CONTAINER;
        }

        if(isRunCommand(requestTask.getCommands())) {
            return SubmitTask.RunModel.COMMAND;
        }

        //  un support run model
        log.warn("run mode not set, must set docker or command ");
        throw new RestException(OBJECT_NOT_NULL_ERROR.getCode(), "must set docker or command!");
    }


    private static boolean isRunContainer(Container container) {
        if (null == container) {
            return false;
        }
        if(null == container.getDocker() ) {
            return false;
        }
        if (StringUtils.isBlank(container.getDocker().getImage())) {
            //  un support run model
            log.warn("docker image not set with use container run mode");
            throw new RestException(OBJECT_NOT_NULL_ERROR.getCode(),"docker image not set with use container run mode");
        }
        return true;
    }

    private static boolean isRunCommand(String commands) {
        if(StringUtils.isBlank(commands)){
            return false;
        }
        return true;
    }


    private static void checkConstraints(Constraints constraints) {
        if(null == constraints) {
            return;
        }

        if(StringUtils.isBlank(constraints.getField())) {
            throw new RestException(OBJECT_NOT_NULL_ERROR.getCode(), "constraints.field should not null!");
        }

        //  to lowercase for compare
        String field = constraints.getField().toLowerCase();
        if(!field.equals(Constraints.FIELD.RACK_ID.getField()) && !field.equals(Constraints.FIELD.HOSTNAME.getField())) {
            throw new RestException(OBJECT_NOT_EQUAL_ERROR.getCode(), "constraints.field should be one of (rack_id, hostname)!");
        }
        constraints.setField(field);

        if(null == constraints.getValues() || constraints.getValues().isEmpty()) {
            throw new RestException(OBJECT_NOT_NULL_ERROR.getCode(), "constraints.values should not null!");
        }
        if(constraints.getValues().stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet()).size() != constraints.getValues().size()) {
            throw new RestException(OBJECT_NOT_EQUAL_ERROR.getCode(), "constraints.values should not have null value when use constrains mode!");
        }
    }

    private static void checkEnv(SubmitTask requestTask) {
        Command.Environment env = requestTask.getEnv();
        if(env != null) {
            if(StringUtils.isBlank(env.getName()) || StringUtils.isBlank(env.getValue())) {
                throw new RestException(OBJECT_INIT_ERROR.getCode(), "env format error, please check env format in requestBody!");
            }
        }
    }

    private static void checkArgs(SubmitTask requestTask) {
        List <String> args = requestTask.getArgs();
        if(args != null && !args.isEmpty()) {
            List <String> arguments = args.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
            if(null == arguments || arguments.isEmpty() || arguments.size() != args.size()) {
                throw new RestException(OBJECT_INIT_ERROR.getCode(), "args format error, please check args format in requestBody!");
            }
        }
    }
}
