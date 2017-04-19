package com.hujiang.juice.common.utils.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  64位ID (42(毫秒)+10(机器随机数)+12(重复累加))
 * Created by xujia on 2017/03/21.
 */
public class IdGenUtil {
    private final static long twepoch = 1288834974657L;
    //  序列ID(12位)   1-12
    private final static long SEQUENCE_OFFSET = 0L;
    private final static long SEQUENCE_LENGTH = 12L;

    //  机器ID(10位)    13-22
    private final static long MACHINE_OFFSET = SEQUENCE_OFFSET + SEQUENCE_LENGTH;
    private final static long MACHINE_LENGTH = 10L;

    //  时间戳ID(42位)  23-64
    private final static long TIMESTAMP_OFFSET = MACHINE_OFFSET + MACHINE_LENGTH;

    private final static long machineId = Math.abs(UUID.randomUUID().hashCode() & 0X3FF);
    private static AtomicLong sequence = new AtomicLong(0);

    @Deprecated
    public static long genId(long bizId){
        return genId();
    }

    public static long genId(){

        //        System.out.println("sequence : " + Long.toBinaryString(sequence.get() & 0x3ff));
        //        System.out.println("machineId : " + Long.toBinaryString(machineId << 10));
        //        System.out.println("bizId : " + Long.toBinaryString(bizId << 15));
        //        System.out.println("time : " + Long.toBinaryString((System.currentTimeMillis() - twepoch) << 22));

        // ID偏移组合生成最终的ID，并返回ID
        return   ((System.currentTimeMillis() - twepoch) << TIMESTAMP_OFFSET)
                | (machineId << MACHINE_OFFSET)
                | (sequence.getAndIncrement() & 0XFFF);
    }

    public static void main(String[] args){

        Map<Long, List<Long>> amap = Maps.newHashMap();
        int i = 0;
        while(i < 500000) {
            i++;
            long id = genId();

            if(amap.containsKey(id)){
                amap.get(id).add(sequence.get());

            } else {
                List<Long> longList = Lists.newArrayList();
                longList.add(sequence.get());
                amap.put(id, longList);
            }
        }

        amap.entrySet().stream().filter(v -> v.getValue().size() > 1).forEach(v -> {System.out.println("key : " + v.getKey() + ", value : " + v.getValue());});
        System.out.println("size : " + amap.entrySet().stream().filter(v -> v.getValue().size() > 1).count());


        //        IdGenUtil gen = new IdGenUtil(4095);
        //        System.out.println(Long.toBinaryString(gen.nextId()));
    }
}
