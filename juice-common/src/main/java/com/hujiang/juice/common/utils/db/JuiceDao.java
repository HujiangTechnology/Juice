package com.hujiang.juice.common.utils.db;


import com.hujiang.jooq.juice.tables.pojos.JuiceFramework;
import com.hujiang.jooq.juice.tables.pojos.JuiceTask;
import com.hujiang.jooq.juice.tables.records.JuiceTaskRecord;
import com.hujiang.juice.common.error.ErrorCode;
import com.hujiang.juice.common.exception.DataBaseException;
import com.hujiang.juice.common.vo.TaskResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.jooq.types.DayToSecond;

import java.time.Duration;
import java.util.List;

import static com.hujiang.jooq.juice.tables.JuiceFramework.JUICE_FRAMEWORK;
import static com.hujiang.jooq.juice.tables.JuiceTask.JUICE_TASK;
import static com.hujiang.juice.common.config.COMMON.MAX_RETRY;
import static com.hujiang.juice.common.config.COMMON.PRIORITY;
import static com.hujiang.juice.common.config.COMMON.RETRY;
import static org.jooq.impl.DSL.currentTimestamp;


/**
 * Created by xujia on 16/12/5.
 */

@Slf4j
@Data
public class JuiceDao {

    private DSLContext context;
    private static final int EXPIRE_DURATION = 24;

    public JuiceDao(DSLContext dlsContext) {
        this.context = dlsContext;
    }

    public boolean submit(DSLContext contextIn, long taskId, String callbackUrl, String taskName, String taskInfo, Integer retry) {
        return contextIn.insertInto(JUICE_TASK)
                .set(JUICE_TASK.TASK_ID, taskId)
                .set(JUICE_TASK.TASK_NAME, taskName)
                .set(JUICE_TASK.TASK_STATUS, TaskResult.Result.NOT_START.getType())
                .set(JUICE_TASK.CALLBACK_URL, callbackUrl)
                .set(JUICE_TASK.MESSAGE, "task is submit")
                .set(JUICE_TASK.TASK_INFO_JSON, taskInfo)
                .set(JUICE_TASK.RETRY, (null == retry || retry <= 0) ? 0 : 1)
                .execute() > 0;
    }

    public JuiceTask query(long taskId) {
        return context.select()
                .from(JUICE_TASK)
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .fetchOneInto(JuiceTask.class);
    }

    public JuiceTask queryRunningTask(Long taskId) {
        return context.select()
                .from(JUICE_TASK)
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .and(JUICE_TASK.TASK_STATUS.le(TaskResult.Result.RUNNING.getType()))
                .fetchOneInto(JuiceTask.class);
    }

    public List<JuiceTask> queryRunningTasks(List<Long> taskIds) {
        return context.select()
                .from(JUICE_TASK)
                .where(JUICE_TASK.TASK_ID.in(taskIds))
                .and(JUICE_TASK.TASK_STATUS.le(TaskResult.Result.RUNNING.getType()))
                .fetchInto(JuiceTask.class);
    }

    public List<JuiceTask> queryTasks(List<Long> taskIds) {
        return context.select()
                .from(JUICE_TASK)
                .where(JUICE_TASK.TASK_ID.in(taskIds))
                .fetchInto(JuiceTask.class);
    }

