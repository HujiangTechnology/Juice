package com.hujiang.juice.common.model;

import lombok.Data;

import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.apache.mesos.v1.Protos.*;
/**
 * Created by xujia on 17/1/18.
 */

@Data
public class TaskManagement {
    List<TaskAgentRel> taskAgentRels;
    long retries;
    int type;

    public TaskManagement(List<TaskAgentRel> taskAgentRels, int type) {
        this.taskAgentRels = taskAgentRels;
        this.type = type;
        this.retries = currentTimeMillis();
    }

    public static TaskAgentRel newTaskAgentRel(long taskId, String taskName, String agentId) {
        return new TaskAgentRel(taskId, taskName, agentId);
    }

    @Data
    public static class TaskAgentRel {
        long taskId;
        String taskName;
        String agentId;

        public TaskAgentRel(long taskId, String taskName, String agentId) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.agentId = agentId;
        }

        @Override
        public String toString() {
            return "taskId : " + taskId + ", taskName : " + taskName + ", agentId : " + agentId;
        }

        public TaskID protosTaskId() {
            return TaskID.newBuilder().setValue(Task.generateTaskNameId(taskName, taskId)).build();
        }

        public AgentID protosAgentId() {
            return AgentID.newBuilder().setValue(agentId).build();
        }

        public void setAgentId(String agentId){
            this.agentId = agentId;
        }
    }

}
