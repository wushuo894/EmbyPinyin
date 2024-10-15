package com.emby.task;

import com.emby.action.StatusAction;
import com.emby.entity.Status;
import com.emby.entity.Views;
import com.emby.util.EmbyUtil;
import com.google.gson.JsonElement;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Setter
public class PinyinTask implements Runnable {
    public static final ReentrantLock LOCK = new ReentrantLock();

    private List<Views> viewsList = new ArrayList<>();

    @Override
    public void run() {
        if (LOCK.isLocked()) {
            log.error("请等待现有任务完成");
            return;
        }
        EmbyUtil.getAdmin();
        Status status = StatusAction.STATUS;
        status.setCurrent(0L)
                .setTotal(0L);
        LOCK.lock();
        String s = viewsList.stream()
                .map(Views::getName)
                .collect(Collectors.joining(", "));
        log.info(s);
        try {
            List<JsonElement> items = new ArrayList<>();
            status.setLoading(true);
            for (Views views : viewsList) {
                items.addAll(EmbyUtil.getItems(views.getId()).asList());
            }
            status.setLoading(false)
                    .setStart(true);
            for (JsonElement jsonElement : items) {
                Long current = status.getCurrent();
                status.setCurrent(current + 1);
                EmbyUtil.pinyin(jsonElement.getAsJsonObject());
            }
            status.setStart(false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            LOCK.unlock();
            status
                    .setStart(false)
                    .setLoading(false);
        }
    }
}
