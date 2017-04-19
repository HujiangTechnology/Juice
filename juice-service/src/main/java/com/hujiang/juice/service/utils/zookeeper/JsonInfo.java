package com.hujiang.juice.service.utils.zookeeper;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by xujia on 17/3/14.
 */

@Data
public class JsonInfo {
    @Data
    public class PathObject {
        Address address;
        String hostname;
        String id;
        long ip;
        String pid;
        int port;
        String version;
        public String getConnectString() {
            return address.getConnectString();
        }
    }

    @Data
    public class Address {
        String hostname;
        String ip;
        int port;

        public String getConnectString() {
            if(StringUtils.isNotBlank(ip) && port > 0) {
                return ip + ":" + port;
            }
            return null;
        }
    }
}
