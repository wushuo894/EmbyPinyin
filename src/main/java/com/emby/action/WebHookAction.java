package com.emby.action;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import com.emby.annotation.Path;
import com.emby.util.EmbyUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Path("/web_hook")
public class WebHookAction implements BaseAction {

    @Override
    public void doAction(HttpServerRequest req, HttpServerResponse res) throws IOException {
        EmbyUtil.getAdmin();
        JsonObject jsonObject = gson.fromJson(req.getBody(), JsonObject.class);

        log.info("webhook: {}", jsonObject);

        JsonElement item = jsonObject.get("Item");
        if (Objects.isNull(item)) {
            resultError();
            return;
        }

        JsonObject itemAsJsonObject = item.getAsJsonObject();
        ThreadUtil.execute(() -> {
            if (EmbyUtil.pinyin(itemAsJsonObject)) {
                log.info("done");
            } else {
                log.error("error");
            }
        });
        resultSuccess();
    }
}
