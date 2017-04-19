package com.hujiang.juice.common.vo;

import lombok.Data;

/**
 * Created by xujia on 17/3/21.
 */

@Data
public class TaskKill {
    private boolean submitTask;
    private byte currentStatus;
    private String resultMessage;

    public TaskKill(boolean submitTask, byte currentStatus, String resultMessage) {
        this.submitTask = submitTask;
        this.currentStatus = currentStatus;
        this.resultMessage = resultMessage;
    }
}
