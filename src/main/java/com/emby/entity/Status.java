package com.emby.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Status {
    private Long total;

    private Long current;

    private Boolean loading;

    private Boolean start;
}
