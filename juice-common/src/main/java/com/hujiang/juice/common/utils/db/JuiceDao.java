package com.hujiang.juice.common.utils.db;


import com.hujiang.jooq.juice.tables.pojos.JuiceFramework;
import com.hujiang.jooq.juice.tables.pojos.JuiceTask;
import com.hujiang.juice.common.vo.TaskResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.types.DayToSecond;

import java.time.Duration;
import java.util.List;

import static com.hujiang.jooq.juice.tables.JuiceFramework.JUICE_FRAMEWORK;
import static com.hujiang.jooq.juice.tables.JuiceTask.JUICE_TASK;
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

    public boolean submit(DSLContext contextIn, long taskId, String tenantId, String callbackUrl, String taskName, String dockerName, String commands) {
        return contextIn.insertInto(JUICE_TASK)
                .set(JUICE_TASK.TASK_ID, taskId)
                .set(JUICE_TASK.TENANT_ID, tenantId)
                .set(JUICE_TASK.TASK_NAME, taskName)
                .set(JUICE_TASK.TASK_STATUS, TaskResult.Result.NOT_START.getType())
                .set(JUICE_TASK.CALLBACK_URL, callbackUrl)
                .set(JUICE_TASK.DOCKER_IMAGE, dockerName)
                .set(JUICE_TASK.MESSAGE, "task is submit")
                .set(JUICE_TASK.COMMANDS, commands)
                .execute() > 0;
    }

    public JuiceTask query(long taskId) {
        return context.select()
                .from(JUICE_TASK)
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .fetchOneInto(JuiceTask.class);
    }

    public JuiceTask query(String tenantId, long taskId) {
        return context.select()
                .from(JUICE_TASK)
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .and(JUICE_TASK.TENANT_ID.eq(tenantId))
                .fetchOneInto(JuiceTask.class);
    }

    public JuiceTask queryRunningTask(String tenantId, Long taskId) {
        return context.select()
                .from(JUICE_TASK)
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .and(JUICE_TASK.TENANT_ID.eq(tenantId))
                .and(JUICE_TASK.TASK_STATUS.le(TaskResult.Result.RUNNING.getType()))
                .fetchOneInto(JuiceTask.class);
    }

    public List<JuiceTask> queryRunningTasks(String tenantId, List<Long> taskIds) {
        return context.select()
                .from(JUICE_TASK)
                .where(JUICE_TASK.TASK_ID.in(taskIds))
                .and(JUICE_TASK.TENANT_ID.eq(tenantId))
                .and(JUICE_TASK.TASK_STATUS.le(TaskResult.Result.RUNNING.getType()))
                .fetchInto(JuiceTask.class);
    }

    public List<JuiceTask> queryTasks(String tenantId, List<Long> taskIds) {
        return context.select()
                .from(JUICE_TASK)
                .where(JUICE_TASK.TASK_ID.in(taskIds))
                .and(JUICE_TASK.TENANT_ID.eq(tenantId))
                .fetchInto(JuiceTask.class);
    }

    public boolean finish(long taskId, byte status, String message) {
        return context.update(JUICE_TASK)
                .set(JUICE_TASK.TASK_STATUS, status)
                .set(JUICE_TASK.MESSAGE, message)
                .set(JUICE_TASK.FINISH_AT, currentTimestamp())
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .and(JUICE_TASK.TASK_STATUS.ne(status))
                .execute() > 0;
    }

    public boolean finishWithCallBack(long taskId, byte status, String message) {
        return context.update(JUICE_TASK)
                .set(JUICE_TASK.TASK_STATUS, status)
                .set(JUICE_TASK.MESSAGE, message)
                .set(JUICE_TASK.FINISH_AT, currentTimestamp())
                .set(JUICE_TASK.CALLBACK_AT, currentTimestamp())
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .and(JUICE_TASK.TASK_STATUS.ne(status))
                .execute() > 0;
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


    public boolean update(long taskId, byte status, String message) {
        return context.update(JUICE_TASK)
                .set(JUICE_TASK.TASK_STATUS, status)
                .set(JUICE_TASK.MESSAGE, message)
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .and(JUICE_TASK.TASK_STATUS.ne(status))
                .execute() > 0;
    }

    public boolean update(long taskId, String agent) {
        return context.update(JUICE_TASK)
                .set(JUICE_TASK.AGENT_ID, agent)
                .where(JUICE_TASK.TASK_ID.eq(taskId))
                .execute() > 0;
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
