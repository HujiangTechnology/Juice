package com.hujiang.juice.service.model;

import com.google.protobuf.ByteString;
import com.hujiang.juice.common.model.TaskManagement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.apache.mesos.v1.Protos.*;
import static org.apache.mesos.v1.scheduler.Protos.Call;

/**
 * Created by xujia on 16/11/22.
 */
public class SchedulerCalls {

    private SchedulerCalls() {}

    @NotNull
    public static Call kill(@NotNull final FrameworkID frameworkId,
                            @NotNull final TaskManagement.TaskAgentRel taskAgentRel
                            ) {
        return Call.newBuilder()
                .setFrameworkId(frameworkId)
                .setType(Call.Type.KILL)
                .setKill(
                        Call.Kill.newBuilder()
                                .setTaskId(taskAgentRel.protosTaskId())
                                .setAgentId(taskAgentRel.protosAgentId())
                                .build()
                ).build();
    }

    @NotNull
    public static Call reconcile(@NotNull final FrameworkID frameworkId,
                            @NotNull final List<TaskManagement.TaskAgentRel> taskAgentRels
    ) {
        Call.Reconcile.Builder builder = Call.Reconcile.newBuilder();
        taskAgentRels.stream().parallel().forEach(
                taskAgentRel -> {
                    builder.addTasks(Call.Reconcile.Task.newBuilder()
                            .setAgentId(taskAgentRel.protosAgentId())
                            .setTaskId(taskAgentRel.protosTaskId())
                            .build());
                }
        );

        return Call.newBuilder()
                .setFrameworkId(frameworkId)
                .setType(Call.Type.RECONCILE)
                .setReconcile(builder)
                .build();
    }

    @NotNull
    public static Call tearDown(@NotNull final FrameworkID frameworkId){
        return Call.newBuilder()
                .setFrameworkId(frameworkId)
                .setType(Call.Type.TEARDOWN)
                .build();
    }
    @NotNull
    public static Call ackUpdate(
            @NotNull final FrameworkID frameworkId,
            @NotNull final ByteString uuid,
            @NotNull final AgentID agentId,
            @NotNull final TaskID taskId
    ) {
        return Call.newBuilder()
                .setFrameworkId(frameworkId)
                .setType(Call.Type.ACKNOWLEDGE)
                .setAcknowledge(
                        Call.Acknowledge.newBuilder()
                                .setUuid(uuid)
                                .setAgentId(agentId)
                                .setTaskId(taskId)
                                .build()
                )
                .build();
    }


    public static @NotNull
    Call accept(
            final @NotNull FrameworkID frameworkId,
            final @NotNull OfferID offerId,
            @NotNull final List<TaskInfo> tasks
    ) {
        return Call.newBuilder()
                .setFrameworkId(frameworkId)
                .setType(Call.Type.ACCEPT)
                .setAccept(
                        Call.Accept.newBuilder()
                                .addOfferIds(offerId)
                                .addOperations(
                                        Offer.Operation.newBuilder()
                                                .setType(Offer.Operation.Type.LAUNCH)
                                                .setLaunch(
                                                        Offer.Operation.Launch.newBuilder()
                                                                .addAllTaskInfos(tasks)
                                                )
                                )
                )
                .build();
    }

    @NotNull
    public static Call decline(@NotNull final FrameworkID frameworkId, @NotNull final List<OfferID> offerIds) {
        return Call.newBuilder()
                .setFrameworkId(frameworkId)
                .setType(Call.Type.DECLINE)
                .setDecline(
                        Call.Decline.newBuilder()
                                .addAllOfferIds(offerIds)
                )
                .build();
    }

    @NotNull
    public static Call subscribe(
            @NotNull final String frameworkId,
            @NotNull final String user,
            @NotNull final String frameworkName,
            final long failoverTimeoutSeconds
    ) {
        final FrameworkID frameworkID = FrameworkID.newBuilder().setValue(frameworkId).build();
        return subscribe(frameworkID, user, frameworkName, failoverTimeoutSeconds);
    }

    @NotNull
    public static Call subscribe(
            @NotNull final FrameworkID frameworkId,
            @NotNull final String user,
            @NotNull final String frameworkName,
            final long failoverTimeoutSeconds
    ) {
        final FrameworkInfo frameworkInfo = FrameworkInfo.newBuilder()
                .setId(frameworkId)
                .setUser(user)
                .setName(frameworkName)
                .setFailoverTimeout(failoverTimeoutSeconds)
                .build();
        return subscribe(frameworkInfo);
    }

    @NotNull
    public static Call subscribe(
            @NotNull final FrameworkInfo frameworkInfo
    ) {
        return Call.newBuilder()
                .setFrameworkId(frameworkInfo.getId())
                .setType(Call.Type.SUBSCRIBE)
                .setSubscribe(
                        Call.Subscribe.newBuilder()
                                .setFrameworkInfo(frameworkInfo)
                )
                .build();
    }
}
