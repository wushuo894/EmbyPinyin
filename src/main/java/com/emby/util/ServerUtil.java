package com.emby.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import cn.hutool.http.server.SimpleServer;
import cn.hutool.http.server.filter.HttpFilter;
import cn.hutool.log.Log;
import com.emby.action.BaseAction;
import com.emby.action.RootAction;
import com.emby.annotation.Path;
import com.emby.entity.Config;
import com.emby.entity.Result;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class ServerUtil {
    public static final ThreadLocal<HttpServerRequest> REQUEST = new ThreadLocal<>();
    public static final ThreadLocal<HttpServerResponse> RESPONSE = new ThreadLocal<>();
    public static String PORT = "9198";
    public static SimpleServer server;

    public static SimpleServer create(String... args) {
        Map<String, String> env = System.getenv();
        int i = Arrays.asList(args).indexOf("--port");

        if (i > -1) {
            PORT = args[i + 1];
        }

        PORT = env.getOrDefault("PORT", PORT);

        server = HttpUtil.createServer(Integer.parseInt(PORT));

        server.addAction("/", new RootAction());
        Set<Class<?>> classes = ClassUtil.scanPackage("com.emby.action");
        for (Class<?> aClass : classes) {
            Path path = aClass.getAnnotation(Path.class);
            if (Objects.isNull(path)) {
                continue;
            }
            Object action = ReflectUtil.newInstanceIfPossible(aClass);
            String urlPath = "/api" + path.value();
            server.addFilter(new HttpFilter() {
                @Override
                public void doFilter(HttpServerRequest req, HttpServerResponse res, Filter.Chain chain) throws IOException {
                    Config config = ConfigUtil.CONFIG;
                    Boolean isInnerIP = config.getIsInnerIP();
                    String ip = getIp();
                    if (isInnerIP) {
                        if (!PatternPool.IPV4.matcher(ip).matches()) {
                            res.send404("404 Not Found");
                            return;
                        }
                        if (Ipv4Util.isInnerIP(ip)) {
                            res.send404("404 Not Found");
                            return;
                        }
                    }
                    chain.doFilter(req.getHttpExchange());
                }
            });
            server.addAction(urlPath, new BaseAction() {
                private final Log log = Log.get(aClass);

                @Override
                public void doAction(HttpServerRequest req, HttpServerResponse res) {
                    try {
                        REQUEST.set(req);
                        RESPONSE.set(res);
                        BaseAction baseAction = (BaseAction) action;
                        baseAction.doAction(req, res);
                    } catch (Exception e) {
                        String message = ExceptionUtil.getMessage(e);
                        String json = gson.toJson(Result.error().setMessage(message));
                        IoUtil.writeUtf8(res.getOut(), true, json);
                        if (!(e instanceof IllegalArgumentException)) {
                            log.error("{} {}", urlPath, e.getMessage());
                            log.debug(e);
                        }
                    } finally {
                        REQUEST.remove();
                        RESPONSE.remove();
                    }
                }
            });
        }
        return server;
    }

    public static String getIp() {
        try {
            HttpServerRequest request = ServerUtil.REQUEST.get();
            HttpExchange httpExchange = (HttpExchange) ReflectUtil.getFieldValue(request, "httpExchange");
            return httpExchange.getRemoteAddress().getAddress().getHostAddress();
        } catch (Exception e) {
            String message = ExceptionUtil.getMessage(e);
            log.error(message, e);
        }
        return "未知";
    }

    public static void stop() {
        if (Objects.isNull(server)) {
            return;
        }
        server.getRawServer().stop(0);
    }
}
