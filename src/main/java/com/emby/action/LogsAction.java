package com.emby.action;

import cn.hutool.http.Method;
import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import com.emby.annotation.Path;
import com.emby.entity.Log;
import com.emby.util.LogUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Path("/logs")
public class LogsAction implements BaseAction {

    @Override
    public void doAction(HttpServerRequest req, HttpServerResponse res) {
        String method = req.getMethod();
        List<Log> logs = LogUtil.LOGS;
        if (Method.DELETE.name().equals(method)) {
            logs.clear();
            log.info("清理日志");
            resultSuccess();
            return;
        }
        resultSuccess(logs);
    }
}
