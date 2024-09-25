package com.emby.action;

import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import com.emby.annotation.Path;
import com.emby.util.EmbyUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Path("/views")
public class ViewsAction implements BaseAction {
    @Override
    public void doAction(HttpServerRequest request, HttpServerResponse response) throws IOException {
        resultSuccess(EmbyUtil.getViews());
    }
}
