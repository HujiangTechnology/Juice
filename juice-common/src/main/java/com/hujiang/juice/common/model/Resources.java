package com.hujiang.juice.common.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.mesos.v1.Protos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.hujiang.juice.common.config.COMMON.CPUS;
import static com.hujiang.juice.common.config.COMMON.MEMS;


/**
 * Created by xujia on 16/12/2.
 */

@Data
public class Resources {

    private String role;
    private double cpu;
    private double mem;

    public Resources(double cpu, double mem) {
        this.cpu = cpu;
        this.mem = mem;
        this.role = "*";
    }

    public Resources(double cpu, double mem, String role) {
        this.cpu = cpu;
        this.mem = mem;
        this.role = role;
    }

    private
    @NotNull
    Protos.Resource addResource(@NotNull String name, @NotNull double value) {
        return Protos.Resource.newBuilder()
                .setName(name)
                .setRole(role)
                .setType(Protos.Value.Type.SCALAR)
                .setScalar(Protos.Value.Scalar.newBuilder().setValue(value).build())
                .build();
    }

    public
    @NotNull
    List<Protos.Resource> protos() {
        List<Protos.Resource> resources = new ArrayList<>();
        resources.add(addResource(CPUS, cpu));
        resources.add(addResource(MEMS, mem));
        return resources;
    }

    public void checkSet() {
        if(StringUtils.isBlank(role)) {
            role = "*";
        }
        cpu = getCpu(cpu);
        mem = getMem(mem);
    }

    public static double getCpu(double v) {

        if (v >= CPU_LEVEL.MAX.getLevel()) {
            return CPU_LEVEL.MAX.getLevel();
        }

        if (v <= CPU_LEVEL.MIN.getLevel()) {
            return CPU_LEVEL.MIN.getLevel();
        }

        return v;
    }

    public static double getMem(double v) {

        if (v >= MEM_LEVEL.MAX.getLevel()) {
            return MEM_LEVEL.MAX.getLevel();
        }

        if (v <= MEM_LEVEL.MIN.getLevel()) {
            return MEM_LEVEL.MIN.getLevel();
        }

        return v;
    }

    public enum CPU_LEVEL {
        MIN(0.1),
        MAX(16.0),
        DEFAULT(1.0);

        private double level;

        CPU_LEVEL(double level) {
            this.level = level;
        }

        public double getLevel() {
            return level;
        }
    }

    public enum MEM_LEVEL {
        MIN(256.0),
        MAX(8192.0 * 2),
        DEFAULT(512.0);

        private double level;

        MEM_LEVEL(double level) {
            this.level = level;
        }

        public double getLevel() {
            return level;
        }
    }
}
