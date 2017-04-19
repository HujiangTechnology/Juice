package com.hujiang.juice.common.model;

import lombok.Data;
import org.apache.mesos.v1.Protos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.hujiang.juice.common.model.Docker.NetWork.*;

/**
 * Created by xujia on 17/1/17.
 */

@Data
public class Docker {
    private String image;
    private Boolean forcePullImage;
    private Boolean privileged;
    private String net;
    private List<Parameter> parameters;
    private List<PortMapping> portMappings;


    public Docker(String image){
        this.image = image;
        forcePullImage = true;
        privileged = false;
        net = BRIDGE;
        parameters = new ArrayList<>();
        portMappings = new ArrayList<>();

    }

    public Docker(String image, boolean forcePullImage){
        this.image = image;
        this.forcePullImage = forcePullImage;
        privileged = false;
        net = BRIDGE;
        parameters = new ArrayList<>();
        portMappings = new ArrayList<>();
    }

    public Docker(String image, boolean forcePullImage, String net){
        this.image = image;
        this.forcePullImage = forcePullImage;
        this.net = net;
        privileged = false;
        parameters = new ArrayList<>();
        portMappings = new ArrayList<>();
    }

    public Docker(String image, boolean forcePullImage, boolean privileged, String net, List<Parameter> parameters, List<PortMapping> portMappings) {
        this.image = image;
        this.forcePullImage = forcePullImage;
        this.privileged = privileged;
        this.net = net;
        this.parameters = parameters;
        this.portMappings = portMappings;
    }

    public @NotNull
    Protos.ContainerInfo.DockerInfo protos() {

        Protos.ContainerInfo.DockerInfo.Builder dockerBuild = Protos.ContainerInfo.DockerInfo.newBuilder()
                .setImage(image)
                .setForcePullImage(forcePullImage)
                .setPrivileged(privileged)
                .setNetwork(exchange());


        if (null != parameters) {
            parameters.forEach(
                    par -> {
                        dockerBuild.addParameters(Protos.Parameter.newBuilder().setKey(par.getKey()).setValue(par.getValue()));
                    }
            );
        }
        if (null != portMappings) {
            portMappings.forEach(
                    por -> {
                        dockerBuild.addPortMappings(Protos.ContainerInfo.DockerInfo.PortMapping.newBuilder()
                                .setContainerPort(por.getContainerPort())
                                .setHostPort(por.getHostPort())
                                .setProtocol(por.getProtocol()));
                    }
            );
        }
        return dockerBuild.build();
    }

    public static PortMapping newPortMapping(int containerPort, int hostPort, String protocol) {
        return new PortMapping(containerPort, hostPort, protocol);
    }

    public static Parameter newParameter(String key, String value) {
        return new Parameter(key, value);
    }

    public static Container.Volume newVolume(String containerPath, String hostPath, String dvo) {
        return new Container.Volume(containerPath, hostPath, dvo);
    }

    private Protos.ContainerInfo.DockerInfo.Network exchange() {
        if(null == net) {
            return Protos.ContainerInfo.DockerInfo.Network.BRIDGE;
        }
        switch (net) {
            case HOST: return Protos.ContainerInfo.DockerInfo.Network.HOST;
            case NONE: return Protos.ContainerInfo.DockerInfo.Network.NONE;
            case USER: return Protos.ContainerInfo.DockerInfo.Network.USER;
            default: return Protos.ContainerInfo.DockerInfo.Network.BRIDGE;
        }
    }

    public interface NetWork {
        String BRIDGE = "BRIDGE";
        String HOST = "HOST";
        String NONE = "NONE";
        String USER = "USER";
    }

    @Data
    public static class PortMapping {
        private int containerPort;
        private int hostPort;
        private String protocol;
        public PortMapping(int containerPort, int hostPort, String protocol) {
            this.containerPort = containerPort;
            this.hostPort = hostPort;
            this.protocol = protocol;
        }
    }

    @Data
    public static class Parameter {
        private String key;
        private String value;
        public Parameter(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
