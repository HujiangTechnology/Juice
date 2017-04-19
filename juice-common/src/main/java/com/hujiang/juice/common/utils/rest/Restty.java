package com.hujiang.juice.common.utils.rest;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.ByteString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hujiang.juice.common.utils.rest.Restty.DefaultHttpClientSetting.*;


/**
 * Created by xujia on 16/6/7.
 */
@Slf4j
@Data
public class Restty {

    private Gson gson = new Gson();

    private OkHttpUtils okHttpUtils;

    private String url = null;
    private RequestBody requestBody = null;
    private Map<String, String> headers = null;
    private MediaType mediaType = null;

    private long connectTimeOut;
    private long readTimeOut;
    private long writeTimeOut;
    private int retrys;
    private boolean emptyParameter = false;

    public static String protoBody() {
        return BodyType.PROTOBUF.getType();
    }

    public static String textBody() {
        return BodyType.TEXT.getType();
    }

    public static String streamBody() {
        return BodyType.STREAMS.getType();
    }

    public static String jsonBody() {
        return BodyType.JSON.getType();
    }


    private Restty(String url, long connectTimeOut, long readTimeOut, long writeTimeOut, int retrys) {
        this.url = url;
        this.connectTimeOut = connectTimeOut;
        this.readTimeOut = readTimeOut;
        this.writeTimeOut = writeTimeOut;
        this.retrys = retrys;
        okHttpUtils = OkHttpUtils.getInstance(connectTimeOut, readTimeOut, writeTimeOut, retrys);
    }

    public static Restty create(String url) {
        return new Restty(url, CONNECT_TIME_OUT, READ_TIME_OUT, WRITE_TIME_OUT, OK_HTTP_RETRY_TIMES);
    }

    public static Restty create(String url, Map<String, String> mapParams) {
        return new Restty(url, CONNECT_TIME_OUT, READ_TIME_OUT, WRITE_TIME_OUT, OK_HTTP_RETRY_TIMES).addAllParameters(mapParams);
    }

    public static Restty create(String url, long connectTimeOut, long readTimeOut, long writeTimeOut, int retrys) {
        return new Restty(url, connectTimeOut, readTimeOut, writeTimeOut, retrys);
    }

    public static Restty create(String url, long connectTimeOut, long readTimeOut, long writeTimeOut, int retrys, Map<String, String> mapParams) {
        return new Restty(url, connectTimeOut, readTimeOut, writeTimeOut, retrys).addAllParameters(mapParams);
    }

    public Restty addAllParameters(Map<String, String> mapParams) {
        StringBuilder sb = new StringBuilder();
        if (null != mapParams && mapParams.size() > 0) {
            for (Map.Entry<String, String> entry : mapParams.entrySet()) {
                sb.append(generateParameterString(entry.getKey(), entry.getValue()));
            }
        }
        this.url += sb.toString();
        return this;
    }

    public Restty addParameter(String key, String value) {
        this.url += generateParameterString(key, value);
        return this;
    }

    public Restty addParameters(String key, List<Object> values) {
        if (null == values || values.size() == 0) {
            return this;
        }
        String valueString = values.stream().map(Object::toString).collect(Collectors.joining(","));

        return addParameter(key, valueString);
    }

    private String generateParameterString(String key, String value) {
        StringBuilder sb = new StringBuilder();
        if (!emptyParameter) {
            synchronized (this) {
                if(!emptyParameter) {
                    sb.append("?");
                    emptyParameter = true;
                } else {
                    sb.append("&");
                }
            }
        } else {
            sb.append("&");
        }
        sb.append(key).append("=").append(value);
        return sb.toString();
    }


    public Restty addMediaType(String mediaType) {
        this.mediaType = MediaType.parse(mediaType);
        return this;
    }

    public Restty addKeepAlive() {
        addHeader("Connection", "keep-alive");
        return this;
    }

    public Restty addAccept(String value) {
        addHeader("Accept", value);
        return this;
    }

    public Restty addHeader(String name, String value) {
        if (null == headers) {
            headers = Maps.newHashMap();
        }
        headers.put(name, value);
        return this;
    }

    public Restty addHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public Restty requestBody(Object body) {
        requestBody = RequestBody.create(mediaType, gson.toJson(body));
        return this;
    }

    public Restty requestBody(String body) {
        requestBody = RequestBody.create(mediaType, body);
        return this;
    }

    public Restty requestBody(File body) {
        requestBody = RequestBody.create(mediaType, body);
        return this;
    }

    public Restty requestBody(ByteString body) {
        requestBody = RequestBody.create(mediaType, body);
        return this;
    }

    public Restty requestBody(byte[] body) {
        requestBody = RequestBody.create(mediaType, body);
        return this;
    }

    public Restty requestBody(byte[] body, int offset, int byteCount) {
        requestBody = RequestBody.create(mediaType, body, offset, byteCount);
        return this;
    }

