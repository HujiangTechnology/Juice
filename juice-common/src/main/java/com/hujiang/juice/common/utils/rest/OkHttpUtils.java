package com.hujiang.juice.common.utils.rest;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by xujia on 16/6/8.
 * <p>
 * config in startup.properties :
 * ok.http.time.out=XX(default = 10)
 * ok.http.retry.times=X(default = 3)
 * if u don't wanner default value
 */

@Slf4j
@Data
public class OkHttpUtils {

    private static OkHttpUtils instance;

    private OkHttpClient okHttpClient;
    private OkHttpClient okHttpsClient;

    private long connectTimeOut;
    private long readTimeOut;
    private long writeTimeOut;
    private int retrys;


    private OkHttpClient.Builder getBuilder(long connectTimeOut, long readTimeOut, long writeTimeOut, int retrys) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return builder.connectTimeout(connectTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)
                .writeTimeout(writeTimeOut, TimeUnit.SECONDS)
                .addInterceptor(
                        new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                // try the request
                                Request request = chain.request();
                                Response response = chain.proceed(request);
                                int tryCount = 0;
                                while (!response.isSuccessful() &&
                                        tryCount < retrys) {
                                    response.close();
                                    response = chain.proceed(request);
                                    tryCount++;
                                }

                                return response;
                            }
                        }
                );
    }

    private OkHttpUtils(long connectTimeOut, long readTimeOut, long writeTimeOut, int retrys) {
        OkHttpClient.Builder builder = getBuilder(connectTimeOut, readTimeOut, writeTimeOut, retrys);

        this.connectTimeOut = connectTimeOut;
        this.readTimeOut = readTimeOut;
        this.writeTimeOut = writeTimeOut;
        this.retrys = retrys;

        okHttpClient = builder.build();
        try {
            ssl(builder);
            okHttpsClient = builder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static OkHttpUtils getInstance(long connectTimeOut, long readTimeOut, long writeTimeOut, int retrys) {
        if(null == instance) {
            synchronized (OkHttpUtils.class) {
                if(null == instance) {
                    instance = new OkHttpUtils(connectTimeOut, readTimeOut, writeTimeOut, retrys);
                }
            }
        }
        return instance;
    }

    private boolean checkSSL(String urlStr) throws MalformedURLException {
        URL url = new URL(urlStr);
        return "https".equalsIgnoreCase(url.getProtocol());
    }

    private void ssl(OkHttpClient.Builder builder) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSL");

        sc.init(null, new TrustManager[]{
                trustManager
        }, new SecureRandom());
        builder.sslSocketFactory(sc.getSocketFactory(), trustManager).hostnameVerifier(hostnameVerifier);
    }

    private static final X509TrustManager trustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    private static final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    };


    public Response put(String url, RequestBody requestBody, Map<String, String> headerMaps) throws IOException {
        return doRequest("PUT", url, requestBody, headerMaps);
    }

    public Response post(String url, RequestBody requestBody, Map<String, String> headerMaps) throws IOException {
        return doRequest("POST", url, requestBody, headerMaps);
    }

    public Response delete(String url, RequestBody requestBody, Map<String, String> headerMaps) throws IOException {
        return doRequest("DELETE", url, requestBody, headerMaps);
    }

    public Response get(String url, Map<String, String> headerMaps) throws IOException {
        return doRequest("GET", url, null, headerMaps);
    }


    public Response doRequest(String type, String url, RequestBody body, Map<String, String> headerMaps) throws IOException {
        return doOkRequest(type, url, body, headerMaps);
    }


    private OkHttpClient getClient(String url) throws MalformedURLException {
        if(checkSSL(url)) {
            return okHttpsClient;
        }
        return okHttpClient;
    }
    /**
     * @param type       request type: get, put, post or delete
     * @param url
     * @param body
     * @param headerMaps
     * @return
     * @throws IOException
     */
    public Response doOkRequest(String type, String url, RequestBody body, Map<String, String> headerMaps) throws IOException {
        Response response = null;
        try {

            Request.Builder builder = new Request.Builder().url(url);

            if (null != headerMaps && headerMaps.size() > 0)
                builder.headers(Headers.of(headerMaps));

            switch (type) {
                case "PUT":
                case "POST":
                case "DELETE":
                case "PATCH":
                    if(null == body) {
                        body = RequestBody.create(null, new byte[0]);
                    }

                    builder.method(type, body);
                    break;
                case "GET":
                case "HEAD":
                    builder.method(type, null);
                    break;
            }
            Request request = builder.build();

            OkHttpClient client = getClient(url);
            response = client.newCall(request).execute();

            if (!response.isSuccessful()) {

                int code = response.code();
                StringBuilder message = new StringBuilder();
                message.append(response.message());
                response.close();

                log.warn("Unexpected code :" + code + ", cause : " + message.toString());
                throw new IOException(message.toString());
            }
            return response;
        } catch (IOException e) {
            if (null != response) {
                response.close();
            }
            throw e;
        }
    }
}
