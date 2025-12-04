package com.emby.action;

import cn.hutool.http.Method;
import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import com.emby.annotation.Path;
import com.emby.entity.Log;
import com.emby.util.LogUtil;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Path("/logs")
public class LogsAction implements BaseAction {

    private static final List<Log> LOGS = LogUtil.LOGS;

    @Override
    @Synchronized("LOGS")
    public void doAction(HttpServerRequest req, HttpServerResponse res) {
        String method = req.getMethod();
        if (Method.DELETE.name().equals(method)) {
            LOGS.clear();
            log.info("清理日志");
            resultSuccess();
            return;
        }
        resultSuccess(LOGS);
    }
}
