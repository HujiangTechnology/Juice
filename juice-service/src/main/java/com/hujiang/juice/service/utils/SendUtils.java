package com.hujiang.juice.service.utils;

import com.google.protobuf.GeneratedMessage;
import com.hujiang.juice.common.utils.rest.Restty;
import com.hujiang.juice.service.utils.protocol.Protocol;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xujia on 16/11/21.
 */
@Slf4j
public class SendUtils {

    public static void sendCall(GeneratedMessage call, Protocol protocol, String streamId, String url) throws IOException {

        log.debug("[call] " + call);

        Restty restty = Restty.create(url)
                .addAccept(protocol.mediaType())
                .addMediaType(protocol.mediaType())
                .requestBody(protocol.getSendBytes(call));

        if (StringUtils.isNotBlank(streamId)) {
            restty.addHeader("Mesos-Stream-Id", streamId);
        }

        try {
            restty.postNoResponse();
        } catch (IOException e) {
            log.warn("send call to mesos master failed, due to : " + e);
            throw e;
        }

    }


    public static int readChunkSize(InputStream stream) throws IOException {
        byte b;

        String s = "";
        while ((b = (byte) stream.read()) != '\n')
            s += (char) b;

        return Integer.parseInt(s);
    }

    public static byte[] readChunk(InputStream stream, int size) throws IOException {
        byte[] buffer = new byte[size];

        for (int i = 0; i < size; i++)
            buffer[i] = (byte) stream.read();

        return buffer;
    }
}
