#!/bin/bash

mvn -B package --file pom.xml
cp target/EmbyPinyin-jar-with-dependencies.jar.jar ./