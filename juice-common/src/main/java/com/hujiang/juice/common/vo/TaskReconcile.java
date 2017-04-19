package com.hujiang.juice.common.vo;

import lombok.Data;

import java.util.List;


/**
 * Created by xujia on 17/2/7.
 */

@Data
public class TaskReconcile {

    private int request;
    private int reconcile;
    private List<Reconcile> reconciles;

    public TaskReconcile(int request, int reconcile, List<Reconcile> reconciles) {
        this.request = request;
        this.reconcile = reconcile;
        this.reconciles = reconciles;
    }

    @Data
    public static class Reconcile {
        private long taskId;
        private boolean reconciled;
        private String message;

        public Reconcile(long taskId, boolean reconciled, String message){
            this.taskId = taskId;
            this.reconciled = reconciled;
            this.message = message;
        }
    }
}
