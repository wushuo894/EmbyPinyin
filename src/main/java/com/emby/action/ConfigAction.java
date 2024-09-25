package com.emby.action;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import com.emby.annotation.Path;
import com.emby.entity.Config;
import com.emby.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Path("/config")
public class ConfigAction implements BaseAction {
    @Override
    public void doAction(HttpServerRequest req, HttpServerResponse res) throws IOException {
        String method = req.getMethod();
        if (method.equals("GET")) {
            resultSuccess(ConfigUtil.CONFIG);
            return;
        }
        if (!method.equals("POST")) {
            resultError();
            return;
        }
        Config config = getBody(Config.class);
        BeanUtil.copyProperties(config, ConfigUtil.CONFIG);
        ConfigUtil.sync();
        ConfigUtil.load();
        resultSuccessMsg("修改成功");
    }
}
