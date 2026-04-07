# Emby-pinyin-custom

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/wushuo894/emby-pinyin/maven.yml?branch=master)](https://github.com/wushuo894/emby-pinyin/actions/workflows/maven.yml)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/wushuo894/emby-pinyin?color=blue&label=download&sort=semver)](https://github.com/wushuo894/EmbyPinyin/releases/latest)
[![GitHub all releases](https://img.shields.io/github/downloads/wushuo894/emby-pinyin/total?color=blue&label=github%20downloads)](https://github.com/wushuo894/emby-pinyin/releases)
[![Docker Pulls](https://img.shields.io/docker/pulls/wushuo894/emby-pinyin)](https://hub.docker.com/r/wushuo894/emby-pinyin)




## 功能介绍
   
 ### 🔤 改名模式
为 Emby 提供多种拼音首字母排序方式：
| 模式 | 示例 |
|------|------|
| 全拼 | chu men de shi jie |
| 首字母 | c m d s j |
| 前置字母 | c_楚门的世界 |
| 默认 | 楚门的世界 |

 ### 🔒 锁定规则

在排序标题末尾添加：“ #lock”  
该项目将跳过自动修改

| 模式 | 示例 |
|------|------|
| 楚门的世界#lock | ✅ |
| 楚门的世界 #lock | ✅ |
| 楚门的世界   #lock | ✅ |
| 楚门的世界 #LOCK | ✅ |  


使用实例1：在合集中新建片单合集并置顶
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

## 图片预览
![screenshot.png](https://raw.githubusercontent.com/wushuo894/emby-pinyin/master/images/screenshot.png#gh-light-mode-only)
![screenshot-dark.png](https://raw.githubusercontent.com/wushuo894/emby-pinyin/master/images/screenshot-dark.png#gh-dark-mode-only)
