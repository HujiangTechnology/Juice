package com.hujiang.juice.rest.web.controller;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.hujiang.jooq.juice.tables.pojos.JuiceTask;
import com.hujiang.juice.common.exception.RestException;
import com.hujiang.juice.common.vo.Result;
import com.hujiang.juice.common.vo.SubmitTask;
import com.hujiang.juice.common.vo.TaskKill;
import com.hujiang.juice.common.vo.TaskReconcile;
import com.hujiang.juice.rest.service.RestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hujiang.juice.common.error.ErrorCode.DATA_FORMAT_ERROR;
import static com.hujiang.juice.common.error.ErrorCode.OBJECT_INIT_ERROR;
import static com.hujiang.juice.common.error.ErrorCode.OBJECT_NOT_NULL_ERROR;


/**
 * Created by xujia on 16/12/5.
 */
@Slf4j
@RestController
@RequestMapping(value = {"/v1/tasks"})
@CrossOrigin(origins = "*")
public class TaskController extends BaseAppController {

    @Autowired
    private RestService restService;

    private Gson gson = new Gson();

    @RequestMapping(method = RequestMethod.POST)
    public Result<Map<String, Long>> post(@RequestHeader(value = "X-Tenant-Id") String tenantId, @RequestBody String requestBody) throws Exception {
        if(StringUtils.isBlank(requestBody)) {
            throw new RestException(OBJECT_NOT_NULL_ERROR.getCode(), "requestBody is not null");
        }
        log.debug("request task body --> " + requestBody);
        SubmitTask object = null;
        try {
            object = gson.fromJson(requestBody, SubmitTask.class);
        } catch (Exception e) {
            throw new RestException(OBJECT_INIT_ERROR.getCode(), e.getMessage());
        }
        long taskId = restService.submits(object, tenantId);
        Map<String, Long> map = Maps.newHashMap();
        map.put("taskId", taskId);
        return buildResult(map);
    }

    @RequestMapping(method = RequestMethod.GET)
    public Result<List<JuiceTask>> get(@RequestHeader(value = "X-Tenant-Id") String tenantId, @RequestParam(value = "taskIds") String taskIds) throws Exception {
        if(StringUtils.isBlank(taskIds)) {
            throw new RestException(OBJECT_NOT_NULL_ERROR.getCode(), "taskIds is not null");
        }
        List<JuiceTask> tasks = restService.querys(tenantId, split(taskIds, ","));
        log.info("tasks : " + tasks.size());
        return buildResult(tasks);
    }

    @RequestMapping(value = "/kill", method = RequestMethod.POST)
    public Result<TaskKill> kill(@RequestHeader(value = "X-Tenant-Id") String tenantId, @RequestParam(value = "taskId") Long taskId) throws Exception {
        if(null == taskId) {
            throw new RestException(OBJECT_NOT_NULL_ERROR.getCode(), "taskId is not null");
        }
        log.info("request taskId --> " + taskId);
        return buildResult(restService.kills(tenantId, taskId));
    }

    @RequestMapping(value = "/reconcile", method = RequestMethod.POST)
    public Result<TaskReconcile> reconcile(@RequestHeader(value = "X-Tenant-Id") String tenantId, @RequestParam(value = "taskIds") String taskIds) throws Exception {
        if(StringUtils.isBlank(taskIds)) {
            throw new RestException(OBJECT_NOT_NULL_ERROR.getCode(), "taskIds is not null");
        }
        return buildResult(restService.reconciles(tenantId, split(taskIds, ",")));
    }

    private List<Long> split(String str, String splitter) {
        String[] s = str.split(splitter);
        if (s.length > 0) {
            try {
                return Arrays.stream(s).map(String::trim).map(Long::parseLong).collect(Collectors.toList());
            } catch (Exception e) {
                throw new RestException(DATA_FORMAT_ERROR.getCode(), "split string to long list error!");
            }
        }
        throw new RestException(DATA_FORMAT_ERROR.getCode(),
                DATA_FORMAT_ERROR.getMessage() + ", taskIds error, correct taskId should be Long type & split by ','");
    }
}
