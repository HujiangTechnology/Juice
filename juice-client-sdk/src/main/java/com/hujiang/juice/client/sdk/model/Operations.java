package com.hujiang.juice.client.sdk.model;

import com.google.common.collect.Maps;
import com.hujiang.juice.client.sdk.exception.JuiceClientException;
import com.hujiang.juice.common.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by xujia on 17/2/15.
 */
@Slf4j
public abstract class Operations {

    public abstract <T> T handle(String requestUrl);

    private static String idsToString(List<Long> ids) {
        return ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    Map<String, String> getTaskIdsStr(List<Long> taskIdList) {
        String ids = idsToString(taskIdList);
        if(StringUtils.isBlank(ids)) {
            String message = "task-ids is null!";
            log.warn(message);
            throw new JuiceClientException(ErrorCode.OBJECT_NOT_NULL_ERROR.getCode(), message);
        }
        Map<String, String> map = Maps.newHashMap();
        map.put("taskIds", ids);
        return map;
    }
}
