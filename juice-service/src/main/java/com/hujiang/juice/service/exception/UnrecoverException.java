package com.hujiang.juice.service.exception;

import lombok.Data;

/**
 * Created by xujia on 16/11/21.
 */

@Data
public class UnrecoverException extends RuntimeException {

    private boolean isResetFrameworkId = false;

    public UnrecoverException(String message, boolean isResetFrameworkId) { this(message, null, isResetFrameworkId ); }

    public UnrecoverException(Throwable cause, boolean isResetFrameworkId) { this(null, cause, isResetFrameworkId); }

    public UnrecoverException(String message, Throwable cause, boolean isResetFrameworkId) {
        super(message, cause);
        this.isResetFrameworkId = isResetFrameworkId;
    }

}
