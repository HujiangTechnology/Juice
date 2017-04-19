package com.hujiang.juice.common.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.v1.Protos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujia on 16/12/2.
 */

@Data
@Slf4j
public class Command {

    private Environment env;
    private List<String> args;
    private String value;

    public Command() {
        args = new ArrayList<>();
    }

    public Command(String value) {
        this.value = value;
    }

    public Command(String value, Environment env, List<String> args) {
        this.value = value;
        this.env = env;
        this.args = args;
    }

    public Command(Environment env) {
        this.env = env;
        args = new ArrayList<>();
    }

    public Command(Environment env, List<String> args) {
        this.env = env;
        this.args = args;
    }

    public void setEnv(Protos.CommandInfo.Builder builder) {
        if (null != env) {
            builder.setEnvironment(Protos.Environment.newBuilder()
                    .addVariables(Protos.Environment.Variable.newBuilder()
                            .setName(env.getName())
                            .setValue(env.getValue()))
                    .build());
        }
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

        setEnv(builder);

        return builder.build();
    }


    public static Environment newEnvironment(String name, String value) {
        return new Environment(name, value);
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
