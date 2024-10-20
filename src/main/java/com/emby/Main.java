package com.emby;

import com.emby.util.ConfigUtil;
import com.emby.util.MavenUtil;
import com.emby.util.ServerUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            ConfigUtil.load();
            ServerUtil
                    .create(args)
                    .start();
            String version = MavenUtil.getVersion();
            log.info("version {}", version);
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(ServerUtil::stop));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
