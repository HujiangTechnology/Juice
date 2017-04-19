package com.hujiang.juice.service.support;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Created by xujia on 16/12/2.
 */

@Slf4j
@Data
public class Support {
    @NotNull
    private final String resourceRole;

    public Support(
            @NotNull final String resourceRole
    ) {
        this.resourceRole = resourceRole;
    }
}
