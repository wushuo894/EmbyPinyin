# EmbyPinyin

Emby 电影、电视剧支持排序

### Docker部署

`docker run -d \
--name emby-pinyin \
-p 9198:9198 \
-e PORT="9198" \
-e HOST="http://192.168.5.4:8096" \
-e KEY="" \
-e ITEM="电影,番剧" \
-e CRON="0 1 * * *" \
-e RUN="TRUE" \
-e TZ=Asia/Shanghai \
--restart always \
wushuo894/emby-pinyin`
