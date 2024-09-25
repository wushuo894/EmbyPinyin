package com.emby.action;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import com.emby.annotation.Path;
import com.emby.entity.Views;
import com.emby.task.PinyinTask;
import com.google.gson.JsonArray;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@Path("/pinyin")
public class PinyinAction implements BaseAction {

    @Override
    public synchronized void doAction(HttpServerRequest request, HttpServerResponse response) throws IOException {
        if (PinyinTask.LOCK.isLocked()) {
            resultErrorMsg("请等待现有任务完成");
        }

        List<Views> viewsList = getBody(JsonArray.class).asList().stream().map(o -> gson.fromJson(o, Views.class)).toList();
        PinyinTask pinyinTask = new PinyinTask();
        pinyinTask.setViewsList(viewsList);
        ThreadUtil.execute(pinyinTask);
        resultSuccessMsg("任务已开始");
    }
}
