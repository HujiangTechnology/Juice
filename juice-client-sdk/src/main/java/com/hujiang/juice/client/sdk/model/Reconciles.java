package com.hujiang.juice.client.sdk.model;

import com.google.common.collect.Lists;
import com.hujiang.juice.client.sdk.exception.JuiceClientException;
import com.hujiang.juice.common.error.ErrorCode;
import com.hujiang.juice.common.utils.rest.ParameterTypeReference;
import com.hujiang.juice.common.utils.rest.Restty;
import com.hujiang.juice.common.vo.Result;
import com.hujiang.juice.common.vo.TaskReconcile;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.hujiang.juice.client.sdk.config.COMMON.*;

/**
 * Created by xujia on 17/2/13.
 */

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class Reconciles extends Operations{
    private List<Long> taskIdList;

    private Reconciles() {
        taskIdList = Lists.newArrayList();
    }

    private Reconciles(List<Long> taskIdList) {
        this.taskIdList = taskIdList;
    }

    public static Reconciles create() {
        return new Reconciles();
    }

    public static Reconciles create(List<Long> taskIdList) {
        return new Reconciles(taskIdList);
    }

    public Reconciles addTask(long id) {
        taskIdList.add(id);
        return this;
    }

    public Reconciles addAllTask(List<Long> ids) {
        taskIdList.addAll(ids);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TaskReconcile handle(String requestUrl) {

        Map<String, String> map = getTaskIdsStr(taskIdList);

        Result<TaskReconcile> result = null;
        try {
            String url = requestUrl + URL_RECONCILE;
            result = Restty.create(url, map)
                    .addMediaType(Restty.jsonBody())
                    .post(new ParameterTypeReference<Result<TaskReconcile>>() {
                    });
        } catch (IOException e) {
            throw new JuiceClientException(ErrorCode.HTTP_REQUEST_ERROR.getCode(), e.getMessage());
        }

        return result != null ? result.getData() : null;
    }
}
