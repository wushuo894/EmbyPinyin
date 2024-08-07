package com.emby;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import com.google.gson.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main implements Runnable {
    public static final Gson gson = new Gson();
    public static String adminUserId = "";
    public static String host = "http://192.168.5.4:8096";
    public static String key = "c30e784137134792b2907b78f5c23b60";
    public static String itemStr = "";
    public static Integer port = 9198;

    public static final Log log = Log.get(Main.class);

    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();

        Map<String, String> envMap = System.getenv();
        Map<String, String> argsMap = CollUtil.split(List.of(args), 2)
                .stream()
                .collect(Collectors.toMap(it -> it.get(0), it -> it.get(1)));

        map.putAll(argsMap);
        map.putAll(envMap);

        boolean run = Boolean.TRUE;

        String cron = "";

        for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
            String k = stringStringEntry.getKey();
            String v = stringStringEntry.getValue();
            if (List.of("-h", "--host", "HOST").contains(k)) {
                host = v;
            }
            if (List.of("-k", "--key", "KEY").contains(k)) {
                key = v;
            }
            if (List.of("-item", "ITEM").contains(k)) {
                itemStr = v;
            }
            if (List.of("-p", "--port", "PORT").contains(k)) {
                port = Integer.parseInt(v);
            }
            if (List.of("-c", "--cron", "CRON").contains(k)) {
                cron = v;
            }
            if (List.of("-r", "--run", "RUN").contains(k)) {
                run = Boolean.parseBoolean(v);
            }
        }

        HttpUtil.createServer(port)
                .addAction("/", (req, res) -> {
                    log.info("Webhooks = {}", req.getBody());
                    try {
                        getAdmin();
                        JsonObject jsonObject = gson.fromJson(req.getBody(), JsonObject.class);
                        JsonElement item = jsonObject.get("Item");
                        JsonElement event = jsonObject.get("Event");
                        if (event.isJsonNull()) {
                            return;
                        }
                        if (!event.getAsString().equals("library.new")) {
                            return;
                        }
                        if (Objects.nonNull(item)) {
                            JsonObject itemAsJsonObject = item.getAsJsonObject();
                            ThreadUtil.execute(() -> {
                                if (pinyin(itemAsJsonObject)) {
                                    log.info("done");
                                } else {
                                    log.error("error");
                                }
                            });
                        }
                    } catch (Exception e) {
                        log.error(e);
                    }
                    res.sendOk();
                }).start();


        getAdmin();

        Main main = new Main();
        try {
            if (run) {
                ThreadUtil.execute(() -> {
                    // 一分钟后再执行 防止开机自启顺序错误
                    ThreadUtil.sleep(1, TimeUnit.MINUTES);
                    main.run();
                });
            }
        } catch (Exception e) {
            log.error(e);
        }

        if (StrUtil.isNotBlank(cron)) {
            CronUtil.schedule(cron, main);
            CronUtil.start();
        }

    }

    @Override
    public synchronized void run() {
        // 获取媒体库列表
        JsonArray items = HttpRequest.get(host + "/Users/" + adminUserId + "/Views?api_key=" + key)
                .thenFunction(res -> {
                    JsonObject body = gson.fromJson(res.body(), JsonObject.class);
                    return body.get("Items").getAsJsonArray();
                });

        if (StrUtil.isBlank(itemStr)) {
            for (int i = 0; i < items.size(); i++) {
                JsonObject viewAsJsonObject = items.get(i).getAsJsonObject();
                JsonElement name = viewAsJsonObject.get("Name");
                System.out.println((i + 1) + "\t" + name);
            }
            System.out.print("请选择: ");
            Scanner scanner = new Scanner(System.in);
            int i = scanner.nextInt();
            JsonElement item = items.get(i - 1);
            items = new JsonArray();
            items.add(item);
        }

        // 遍历媒体库
        for (JsonElement item : items) {
            JsonObject itemAsJsonObject = item.getAsJsonObject();
            String itemId = itemAsJsonObject.get("Id").getAsString();
            String name = itemAsJsonObject.get("Name").getAsString();
            // 匹配媒体库名称 - 则全匹配
            if (StrUtil.isNotBlank(itemStr) && !itemStr.equals("-")) {
                if (!Arrays.asList(itemStr.split(",")).contains(name)) {
                    continue;
                }
            }
            for (JsonElement jsonElement : getItems(itemId)) {
                pinyin(jsonElement.getAsJsonObject());
            }
        }
    }

    /**
     * 获取管理员账户
     */
    private static void getAdmin() {
        if (StrUtil.isNotBlank(adminUserId)) {
            return;
        }
        try {
            JsonObject adminUser = HttpRequest.get(host + "/Users?api_key=" + key)
                    .thenFunction(res -> {
                        JsonArray jsonElements = gson.fromJson(res.body(), JsonArray.class);
                        for (JsonElement jsonElement : jsonElements) {
                            JsonObject user = jsonElement.getAsJsonObject();
                            JsonObject policy = user.get("Policy").getAsJsonObject();
                            boolean isAdministrator = policy.get("IsAdministrator").getAsBoolean();
                            if (!isAdministrator) {
                                continue;
                            }
                            return user;
                        }
                        return null;
                    });
            if (Objects.isNull(adminUser)) {
                log.error("未找到管理员账户，请检查你的API KEY参数");
                return;
            }
            adminUserId = adminUser.get("Id").getAsString();
            log.info("adminUserId => {}", adminUserId);
        } catch (Exception e) {
            throw new RuntimeException("网络异常");
        }
    }

    /**
     * 拼音排序
     *
     * @param itemAsJsonObject
     */
    public static Boolean pinyin(JsonObject itemAsJsonObject) {
        String id = itemAsJsonObject.get("Id").getAsString();
        JsonElement seriesName = itemAsJsonObject.get("SeriesName");
        if (Objects.nonNull(seriesName)) {
            id = itemAsJsonObject.get("SeriesId").getAsString();
        }
        JsonObject jsonObject = HttpRequest.get(host + "/Users/" + adminUserId + "/Items/" + id + "?api_key=" + key)
                .thenFunction(res -> {
                    if (!JSONUtil.isTypeJSON(res.body())) {
                        log.error(res.body());
                        return null;
                    }
                    JsonObject body;
                    try {
                        body = gson.fromJson(res.body(), JsonObject.class);
                    } catch (Exception e) {
                        log.error("JSON解析失败 === > {}", res.body());
                        log.error(e);
                        throw new RuntimeException("JSON解析失败");
                    }
                    String name = body.get("Name").getAsString();
                    String pinyin = PinyinUtil.getPinyin(name);
                    body.addProperty("SortName", pinyin);
                    body.addProperty("ForcedSortName", pinyin);
                    JsonArray lockedFields = body.get("LockedFields").getAsJsonArray();
                    lockedFields.add("SortName");
                    return body;
                });
        if (Objects.isNull(jsonObject)) {
            return false;
        }
        return HttpRequest.post(host + "/Items/" + id + "?api_key=" + key)
                .body(gson.toJson(jsonObject))
                .thenFunction(HttpResponse::isOk);
    }

    /**
     * 递归获取所有视频
     *
     * @param ItemId
     * @return
     */
    public static JsonArray getItems(String ItemId) {
        return HttpRequest.get(host + "/Users/" + adminUserId + "/Items?api_key=" + key)
                .form("ParentId", ItemId)
                .thenFunction(res -> {
                    JsonArray retItems = new JsonArray();
                    JsonObject jsonObject;
                    try {
                        jsonObject = gson.fromJson(res.body(), JsonObject.class);
                    } catch (Exception e) {
                        log.error("JSON 序列化异常");
                        log.error(e);
                        return retItems;
                    }
                    JsonArray items = jsonObject.get("Items").getAsJsonArray();
                    for (JsonElement item : items) {
                        JsonObject itemAsJsonObject = item.getAsJsonObject();
                        String id = itemAsJsonObject.get("Id").getAsString();
                        String type = itemAsJsonObject.get("Type").getAsString();
                        if (List.of("Folder", "CollectionFolder").contains(type)) {
                            retItems.addAll(getItems(id));
                            continue;
                        }
                        if (List.of("Series", "Movie", "BoxSet", "Audio", "MusicAlbum", "MusicArtist", "Video", "Photo")
                                .contains(type)) {
                            retItems.add(itemAsJsonObject);
                        }
                    }

                    return retItems;
                });
    }

}
