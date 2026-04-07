package emby.pinyin.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import emby.pinyin.action.StatusAction;
import emby.pinyin.entity.Config;
import emby.pinyin.entity.Status;
import emby.pinyin.entity.Views;
import emby.pinyin.enums.PinyinMode;
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

                    // 优先从排序标题判断 #lock（更符合实际用途）
                    String sortName = body.has("SortName") && !body.get("SortName").isJsonNull()
                            ? body.get("SortName").getAsString()
                            : "";

                    String checkTarget = StrUtil.isNotBlank(sortName) ? sortName : name;
                    String lower = checkTarget.toLowerCase().trim();

                    // 跳过带有 #lock 标记的项目（支持 xxx#lock 和 xxx #lock）
                    if (lower.endsWith("#lock")) {
                        log.debug("skip (locked): {}", checkTarget);
                        return body;
                    }

                    String pinyin = "";

                    PinyinMode pinyinMode = config.getPinyinMode();

                    // 根据不同模式生成排序字段（SortName）
                    if (pinyinMode == PinyinMode.PINYIN) {
                        // 模式1：全拼，例如 "世界" → "shijie"
                        pinyin = PinyinUtils.getPinyin(name, " ").toLowerCase();

                    } else if (pinyinMode == PinyinMode.FIRST_LETTER) {
                        // 模式2：首字母，例如 "世界" → "sj"
                        pinyin = PinyinUtils.getFirstLetter(name, " ").toLowerCase();

                    } else if (pinyinMode == PinyinMode.PREFIX) {
                        // 模式3：前置字母，例如 "世界" → "s_世界"
                        String first = PinyinUtils.getFirstLetter(name, "");
                        String initial = (first != null && first.length() > 0)
                                ? first.substring(0, 1).toLowerCase()
                                : "";
                        pinyin = initial + "_" + name;

                    } else if (pinyinMode == PinyinMode.DEFAULT) {
                        // 模式4：Emby默认
                        // 这里不再删除字段，而是直接使用原始名称作为排序
                        pinyin = name;
                    }

                    log.debug("name: {} , pinyin: {}", name, pinyin);

                    if (StrUtil.isNotBlank(pinyin)) {
                        // 统一写入排序字段（所有模式都会写入并锁定）
                        body.addProperty("SortName", pinyin);
                        body.addProperty("ForcedSortName", pinyin);

                        // 锁定排序字段，防止被 Emby 或其他工具覆盖
                        JsonArray lockedFields = body.get("LockedFields").getAsJsonArray();
                        lockedFields.asList().clear();
                        lockedFields.add("SortName");
                    }

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
