package com.hujiang.juice.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by xujia on 2016/6/14.
 */
public class JsonUtils {

    private static final String MediaType = "application/json;charset=UTF-8";
    private static ObjectMapper mapper = new ObjectMapper();

    public static void writeJsonResult(HttpServletResponse response, Object data) throws IOException {
        response.setContentType(MediaType);
        mapper.writeValue(response.getOutputStream(), data);
    }

}
