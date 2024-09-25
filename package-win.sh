#!/bin/bash

mkdir emby-pinyin
cp target/emby-pinyin-launcher.exe emby-pinyin/

sudo apt update
sudo apt install zip unzip
wget https://github.com/ojdkbuild/ojdkbuild/releases/download/java-17-openjdk-17.0.3.0.6-1/java-17-openjdk-17.0.3.0.6-1.jre.win.x86_64.zip
unzip java-17-openjdk-17.0.3.0.6-1.jre.win.x86_64.zip
mv java-17-openjdk-17.0.3.0.6-1.jre.win.x86_64 emby-pinyin/jre
zip -r emby-pinyin.win.x86_64.zip emby-pinyin

ls emby-pinyin.win.x86_64.zip -al