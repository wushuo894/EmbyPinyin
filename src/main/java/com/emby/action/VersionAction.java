package com.emby.action;

import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import com.emby.annotation.Path;
import com.emby.util.MavenUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/version")
public class VersionAction implements BaseAction {
    @Override
    public void doAction(HttpServerRequest request, HttpServerResponse response) {
        resultSuccess(MavenUtil.getVersion());
    }
}
