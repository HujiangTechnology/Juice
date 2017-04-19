package com.hujiang.juice.common.model;

import lombok.Data;

/**
 * Created by xujia on 17/3/7.
 */

@Data
public class Expire {
    private long firstReservedTimes;
    private int resourceLack;
    private int offerLack;
    public Expire() {
        firstReservedTimes = 0;
        resourceLack = 0;
        offerLack = 0;
    }

    public void incrementResourceLack() {
        resourceLack++;
    }

    public void incrementOfferLack() {
        offerLack++;
    }
}
