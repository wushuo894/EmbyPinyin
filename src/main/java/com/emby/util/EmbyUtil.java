package com.emby.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.emby.action.StatusAction;
import com.emby.entity.Config;
import com.emby.entity.Status;
import com.emby.entity.Views;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class EmbyUtil {
    private static final Gson gson = new Gson();

    public static String ADMIN_USER_ID = "";

    /**
     * 获取管理员账户
     */
    public static void getAdmin() {
        if (StrUtil.isNotBlank(ADMIN_USER_ID)) {
            return;
        }
        Config config = ConfigUtil.CONFIG;
        String host = config.getHost();
        String key = config.getKey();

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
            ADMIN_USER_ID = adminUser.get("Id").getAsString();
            log.info("adminUserId => {}", ADMIN_USER_ID);
        } catch (Exception e) {
            throw new RuntimeException("网络异常");
        }
    }

    /**
     * 拼音排序
     *
     * @param item 视频
     */
    public static Boolean pinyin(JsonObject item) {
        Config config = ConfigUtil.CONFIG;
        String host = config.getHost();
        String key = config.getKey();

        String id = item.get("Id").getAsString();
        JsonElement seriesName = item.get("SeriesName");
        if (Objects.nonNull(seriesName)) {
            id = item.get("SeriesId").getAsString();
        }
        JsonObject jsonObject = HttpRequest.get(host + "/Users/" + ADMIN_USER_ID + "/Items/" + id + "?api_key=" + key)
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
                        log.error(e.getMessage(), e);
                        throw new RuntimeException("JSON解析失败");
                    }
                    String name = body.get("Name").getAsString();
                    String pinyin = PinyinUtil.getPinyin(name);
                    log.debug("name: {} , pinyin: {}", name, pinyin);
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
     * @param viewsId 媒体库id
     * @return 视频列表
     */
    public static JsonArray getItems(String viewsId) {
        Config config = ConfigUtil.CONFIG;
        String host = config.getHost();
        String key = config.getKey();

        return HttpRequest.get(host + "/Users/" + ADMIN_USER_ID + "/Items?api_key=" + key)
                .form("ParentId", viewsId)
                .thenFunction(res -> {
                    JsonArray retItems = new JsonArray();
                    JsonObject jsonObject;
                    try {
                        jsonObject = gson.fromJson(res.body(), JsonObject.class);
                    } catch (Exception e) {
                        log.error("JSON 序列化异常");
                        log.error(e.getMessage(), e);
                        return retItems;
                    }
                    JsonArray items = jsonObject.get("Items").getAsJsonArray();
                    Status status = StatusAction.STATUS;
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
                            Long total = status.getTotal();
                            status.setTotal(total + 1);
                        }
                    }
                    return retItems;
                });
    }

    /**
     * 获取媒体库列表
     *
     * @return 媒体库列表
     */
    public static synchronized List<Views> getViews() {
        Config config = ConfigUtil.CONFIG;
        String host = config.getHost();
        String key = config.getKey();
        List<String> cronIds = config.getCronIds();

        List<Views> viewsList = new ArrayList<>();

        if (StrUtil.isBlank(host)) {
            log.warn("host 为空");
            return viewsList;
        }

        if (StrUtil.isBlank(key)) {
            log.warn("key 为空");
            return viewsList;
        }

        getAdmin();

        JsonArray items = HttpRequest.get(host + "/Users/" + ADMIN_USER_ID + "/Views?api_key=" + key)
                .thenFunction(res -> {
                    JsonObject body = gson.fromJson(res.body(), JsonObject.class);
                    return body.get("Items").getAsJsonArray();
                });

        // 遍历媒体库
        for (JsonElement item : items) {
            JsonObject itemAsJsonObject = item.getAsJsonObject();
            String id = itemAsJsonObject.get("Id").getAsString();
            String name = itemAsJsonObject.get("Name").getAsString();
            Views views = new Views()
                    .setId(id)
                    .setName(name)
                    .setCron(cronIds.contains(id));
            viewsList.add(views);
        }

        return viewsList;

    }

}
