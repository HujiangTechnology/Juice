package com.hujiang.juice.service.utils;

import com.hujiang.juice.common.model.Resources;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Created by xujia on 16/12/3.
 */

@Data
public class ResourcesUtils {

    private int availableCpus;
    private int availableMems;
    private String role;

    public ResourcesUtils(double availableCpus, double availableMems, double threshold, String role) {
        this.availableCpus = (int)(availableCpus * threshold);
        this.availableMems = (int)(availableMems * threshold);
        this.role = role;
    }

    public boolean allocating(@NotNull Resources resources) {
        if(resources.getCpu() <= availableCpus && resources.getMem() <= availableMems) {
            availableCpus -= resources.getCpu();
            availableMems -= resources.getMem();
            return true;
        }
        return false;
    }

    public boolean isAvailable(){
        return availableCpus >= 0 && availableMems >= 0;
    }
}
