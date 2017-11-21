package com.hujiang.juice.client.sdk.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hujiang.juice.client.sdk.exception.JuiceClientException;
import com.hujiang.juice.common.error.ErrorCode;
import com.hujiang.juice.common.model.*;

import com.hujiang.juice.common.utils.rest.ParameterTypeReference;
import com.hujiang.juice.common.utils.rest.Restty;
import com.hujiang.juice.common.vo.Result;
import com.hujiang.juice.common.vo.SubmitTask;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.hujiang.juice.common.config.COMMON.PRIORITY;

/**
 * Created by xujia on 17/2/13.
 */

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Submits extends Operations{
    private SubmitTask submitTask;

    private Submits() {
        submitTask = new SubmitTask();
    }
    private Submits(SubmitTask requestTask) {
        this.submitTask = requestTask;
    }

    public static Submits create() {
        return new Submits();
    }

    public static Submits create(SubmitTask requestTask) {
        return new Submits(requestTask);
    }

    public Submits setDocker(Docker docker) {
        if (null != submitTask.getRunMode()) {
            log.info("already set run mode : " + submitTask.getRunMode() + ", can't set it again!");
        }
        submitTask.setRunMode(SubmitTask.RunModel.CONTAINER);
        submitTask.setContainer(new Container(docker));
        return this;
    }

    public Submits setDockerImage(String image) {
        if (null != submitTask.getRunMode()) {
            log.info("already set run mode : " + submitTask.getRunMode() + ", can't set it again!");
        }
        submitTask.setRunMode(SubmitTask.RunModel.CONTAINER);
        submitTask.setContainer(new Container(new Docker(image)));
        return this;
    }

    public Submits setDockerImage(String image, boolean forcePullImage) {
        if (null != submitTask.getRunMode()) {
            log.info("already set run mode : " + submitTask.getRunMode() + ", can't set it again!");
        }
        submitTask.setRunMode(SubmitTask.RunModel.CONTAINER);
        submitTask.setContainer(new Container(new Docker(image, forcePullImage)));
        return this;
    }

    public Submits setCommands(String commands) {
        if (null != submitTask.getRunMode()) {
            log.info("already set run mode : " + submitTask.getRunMode() + ", can't set it again!");
        }
        submitTask.setRunMode(SubmitTask.RunModel.COMMAND);
        submitTask.setCommands(commands);
        return this;
    }

    public Submits setTaskName(String taskName) {
        submitTask.setTaskName(taskName);
        return this;
    }

    public Submits setCallBackUrl(String callBackUrl) {
        submitTask.setCallbackUrl(callBackUrl);
        return this;
    }

    public Submits setHighPriority() {
        submitTask.setPriority(PRIORITY);
        return this;
    }

    public Submits addEnv(String k, String v) {
        if(submitTask.getEnvs() == null) {
            submitTask.setEnvs(Lists.newArrayList());
        }
        submitTask.getEnvs().add(new Command.Environment(k, v));
        return this;
    }

    public Submits setConstraints(Constraints.FIELD key, Set<String> values) {
        submitTask.setConstraints(new Constraints(key.getField(), values));
        return this;
    }

    public Submits addConstraints(Constraints.FIELD key, String value) {
        if(null == submitTask.getConstraints()) {
            submitTask.setConstraints(new Constraints(key.getField(), Sets.newHashSet()));
        }
        submitTask.getConstraints().getValues().add(value);
        return this;
    }

    public Submits addArgs(String arg) {
        if (submitTask.getArgs() == null) {
            submitTask.setArgs(Lists.newArrayList());
        }
        submitTask.getArgs().add(arg);
        return this;
    }

    public Submits addReadOnlyVolume(String containerPath, String hostPath) {
        addVolumes(new Container.Volume(containerPath, hostPath, Container.Volume.DVO.READONLY));
        return this;
    }

    public Submits addReadWriteVolume(String containerPath, String hostPath) {
        addVolumes(new Container.Volume(containerPath, hostPath, Container.Volume.DVO.READWRITE));
        return this;
    }

    public Submits addResources(double cpus, double mems) {
        submitTask.setResources(new Resources(Resources.getCpu(cpus), Resources.getMem(mems)));
        return this;
    }

    private void addVolumes(Container.Volume volume) {
        if (submitTask.getContainer().getVolumes() == null) {
            List<Container.Volume> volumes = Lists.newArrayList();
            submitTask.getContainer().setVolumes(volumes);
        }
        submitTask.getContainer().getVolumes().add(volume);
    }


    @Override
    @SuppressWarnings("unchecked")
    public Long handle(String requestUrl) {
        Result<Response> result = null;
        try {
            result = Restty.create(requestUrl)
                    .addMediaType(Restty.jsonBody())
                    .requestBody(submitTask)
                    .post(new ParameterTypeReference<Result<Response>>() {
                    });

        } catch (IOException e) {
            throw new JuiceClientException(ErrorCode.HTTP_REQUEST_ERROR.getCode(), e.getMessage());
        }
        return result != null ? result.getData().getTaskId() : null;
    }


    @Data
    private class Response {
        private Long taskId;
    }
}
