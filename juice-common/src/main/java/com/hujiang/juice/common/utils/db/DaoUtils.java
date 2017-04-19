package com.hujiang.juice.common.utils.db;

import com.hujiang.jooq.juice.tables.pojos.JuiceFramework;
import com.hujiang.jooq.juice.tables.pojos.JuiceTask;
import com.hujiang.juice.common.exception.DataBaseException;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

import static com.hujiang.juice.common.error.ErrorCode.DB_OPERATION_ERROR;

/**
 * Created by xujia on 17/1/18.
 */

@Slf4j
public class DaoUtils {

    private JuiceDao juiceDao;

    public DaoUtils(JuiceDao juiceDao) {
        this.juiceDao = juiceDao;
    }

    public DSLContext getContext() {
        return juiceDao.getContext();
    }

    public JuiceTask queryTask(long taskId) {
        try {
            return juiceDao.query(taskId);
        } catch (Exception e) {
            String error = "query task error, taskId : " + taskId + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public JuiceTask queryTask(String tenantId, long taskId) {
        try {
            return juiceDao.query(tenantId, taskId);
        } catch (Exception e) {
            String error = "query task error, taskId : " + taskId + ", tenantId : " + tenantId + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public JuiceTask queryRunningTask(String tenantId, long taskId) {
        try {
            return juiceDao.queryRunningTask(tenantId, taskId);
        } catch (Exception e) {
            String error = "query running task error, taskId : " + taskId + ", tenantId : " + tenantId + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public List<JuiceTask> queryRunningTasks(String tenantId, List<Long> taskIds) {
        try {
            return juiceDao.queryRunningTasks(tenantId, taskIds);
        } catch (Exception e) {
            String error = "query running tasks error, due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public List<JuiceTask> queryTasks(String tenantId, List<Long> taskIds) {
        try {
            return juiceDao.queryTasks(tenantId, taskIds);
        } catch (Exception e) {
            String error = "query running tasks error, due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean finishTask(long taskId, byte type, String message) {
        try {
            return juiceDao.finish(taskId, type, message);
        } catch (Exception e) {
            String error = "finish task error, taskId : " + taskId + ", type : " + type + ", message : " + message + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean finishTaskWithCallBack(long taskId, byte type, String message) {
        try {
            return juiceDao.finishWithCallBack(taskId, type, message);
        } catch (Exception e) {
            String error = "finish task error, taskId : " + taskId + ", type : " + type + ", message : " + message + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean expiredTask(long taskId, int expiredHours) {
        try {
            return juiceDao.isExpired(taskId, expiredHours);
        } catch (Exception e) {
            String error = "expired task error, taskId : " + taskId + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean updateTask(long taskId, byte type, String message) {
        try {
            return juiceDao.update(taskId, type, message);
        } catch (Exception e) {
            String error = "update task error, taskId : " + taskId + ", type : " + type + ", message : " + message + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean updateTask(long taskId, String agent) {
        try {
            return juiceDao.update(taskId, agent);
        } catch (Exception e) {
            String warn = "update task error, taskId : " + taskId + ", agent : " + agent + ", due to : " + e.getMessage();
            log.warn(warn);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean submit(Configuration configuration, long taskId, String tenantId, String callbackUrl, String taskName, String dockerName, String commands) {
        try {
            return juiceDao.submit(DSL.using(configuration), taskId, tenantId, callbackUrl, taskName, dockerName, commands);
        } catch (Exception e) {
            String error = "submit task error, taskId : " + taskId + ", tenantId : " + tenantId + ", taskName : " + taskName + ", callbackUrl : " + callbackUrl + ", dockerName : " + dockerName + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }


    public boolean saveFrameworkId(String frameworkTag, String frameworkId)  {
        try {
            return juiceDao.saveFrameworkId(frameworkTag, frameworkId);
        } catch (Exception e) {
            String error = "refreshFramework error, frameworkTag : " + frameworkTag + ", frameworkId : " + frameworkId + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public JuiceFramework queryFramework(String frameworkTag) {
        try {
            return juiceDao.queryFramework(frameworkTag);
        } catch (Exception e) {
            String error = "queryFramework error, frameworkTag : " + frameworkTag + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean unActiveFramework(String frameworkTag) {
        try {
            return juiceDao.unActiveFramework(frameworkTag);
        } catch (Exception e) {
            String error = "unActiveFramework error, frameworkTag : " + frameworkTag + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }
}
