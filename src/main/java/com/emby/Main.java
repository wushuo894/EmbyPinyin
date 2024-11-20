package com.emby;

import com.emby.util.ConfigUtil;
import com.emby.util.MavenUtil;
import com.emby.util.ServerUtil;
import com.github.promeg.pinyinhelper.Pinyin;
import com.github.promeg.pinyinhelper.PinyinMapDict;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Main {
    public static void main(String[] args) {
        HashMap<String, String[]> map = new HashMap<>();
        map.put("重庆", new String[]{"chong", "qing"});
        map.put("重启", new String[]{"chong", "qi"});
        map.put("重回", new String[]{"chong", "hui"});
        map.put("重生", new String[]{"chong", "sheng"});
        map.put("重来", new String[]{"chong", "lai"});

        Pinyin.init(Pinyin.newConfig()
                .with(new PinyinMapDict() {
                    @Override
                    public Map<String, String[]> mapping() {
                        return map;
                    }
                }));

        try {
            ConfigUtil.load();
            ServerUtil
                    .create(args)
                    .start();
            String version = MavenUtil.getVersion();
            log.info("version {}", version);
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(ServerUtil::stop));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
