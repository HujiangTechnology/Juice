package com.hujiang.juice.common.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.v1.Protos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xujia on 16/12/2.
 */

@Data
@Slf4j
public class Command {

    private List<Environment> envs;
    private List<String> args;
    private List<String> uris;
    private String value;

    public Command(String value, List<Environment> envs, List<String> args, List<String> uris) {
        this.value = value;
        this.envs = envs;
        this.args = args;
        this.uris = uris;
    }

    public @NotNull Protos.CommandInfo protos(boolean isShell) {

        Protos.CommandInfo.Builder builder = Protos.CommandInfo.newBuilder();
        if(isShell) {
            builder.setValue(value);
        } else {
            builder.setShell(false);
            if (null != args && !args.isEmpty()) {
                builder.addAllArguments(args);
            }
        }

        if(null != uris && !uris.isEmpty()) {
            builder.addAllUris(uris.stream().map(uri -> Protos.CommandInfo.URI.newBuilder().setValue(uri).build()).collect(Collectors.toList()));
        }

        if(null != envs && !envs.isEmpty()) {
            Protos.Environment.Builder envBuilder = Protos.Environment.newBuilder();
            envBuilder.addAllVariables(envs.stream().map(env -> Protos.Environment.Variable.newBuilder().setName(env.getName()).setValue(env.getValue()).build()).collect(Collectors.toList()));
            builder.setEnvironment(envBuilder).build();
        }

        return builder.build();
    }

    @Data
    public static class Environment {
        private String name;
        private String value;

        public Environment(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
