package com.hujiang.juice.service.driver;

import com.google.common.collect.Maps;
import com.hujiang.juice.common.exception.CacheException;
import com.hujiang.juice.common.utils.rest.Restty;
import com.hujiang.juice.service.exception.UnrecoverException;
import com.hujiang.juice.service.support.Support;
import com.hujiang.juice.common.vo.TaskResult;
import com.hujiang.juice.service.exception.DriverException;
import com.hujiang.juice.service.model.Host;
import com.hujiang.juice.service.model.SchedulerCalls;
import com.hujiang.juice.service.service.AuxiliaryService;
import com.hujiang.juice.service.service.SchedulerService;
import com.hujiang.juice.service.utils.SendUtils;
import com.hujiang.juice.service.utils.protocol.Protobuf;
import com.hujiang.juice.service.utils.protocol.Protocol;
import com.hujiang.juice.service.utils.zookeeper.CuratorUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.mesos.v1.scheduler.Protos;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.hujiang.juice.common.vo.TaskResult.Result.ERROR;
import static com.hujiang.juice.service.config.JUICE.*;
import static org.apache.mesos.v1.Protos.*;

/**
 * Created by xujia on 16/11/22.
 */


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class SchedulerDriver {
    private FrameworkID frameworkId;
    private CuratorUtils curatorUtils;
    private volatile Host host = new Host();

    private Support support;
    private Protocol protocol;
    private volatile String streamId;
    private final List<OfferID> declines = newArrayList();
    private final Map<String, Set<String>> attrMap = Maps.newHashMap();

    private final Map<Long, String> killMap = Maps.newConcurrentMap();

    public String getUrl() {
        return host.getUrl();
    }


    public SchedulerDriver() {
        protocol = new Protobuf();
        support = new Support(MESOS_FRAMEWORK_ROLE);
        if (StringUtils.isNotBlank(MESOS_SCHEDULER_END_POINT_ZK)) {
            curatorUtils = new CuratorUtils(MESOS_SCHEDULER_END_POINT_ZK, host);
            curatorUtils.init();
            log.info("service is initializing and running by curator(zookeeper)!");
        } else {
            log.error("mesos host is not config, service will down!");
            System.exit(-1);
        }
        log.info("host : " + host.getHost());
    }

    public void run() {

        //  get framework id
        frameworkId = SchedulerService.getFrameworkId();

        //  start a new thread to listen task management list
        log.info("start juice auxiliary service");
        AuxiliaryService.start(this);

        try {
            log.info("start juice service");
            while (true) {
                try {
                    connecting();
                } catch (Exception e) {
                    if (e instanceof UnrecoverException) {
                        log.error("server will recover now, cause : " + e);
                        reset(((UnrecoverException) e).isResetFrameworkId());
                    }

                    log.error("server will restart after 30s due to : " + e);
                    try {
                        Thread.sleep(30 * 1000L);
                    } catch (InterruptedException e1) {
                        log.warn(e1.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    private void connecting() throws Exception {
        InputStream stream = null;
        Response res = null;

        try {
            Protos.Call call = subscribeCall();
            res = Restty.create(getUrl())
                    .addAccept(protocol.mediaType())
                    .addMediaType(protocol.mediaType())
                    .addKeepAlive()
                    .requestBody(protocol.getSendBytes(call))
                    .post();

            streamId = res.header(STREAM_ID);
            stream = res.body().byteStream();
            log.info("send subscribe, frameworkId : " + frameworkId + " , url " + getUrl() + ", streamId : " + streamId);
            log.debug("subscribe call : " + call);
            if (null == stream) {
                log.warn("stream is null");
                throw new DriverException("stream is null");
            }
            while (true) {
                int size = SendUtils.readChunkSize(stream);
                byte[] event = SendUtils.readChunk(stream, size);

                onEvent(event);
            }
        } catch (Exception e) {
            log.error("service handle error, due to : " + e);
            throw e;
        } finally {
            if (null != stream) {
                stream.close();
            }
            if (null != res) {
                res.close();
            }
            streamId = null;
        }
    }

    private void reset(boolean isResetFrameworkId) {
        if (isResetFrameworkId) {
            String removingId = frameworkId.getValue();
            try {
                killMap.clear();
                declines.clear();
                attrMap.clear();
                AuxiliaryService.loggedErrors();
                AuxiliaryService.getSendErrors().clear();
                SchedulerService.removeFrameworkId();
            } catch (CacheException ex) {
                log.error("remove framework id : " + removingId + " from db error!");
            }
            AuxiliaryService.loggedErrors();
            frameworkId = SchedulerService.genFrameworkId();
        }
    }

    private Protos.Call subscribeCall() {

        String hostName = System.getenv("HOST");
        if (StringUtils.isBlank(hostName)) {
            try {
                hostName = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                hostName = "unknown host";
                e.printStackTrace();
            }
        }

        return SchedulerCalls.subscribe(
                FrameworkInfo.newBuilder()
                        .setId(frameworkId)
                        .setHostname(hostName)
                        .setUser(Optional.ofNullable(System.getenv("user")).orElse("root"))
                        .setName(MESOS_SCHEDULER_NAME)
                        .setFailoverTimeout(FRAMEWORK_FAILOVER_TIMEOUT)
                        .setRole(MESOS_FRAMEWORK_ROLE)
                        .build());
    }

    private void onEvent(byte[] bytes) {

        Protos.Event event = null;
        try {
            event = (Protos.Event) protocol.getEvent(bytes, Protos.Event.class);
        } catch (Exception e) {
            log.warn("parser event error, raw event : " + new String(bytes));
            throw new DriverException("parser event error!");
        }

        log.debug("event type : " + event.getType());
        switch (event.getType()) {
            case SUBSCRIBED:
                Protos.Event.Subscribed subscribed = event.getSubscribed();
                SchedulerService.subscribed(subscribed, frameworkId);
                break;
            case OFFERS:
                try {
                    long start = System.currentTimeMillis();
                    event.getOffers().getOffersList().stream()
                            .filter(of -> {
                                if (SchedulerService.filterAndAddAttrSys(of, attrMap)) {
                                    return true;
                                }
                                declines.add(of.getId());
                                return false;
                            })
                            .forEach(
                                    of -> {
                                        List<TaskInfo> tasks = newArrayList();
                                        String offerId = of.getId().getValue();
                                        try {
                                            SchedulerService.handleOffers(killMap, support, of, attrMap.get(offerId), declines, tasks);
                                        } catch (Exception e) {
                                            declines.add(of.getId());
                                            tasks.forEach(
                                                    t -> {
                                                        AuxiliaryService.getTaskErrors()
                                                                .push(new TaskResult(com.hujiang.juice.common.model.Task.splitTaskNameId(t.getTaskId().getValue())
                                                                        , ERROR, "task failed due to exception!"));
                                                    }
                                            );
                                            tasks.clear();
                                        }
                                        if (tasks.size() > 0) {
                                            AuxiliaryService.acceptOffer(protocol, streamId, of.getId(), frameworkId, tasks, getUrl());
                                        }
                                    }
                            );

                    if (declines.size() > 0) {
                        AuxiliaryService.declineOffer(protocol, streamId, frameworkId, SchedulerCalls.decline(frameworkId, declines), getUrl());
                    }
                    long end = System.currentTimeMillis();
                    log.debug("accept --> used time : " + (end - start) + " ms");
                } finally {
                    declines.clear();
                    attrMap.clear();
                }
                break;
            case UPDATE:
                TaskStatus status = event.getUpdate().getStatus();
                if (status.hasUuid()) {
                    SchedulerService.update(killMap, status, protocol, frameworkId, streamId, getUrl());
                    try {
                        SendUtils.sendCall(acknowledgeCall(status), protocol, streamId, getUrl());
                    } catch (Exception e) {
                        log.warn("send acknowledge call error!");
                        throw new DriverException(e);
                    }
                }
                break;
            case MESSAGE:
                Protos.Event.Message message = event.getMessage();
                SchedulerService.message(message.getAgentId(), message.getData().toByteArray());
                break;
            case ERROR:
                SchedulerService.error(event);
            case RESCIND:
            case FAILURE:
            case HEARTBEAT:
                break; // ignore
            default:
                log.warn("Unsupported event : " + event);
                throw new DriverException("Unsupported event : " + event);
        }
    }


    private Protos.Call acknowledgeCall(TaskStatus status) {
        return SchedulerCalls.ackUpdate(frameworkId, status.getUuid(), status.getAgentId(), status.getTaskId());
    }
}
