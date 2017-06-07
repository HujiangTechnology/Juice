package com.hujiang.juice.service.utils.zookeeper;

import com.hujiang.juice.common.utils.CommonUtils;
import com.hujiang.juice.service.exception.DriverException;
import com.hujiang.juice.service.model.Host;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hujiang.juice.service.config.JUICE.*;


/**
 * Created by xujia on 17/3/13.
 */

@Slf4j
@Data
public class CuratorUtils {

    private Host host;
    private volatile String path;
    private CuratorFramework client;
    private ExecutorService pool;
    private NodeCache nodeCache;


    public String generatePath() throws Exception {
        return client.getChildren().forPath(MESOS_ROOT_PATH).stream().filter(ch -> ch.startsWith(PATH_NAME)).sorted().findFirst().map(this::getFullPath).orElse("");
    }

    private String getFullPath(String pathName) {
        return MESOS_ROOT_PATH + HTTP_SEPERATOR + pathName;
    }

    public CuratorUtils(String connectString, Host host) {
        this.host = host;

        client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(10000)
                .connectionTimeoutMs(10000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
        try {
            path = generatePath();
            if (null == path) {
                log.error("init curator failed due to path is null!");
                throw new DriverException("init curator failed due to path is null, servie will down");
            }
        } catch (Exception e) {
            log.error("CuratorUtils construct failed, service will down!");
            client.close();
            System.exit(-1);
        }
    }

    private void childrenCacheListenable() throws Exception {
        final PathChildrenCache childrenCache = new PathChildrenCache(client, MESOS_ROOT_PATH, true);
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        childrenCache.getListenable().addListener(
                new PathChildrenCacheListener() {
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
                            throws Exception {
                        switch (event.getType()) {
                            case CHILD_ADDED:
                                added();
                                break;
                            case CHILD_REMOVED:
                                removed(event.getData().getPath());
                                break;
                            case CHILD_UPDATED:
                                updated(event.getData().getPath());
                                break;
                            default:
                                break;
                        }
                    }
                },
                pool
        );
    }

    private void added() {
        try {
            if (StringUtils.isBlank(path)) {
                String tmpPath = generatePath();
                if (StringUtils.isNotBlank(tmpPath)) {
                    path = tmpPath;
                }
                setHost();
                log.info("path changed to : " + path);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void removed(String changedPath) {
        if (StringUtils.isNotBlank(changedPath) && path.equals(changedPath)) {
            try {
                path = generatePath();
                if (StringUtils.isNotBlank(path)) {
                    log.warn("path changed to  : " + path);
                    setHost();
                }
            } catch (Exception e) {
                path = "";
                log.error("path removed : " + changedPath);
                log.error(e.getMessage());
            }
        }
    }

    private void updated(String changedPath) {
        try {
            if(StringUtils.isBlank(path) || path.equals(changedPath)) {
                setHost();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    private void setHost() {
        try {
            if (StringUtils.isNotBlank(path)) {
                String connectString = getConnectString();
                if (StringUtils.isNotBlank(connectString) && (StringUtils.isBlank(host.getHost()) || !host.getHost().equals(connectString))) {
                    host.setHost(connectString);
                    log.info("path update, update mesos host to : " + host.getHost());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    private String getConnectString() throws Exception {
        String connection = getLeaderConnectString();
        if (StringUtils.isNotBlank(connection)) {
            log.info("connection -> " + connection);
            return CommonUtils.fixUrl(connection);
        }
        return null;
    }

    public String getLeaderConnectString() throws Exception {
        String data = getData(path);
        return gson.fromJson(data, JsonInfo.PathObject.class).getConnectString();
    }

    private String getData(String path) throws Exception {
        return new String(client.getData().forPath(path));
    }

    public void init() {
        try {
            pool = Executors.newFixedThreadPool(1);
            childrenCacheListenable();
            Thread.sleep(1000);
            String connectString = getConnectString();
            if (StringUtils.isBlank(connectString)) {
                throw new DriverException("get mesos host from zk error");
            }
            host.setHost(connectString);
        } catch (Exception e) {
            String errMessage = "start curator listen failed, service will down!";
            log.error(errMessage);
            pool.shutdown();
            System.exit(-1);
        }
    }
}
