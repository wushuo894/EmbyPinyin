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

| 参数   | 作用          | 默认值                   |
|------|-------------|-----------------------|
| PORT | 端口号         | 9877                  |
| HOST | emby 地址     | http://127.0.0.1:3000 |
| KEY  | API Key     | 空                     |
| ITEM | 媒体库(可以用,分割) | 电影,番剧                 |
| RUN  | 启动时运行       | TRUE                  |
| CRON | 计划任务        | 0 1 * * *             |
| TZ   | 时区          | Asia/Shanghai         |
