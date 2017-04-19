package com.hujiang.juice.common.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.v1.Protos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.hujiang.juice.common.model.Container.Volume.DVO.*;
import static com.hujiang.juice.common.model.Container.TYPE.*;

/**
 * Created by xujia on 16/12/2.
 */
@Data
@Slf4j
public class Container {

    private Docker docker;
    private String type;
    private List<Volume> volumes;

    public Container(Docker docker) {
        this.docker = docker;
        this.type = TYPE.DOCKER;
    }

    public Container(Docker docker, List<Volume> volumes) {
        this.type = TYPE.DOCKER;
        this.docker = docker;
        this.volumes = volumes;
    }

    public Container(Docker docker, String type, List<Volume> volumes) {
        this.type = type;
        this.docker = docker;
        this.volumes = volumes;
    }

    public @NotNull Protos.ContainerInfo protos() {

        Protos.ContainerInfo.Builder builder
                = Protos.ContainerInfo.newBuilder()
                .setDocker(docker.protos())
                .setType(getType());

        if(null != volumes && !volumes.isEmpty()) {
            volumes.forEach(
                    volume -> {
                        builder.addVolumes(Protos.Volume.newBuilder()
                                .setContainerPath(volume.getContainerPath())
                                .setHostPath(volume.getHostPath())
                                .setMode(volume.getMode())
                                .build());
                    }
            );
        }

        return builder.build();
    }

    @Data
    public static class Volume {
        private String containerPath;
        private String hostPath;
        private String dvo;

        public Volume(String containerPath, String hostPath, String dvo) {
            this.containerPath = containerPath;
            this.hostPath = hostPath;
            this.dvo = dvo;
        }

        public Protos.Volume.Mode getMode() {
            switch (dvo) {
                case READONLY : return Protos.Volume.Mode.RO;
                default: return Protos.Volume.Mode.RW;
            }
        }

        public interface DVO {
            String READONLY = "RO";
            String READWRITE = "RW";
        }
    }

    public Protos.ContainerInfo.Type getType() {
        switch (type) {
            case MESOS : return Protos.ContainerInfo.Type.MESOS;
            default: return Protos.ContainerInfo.Type.DOCKER;
        }
    }

    public interface TYPE {
        String DOCKER = "DOCKER";
        String MESOS = "MESOS";
    }
}
