package com.hujiang.juice.client.sdk.utils;

import com.hujiang.juice.client.sdk.model.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by xujia on 16/12/12.
 */

@Data
@Slf4j
public class JuiceClient {

    private Operations operations;
    private String requestUrl;
    private String accessToken;

    private JuiceClient(String requestUrl, String accessToken) {
        this.requestUrl = requestUrl;
        this.accessToken = accessToken;
    }

    private JuiceClient(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public JuiceClient setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public static JuiceClient create(String requestUrl, String accessToken) {
        return new JuiceClient(requestUrl, accessToken);
    }

    public static JuiceClient create(String requestUrl) {
        return new JuiceClient(requestUrl);
    }

    public JuiceClient setOperations(Operations operations) {
        this.operations = operations;
        return this;
    }

    public <T> T handle() {
        return this.operations.handle(requestUrl);
    }
}
