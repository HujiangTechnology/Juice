package com.hujiang.juice.client.sdk.model;


import com.google.common.collect.Maps;
import com.hujiang.juice.client.sdk.exception.JuiceClientException;
import com.hujiang.juice.common.error.ErrorCode;
import com.hujiang.juice.common.utils.rest.ParameterTypeReference;
import com.hujiang.juice.common.utils.rest.Restty;
import com.hujiang.juice.common.vo.Result;
import com.hujiang.juice.common.vo.TaskKill;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

import static com.hujiang.juice.client.sdk.config.COMMON.*;

/**
 * Created by xujia on 17/2/13.
 */

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class Kills extends Operations {
    private long taskId;

    private Kills(long taskId) {
        this.taskId = taskId;
    }

    private Kills() {

    }

    public static Kills create(long taskId) {
        return new Kills(taskId);
    }

    public static Kills create() {
        return new Kills();
    }

    public Kills setTaskId(long taskId) {
        this.taskId = taskId;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TaskKill handle(String requestUrl) {
        Result<TaskKill> result = null;
        try {
            Map<String, String> map = Maps.newHashMap();
            map.put("taskId", String.valueOf(taskId));
            String url = requestUrl + URL_KILL;
            result = Restty.create(url, map)
                    .addMediaType(Restty.jsonBody())
                    .post(new ParameterTypeReference<Result<TaskKill>>() {
                    });
        } catch (IOException e) {
            throw new JuiceClientException(ErrorCode.HTTP_REQUEST_ERROR.getCode(), e.getMessage());
        }

        return result != null ? result.getData() : null;
    }
}