    public boolean finishTask(@NotNull Long taskId, @NotNull Byte status, boolean isCallBack, String message, String ipWithPort, String source) {
        try {
            UpdateQuery<JuiceTaskRecord> record = context.updateQuery(JUICE_TASK);
            record.addValue(JUICE_TASK.TASK_STATUS, status);
            record.addValue(JUICE_TASK.MESSAGE, message);
            record.addValue(JUICE_TASK.FINISH_AT, currentTimestamp());
            if (isCallBack) {
                record.addValue(JUICE_TASK.CALLBACK_AT, currentTimestamp());
            }
            if (StringUtils.isNotBlank(ipWithPort)) {
                record.addValue(JUICE_TASK.AGENT_HOST, ipWithPort);
            }
            if (StringUtils.isNotBlank(source)) {
                record.addValue(JUICE_TASK.LOG_PATH, source);
            }
            record.addConditions(JUICE_TASK.TASK_ID.eq(taskId));
            record.addConditions(JUICE_TASK.TASK_STATUS.ne(status));

            if (0 == record.execute()) {
                log.debug("not matched record to update, sql : " + record.getSQL());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("data base access error", e);
            throw new DataBaseException(ErrorCode.DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean isExpired(long taskId, int expiredOfHours) {
        if(expiredOfHours <= 0) {
            expiredOfHours = EXPIRE_DURATION;
        }
        return context.select()
                .from(JUICE_TASK)
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .and(JUICE_TASK.TASK_STATUS.le(TaskResult.Result.RUNNING.getType()))
                .and(JUICE_TASK.SUBMIT_AT.add(DayToSecond.valueOf(Duration.ofHours(expiredOfHours).toMillis())).le(currentTimestamp()))
                .fetchOneInto(JuiceTask.class) != null;
    }

    public boolean rerty(Configuration configuration, long taskId, String message) {
        return DSL.using(configuration).update(JUICE_TASK)
                .set(JUICE_TASK.TASK_STATUS, TaskResult.Result.NOT_START.getType())
                .set(JUICE_TASK.MESSAGE, message)
                .set(JUICE_TASK.RETRY, JUICE_TASK.RETRY.add(1))
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .execute() > 0;
    }

    public boolean updateWithLogPath(long taskId, byte status, String message, String logPath) {
        try{
            UpdateQuery<JuiceTaskRecord> record = context.updateQuery(JUICE_TASK);
            record.addValue(JUICE_TASK.TASK_STATUS, status);
            record.addValue(JUICE_TASK.MESSAGE, message);
            if(StringUtils.isNotBlank(logPath)) {
                record.addValue(JUICE_TASK.LOG_PATH, logPath);
            }
            record.addConditions(JUICE_TASK.TASK_ID.eq(taskId));
            record.addConditions(JUICE_TASK.TASK_STATUS.ne(status));

            if (0 == record.execute()) {
                log.debug("not matched record to update, sql : " + record.getSQL());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("data base access error", e);
            throw new DataBaseException(ErrorCode.DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean update(@NotNull Long taskId, @NotNull String agent, String ipWithPort) {
        try {
            UpdateQuery<JuiceTaskRecord> record = context.updateQuery(JUICE_TASK);
            record.addValue(JUICE_TASK.AGENT_ID, agent);
            if (StringUtils.isNotBlank(ipWithPort)) {
                record.addValue(JUICE_TASK.AGENT_HOST, ipWithPort);
            }

            record.addConditions(JUICE_TASK.TASK_ID.eq(taskId));

            if (0 == record.execute()) {
                log.debug("not matched record to update, sql : " + record.getSQL());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("data base access error", e);
            throw new DataBaseException(ErrorCode.DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean saveFrameworkId(String frameworkTag, String frameworkId) {
        return context.insertInto(JUICE_FRAMEWORK)
                .set(JUICE_FRAMEWORK.FRAMEWORK_TAG, frameworkTag)
                .set(JUICE_FRAMEWORK.FRAMEWORK_ID, frameworkId)
                .set(JUICE_FRAMEWORK.IS_ACTIVE, 1)
                .onDuplicateKeyUpdate()
                .set(JUICE_FRAMEWORK.FRAMEWORK_ID, frameworkId)
                .set(JUICE_FRAMEWORK.IS_ACTIVE, 1)
                .set(JUICE_FRAMEWORK.LAST_UPDATE_AT, currentTimestamp())
                .execute() > 0;
    }

    public JuiceFramework queryFramework(String frameworkTag) {
        return context.select()
                    .from(JUICE_FRAMEWORK)
                    .where(JUICE_FRAMEWORK.FRAMEWORK_TAG.eq(frameworkTag))
                    .and(JUICE_FRAMEWORK.IS_ACTIVE.eq(1))
                    .fetchOneInto(JuiceFramework.class);
    }

    public boolean unActiveFramework(String frameworkTag) {
        return context.update(JUICE_FRAMEWORK)
                        .set(JUICE_FRAMEWORK.IS_ACTIVE, 0)
                        .where(JUICE_FRAMEWORK.FRAMEWORK_TAG.eq(frameworkTag))
                        .execute() > 0;
    }
}
