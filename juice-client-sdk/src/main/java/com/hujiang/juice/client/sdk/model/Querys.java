package com.hujiang.juice.client.sdk.model;

import com.google.common.collect.Lists;

import com.hujiang.jooq.juice.tables.pojos.JuiceTask;
import com.hujiang.juice.client.sdk.exception.JuiceClientException;
import com.hujiang.juice.common.error.ErrorCode;
import com.hujiang.juice.common.utils.rest.ParameterTypeReference;
import com.hujiang.juice.common.utils.rest.Restty;
import com.hujiang.juice.common.vo.Result;
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
public class Querys extends Operations{
    private List<Long> taskIdList;

    private Querys() {
        taskIdList = Lists.newArrayList();
    }

    private Querys(List<Long> taskIdList) {
        this.taskIdList = taskIdList;
    }

    public static Querys create() {
        return new Querys();
    }

    public static Querys create(List<Long> taskIdList) {
        return new Querys(taskIdList);
    }

    public Querys addTask(long id) {
        taskIdList.add(id);
        return this;
    }

    public Querys addAllTask(List<Long> ids) {
        taskIdList.addAll(ids);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JuiceTask> handle(String requestUrl, String tenantId) {

        Map<String, String> map = getTaskIdsStr(taskIdList);

        Result<List<JuiceTask>> result = null;
        try {
            result = Restty.create(requestUrl, map)
                    .addMediaType(Restty.jsonBody())
                    .addHeader(TENANT_ID_HEAD, tenantId)
                    .get(new ParameterTypeReference<Result<List<JuiceTask>>>() {
                    });

        } catch (IOException e) {
            throw new JuiceClientException(ErrorCode.HTTP_REQUEST_ERROR.getCode(), e.getMessage());
        }

        return result != null ? result.getData() : null;
    }

}
