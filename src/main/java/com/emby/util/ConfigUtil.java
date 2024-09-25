package com.emby.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.Scheduler;
import com.emby.entity.Config;
import com.emby.entity.Views;
import com.emby.task.PinyinTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConfigUtil {
    public static final Config CONFIG = new Config();
    public static final Scheduler SCHEDULER = CronUtil.getScheduler();

    static {
        CONFIG.setHost("")
                .setKey("")
                .setDebug(false)
                .setCron(false)
                .setCronStr("0 1 * * *")
                .setCronIds(new ArrayList<>());
    }

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    /**
     * 获取设置文件夹
     *
     * @return
     */
    public static File getConfigDir() {
        Map<String, String> env = System.getenv();
        String config = env.getOrDefault("CONFIG", "config");
        return new File(config).getAbsoluteFile();
    }

    /**
     * 获取设置文件
     *
     * @return
     */
    public static File getConfigFile() {
        File configDir = getConfigDir();
        return new File(configDir + File.separator + "config.json");
    }

    /**
     * 加载设置
     */
    public static synchronized void load() {
        File configFile = getConfigFile();

        if (!configFile.exists()) {
            FileUtil.writeUtf8String(GSON.toJson(CONFIG), configFile);
        }
        log.debug("加载配置文件 {}", configFile);
        String s = FileUtil.readUtf8String(configFile);
        BeanUtil.copyProperties(GSON.fromJson(s, Config.class), CONFIG, CopyOptions
                .create()
                .setIgnoreNullValue(true));
        LogUtil.loadLogback();


        Boolean cron = CONFIG.getCron();
        String cronStr = CONFIG.getCronStr();
        if (!cron) {
            log.info("定时任务未开启");
            return;
        }
        if (SCHEDULER.isStarted()) {
            SCHEDULER.stop(true);
        }
        try {
            SCHEDULER.schedule(cronStr, new Runnable() {
                @Override
                public void run() {
                    List<String> cronIds = CONFIG.getCronIds();
                    EmbyUtil.getAdmin();
                    if (StrUtil.isBlank(EmbyUtil.ADMIN_USER_ID)) {
                        return;
                    }
                    List<Views> views = EmbyUtil.getViews();
                    views = views.stream().filter(it -> cronIds.contains(it.getId())).toList();
                    PinyinTask pinyinTask = new PinyinTask();
                    pinyinTask.setViewsList(views);
                    ThreadUtil.execute(pinyinTask);
                }
            });
            SCHEDULER.start();
            log.info("定时任务已开启 {}", cronStr);
        } catch (Exception e) {
            log.error("定时任务启动失败");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将设置保存到磁盘
     */
    public static synchronized void sync() {
        File configFile = getConfigFile();
        log.debug("保存配置 {}", configFile);
        try {
            String json = GSON.toJson(CONFIG);
            // 校验json没有问题
            GSON.fromJson(json, Config.class);
            FileUtil.writeUtf8String(json, configFile);
            LogUtil.loadLogback();
            log.debug("保存成功 {}", configFile);
        } catch (Exception e) {
            log.error("保存失败 {}", configFile);
            log.error(e.getMessage(), e);
        }
    }
}
