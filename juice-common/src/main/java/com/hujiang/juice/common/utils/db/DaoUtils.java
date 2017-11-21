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

    public JuiceTask queryRunningTask(long taskId) {
        try {
            return juiceDao.queryRunningTask(taskId);
        } catch (Exception e) {
            String error = "query running task error, taskId : " + taskId + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public List<JuiceTask> queryRunningTasks(List<Long> taskIds) {
        try {
            return juiceDao.queryRunningTasks(taskIds);
        } catch (Exception e) {
            String error = "query running tasks error, due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public List<JuiceTask> queryTasks(List<Long> taskIds) {
        try {
            return juiceDao.queryTasks(taskIds);
        } catch (Exception e) {
            String error = "query running tasks error, due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean finishTaskWithIP(long taskId, byte type, String message, String ipWithPort) {
        try {
            return juiceDao.finishTask(taskId, type, false, message, ipWithPort, "");
        } catch (DataBaseException e) {
            log.error("finish task error, taskId : " + taskId + ", type : " + type + ", message : " + message + ", due to : " + e.getMessage());
            throw e;
        }
    }

    public boolean finishTaskWithSource(long taskId, byte type, String message, String source) {
        try {
            return juiceDao.finishTask(taskId, type, false, message, "", source);
        } catch (DataBaseException e) {
            log.error("finish task error, taskId : " + taskId + ", type : " + type + ", message : " + message + ", due to : " + e.getMessage());
            throw e;
        }
    }

    public boolean finishTaskWithCallBack(long taskId, byte type, String message, String source) {
        try {
            return this.juiceDao.finishTask(taskId, type, true, message, "", source);
        } catch (Exception var7) {
            log.error("finish task error, taskId : " + taskId + ", type : " + type + ", message : " + message + ", due to : " + var7.getMessage());
            throw var7;
        }
    }

    public boolean retry(Configuration configuration, long taskId, String message) {
        try {
            return juiceDao.rerty(configuration, taskId, message);
        } catch (Exception e) {
            String error = "retry task error, taskId : " + taskId + ", message : " + message + ", due to : " + e.getMessage();
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

    public boolean updateTaskWithLogPath(long taskId, byte type, String message, String logPath) {
        try {
            return juiceDao.updateWithLogPath(taskId, type, message, logPath);
        } catch (Exception e) {
            String error = "update task error, taskId : " + taskId + ", type : " + type + ", message : " + message + ", due to : " + e.getMessage();
            log.error(error);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean updateTaskWithIP(long taskId, String agent, String ip) {
        try {
            return juiceDao.update(taskId, agent, ip);
        } catch (DataBaseException e) {
            log.error("update task error, taskId : " + taskId + ", agent : " + agent + ", host : " + ip + ", due to : " + e.getMessage());
            throw e;
        }
    }

    public boolean updateTask(long taskId, String agent) {
        try {
            return juiceDao.update(taskId, agent, "");
        } catch (Exception e) {
            String warn = "update task error, taskId : " + taskId + ", agent : " + agent + ", due to : " + e.getMessage();
            log.warn(warn);
            throw new DataBaseException(DB_OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    public boolean submit(Configuration configuration, long taskId, String callbackUrl, String taskName, String taskInfo, Integer retry) {
        try {
            return juiceDao.submit(DSL.using(configuration), taskId, callbackUrl, taskName, taskInfo, retry);
        } catch (Exception e) {
            String error = "submit task error, taskId : " + taskId  + ", taskName : " + taskName + ", callbackUrl : " + callbackUrl + ", due to : " + e.getMessage();
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
