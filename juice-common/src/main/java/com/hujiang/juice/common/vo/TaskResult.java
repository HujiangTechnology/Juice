package com.hujiang.juice.common.vo;

import lombok.Data;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by xujia on 16/11/30.
 */

@Data
public class TaskResult {
    private long taskId;
    private Result result;
    private String message;

    public TaskResult(long taskId, Result result, String message) {
        this.taskId = taskId;
        this.result = result;
        this.message = message;
    }

    public enum Result {
        NOT_START(Byte.valueOf("-1")),
        STAGING(Byte.valueOf("0")),
        RUNNING(Byte.valueOf("1")),
        FINISHED(Byte.valueOf("2")),
        FAILED(Byte.valueOf("3")),
        LOST(Byte.valueOf("4")),
        ERROR(Byte.valueOf("5")),
        KILLED(Byte.valueOf("6")),
        UNREACHABLE(Byte.valueOf("7")),
        DROPPED(Byte.valueOf("8")),
        GONE(Byte.valueOf("9")),
        GONE_BY_OPERATOR(Byte.valueOf("10")),
        UNKNOWN(Byte.valueOf("11")),
        EXPIRED(Byte.valueOf("12"));

        private byte type;

        public byte getType() {
            return type;
        }

        Result(byte type) {
            this.type = type;
        }

        public static String getName(byte b) {
            Optional<Result> result = Arrays.stream(Result.values()).filter(v -> v.getType() == b).findFirst();
            if(result.isPresent()) {
                return result.get().name();
            }
            return null;
        }
    }
}
