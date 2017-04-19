package com.hujiang.juice.service.model;

import lombok.Data;

/**
 * Created by xujia on 17/2/6.
 */

@Data
public class Host {
    private String host;
    private static final String uri = "/api/v1/scheduler";

    public String getUrl() {
        return host + uri;
    }
}