    //  post
    public <T> T post(ParameterTypeReference<T> parameterTypeReference) throws IOException {
        try (Response response = post()) {
            return null == response.body() ? null : gson.fromJson(response.body().charStream(), parameterTypeReference.getType());
        }
    }

    //  this function must close response manually
    public Response post() throws IOException {
        return okHttpUtils.post(url, requestBody, headers);
    }

    public void postNoResponse() throws IOException {
        Response response = null;
        try {
            response = okHttpUtils.post(url, requestBody, headers);
        } finally {
            if(null != response) {
                response.close();
            }
        }
    }

    public byte[] postBytes() throws IOException {
        Response response = okHttpUtils.post(url, requestBody, headers);
        return null == response ? null : response.body().bytes();
    }

    public String postString() throws IOException {
        Response response = okHttpUtils.post(url, requestBody, headers);
        return null == response ? null : response.body().string();
    }

    //  this function must close response manually
    public InputStream postStream() throws IOException {
        Response response = okHttpUtils.post(url, requestBody, headers);
        return null == response ? null : response.body().byteStream();
    }

    //  get
    public <T> T get(ParameterTypeReference<T> parameterTypeReference) throws IOException {
        try (Response response = get()) {
            return null == response.body() ? null : gson.fromJson(response.body().charStream(), parameterTypeReference.getType());
        }
    }

    public Response get() throws IOException {
        return okHttpUtils.get(url, headers);
    }

    public void getNoResponse() throws IOException {
        Response response = null;
        try {
            response = okHttpUtils.get(url, headers);
        } finally {
            if(null != response) {
                response.close();
            }
        }
    }

    public byte[] getBytes() throws IOException {
        Response response = okHttpUtils.get(url, headers);
        return null == response ? null : response.body().bytes();
    }

    public String getString() throws IOException {
        Response response = okHttpUtils.get(url, headers);
        return null == response ? null : response.body().string();
    }

    //  this function must close response manually
    public InputStream getStream() throws IOException {
        Response response = okHttpUtils.get(url, headers);
        return null == response ? null : response.body().byteStream();
    }

    //  put
    public <T> T put(ParameterTypeReference<T> parameterTypeReference) throws IOException {
        try (Response response = put()) {
            return null == response.body() ? null : gson.fromJson(response.body().charStream(), parameterTypeReference.getType());
        }
    }

    //  this function must close response manually
    public Response put() throws IOException {
        return okHttpUtils.put(url, requestBody, headers);
    }

    public void putNoResponse() throws IOException {
        Response response = null;
        try {
            response = okHttpUtils.put(url, requestBody, headers);
        } finally {
            if(null != response) {
                response.close();
            }
        }
    }

    public byte[] putBytes() throws IOException {
        Response response = okHttpUtils.put(url, requestBody, headers);
        return null == response ? null : response.body().bytes();
    }

    public String putString() throws IOException {
        Response response = okHttpUtils.put(url, requestBody, headers);
        return null == response ? null : response.body().string();
    }

    //  this function must close response manually
    public InputStream putStream() throws IOException {
        Response response = okHttpUtils.put(url, requestBody, headers);
        return null == response ? null : response.body().byteStream();
    }

    //  delete
    public <T> T delete(ParameterTypeReference<T> parameterTypeReference) throws IOException {
        try (Response response = delete()) {
            return null == response.body() ? null : gson.fromJson(response.body().charStream(), parameterTypeReference.getType());
        }
    }

    //  this function must close response manually
    public Response delete() throws IOException {
        return okHttpUtils.delete(url, requestBody, headers);
    }

    public void deleteNoResponse() throws IOException {
        Response response = null;
        try {
            response = okHttpUtils.delete(url, requestBody, headers);
        } finally {
            if(null != response) {
                response.close();
            }
        }
    }

    public byte[] deleteBytes() throws IOException {
        Response response = okHttpUtils.delete(url, requestBody, headers);
        return null == response ? null : response.body().bytes();
    }

    public String deleteString() throws IOException {
        Response response = okHttpUtils.delete(url, requestBody, headers);
        return null == response ? null : response.body().string();
    }

    //  this function must close response manually
    public InputStream deleteStream() throws IOException {
        Response response = okHttpUtils.delete(url, requestBody, headers);
        return null == response ? null : response.body().byteStream();
    }

    enum BodyType {
        TEXT("text/plain"),
        STREAMS("octet-stream"),
        JSON("application/json"),
        PROTOBUF("application/x-protobuf");

        private String type;

        BodyType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    class DefaultHttpClientSetting {

        public static final long CONNECT_TIME_OUT = 20;
        public static final long READ_TIME_OUT = 20;
        public static final long WRITE_TIME_OUT = 20;
        public static final int OK_HTTP_RETRY_TIMES = 1;
    }
}
