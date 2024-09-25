package com.emby.action;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.http.server.HttpServerResponse;
import cn.hutool.http.server.action.Action;
import com.emby.entity.Result;
import com.emby.util.ServerUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Objects;

public interface BaseAction extends Action {
    Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    default <T> T getBody(Class<T> tClass) {
        return gson.fromJson(ServerUtil.REQUEST.get().getBody(), tClass);
    }

    default <T> void resultSuccess() {
        result(Result.success());
    }

    default <T> void resultSuccess(T t) {
        result(Result.success(t));
    }

    default <T> void resultSuccessMsg(String t, Object... argArray) {
        result(Result.success().setMessage(StrFormatter.format(t, argArray)));
    }

    default <T> void resultError() {
        result(Result.error());
    }

    default <T> void resultError(T t) {
        result(Result.error(t));
    }

    default <T> void resultErrorMsg(String t, Object... argArray) {
        result(Result.error().setMessage(StrFormatter.format(t, argArray)));
    }

    default <T> void result(Result<T> result) {
        staticResult(result);
    }


    static <T> void staticResult(Result<T> result) {
        HttpServerResponse httpServerResponse = ServerUtil.RESPONSE.get();
        if (Objects.isNull(httpServerResponse)) {
            return;
        }
        httpServerResponse.setContentType("application/json; charset=utf-8");
        String json = gson.toJson(result);
        IoUtil.writeUtf8(httpServerResponse.getOut(), true, json);
    }
}
