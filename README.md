# EmbyPinyin

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/wushuo894/EmbyPinyin/maven.yml?branch=master)](https://github.com/wushuo894/EmbyPinyin/actions/workflows/maven.yml)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/wushuo894/EmbyPinyin?color=blue&label=download&sort=semver)](https://github.com/wushuo894/EmbyPinyin/releases/latest)
[![GitHub all releases](https://img.shields.io/github/downloads/wushuo894/EmbyPinyin/total?color=blue&label=github%20downloads)](https://github.com/wushuo894/EmbyPinyin/releases)
[![Docker Pulls](https://img.shields.io/docker/pulls/wushuo894/emby-pinyin)](https://hub.docker.com/r/wushuo894/emby-pinyin)

Emby 支持拼音首字母排序

![Xnip2024-09-25_13-29-32.jpg](https://raw.githubusercontent.com/wushuo894/EmbyPinyin/master/images/Xnip2024-09-25_13-29-32.jpg)

## Docker部署

    docker run -d \
    --name emby-pinyin \
    -v /volume1/docker/emby-pinyin/config:/config \
    -p 9198:9198 \
    -e PORT="9198" \
    -e CONFIG="/config" \
    -e TZ=Asia/Shanghai \
    --restart always \
    ghcr.io/wushuo894/emby-pinyin

| 参数   | 作用  | 默认值           |
|------|-----|---------------|
| PORT | 端口号 | 9198          |
| TZ   | 时区  | Asia/Shanghai |

## 设置 Webhooks

| 参数     | 设置                        |
|--------|---------------------------|
| URL    | http://ip:端口/api/web_hook |
| 请求类型   | application/json          |
| Events | 媒体库/新媒体已添加                |

