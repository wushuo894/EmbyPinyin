package com.emby;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static final Gson gson = new Gson();
    public static String adminUserId = "";
    public static String host = "http://192.168.5.4:8096";
    public static String key = "c30e784137134792b2907b78f5c23b60";
    public static String itemStr = "";
    public static Integer port = 9198;

    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();

        Map<String, String> envMap = System.getenv();
        Map<String, String> argsMap = CollUtil.split(List.of(args), 2)
                .stream()
                .collect(Collectors.toMap(it -> it.get(0), it -> it.get(1)));

        map.putAll(argsMap);
        map.putAll(envMap);

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
        }

        HttpUtil.createServer(port)
                .addAction("/", (req, res) -> {
                    JsonObject jsonObject = gson.fromJson(req.getBody(), JsonObject.class);
                    JsonObject item = jsonObject.get("Item").getAsJsonObject();
                    ThreadUtil.execute(() -> pinyin(item));
                    res.sendOk();
                }).start();

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
        Assert.notNull(adminUser, "未找到管理员账户，请检查你的API KEY参数");

        adminUserId = adminUser.get("Id").getAsString();

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

        for (JsonElement item : items) {
            JsonObject itemAsJsonObject = item.getAsJsonObject();
            String itemId = itemAsJsonObject.get("Id").getAsString();
            String name = itemAsJsonObject.get("Name").getAsString();
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

    public static void pinyin(JsonObject itemAsJsonObject) {
        String id = itemAsJsonObject.get("Id").getAsString();
        JsonObject jsonObject = HttpRequest.get(host + "/Users/" + adminUserId + "/Items/" + id + "?api_key=" + key)
                .thenFunction(res -> {
                    JsonObject body = gson.fromJson(res.body(), JsonObject.class);
                    String name = body.get("Name").getAsString();
                    String pinyin = PinyinUtil.getPinyin(name);
                    body.addProperty("SortName", pinyin);
                    body.addProperty("ForcedSortName", pinyin);
                    JsonArray lockedFields = body.get("LockedFields").getAsJsonArray();
                    lockedFields.add("SortName");
                    return body;
                });
        HttpRequest.post(host + "/Items/" + id + "?api_key=" + key)
                .body(gson.toJson(jsonObject))
                .execute();
    }

    public static JsonArray getItems(String ItemId) {
        return HttpRequest.get(host + "/Users/" + adminUserId + "/Items?api_key=" + key)
                .form("ParentId", ItemId)
                .thenFunction(res -> {
                    JsonObject jsonObject = gson.fromJson(res.body(), JsonObject.class);
                    JsonArray items = jsonObject.get("Items").getAsJsonArray();
                    JsonArray retItems = new JsonArray();
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