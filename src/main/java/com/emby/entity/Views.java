package com.emby.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Views {
    private String id;
    private String name;
    private Boolean cron;
}
