package com.emby.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Config {
    private String host;
    private String key;
    private Boolean debug;
    private Boolean cron;
    private String cronStr;
    private Boolean isInnerIP;
    private List<String> cronIds;
}
