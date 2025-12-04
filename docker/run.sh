#!/bin/sh

path="./"
jar="emby-pinyin-jar-with-dependencies.jar"
jar_path=$path$jar

sigterm_handler() {
    stop
    exit 0
}

trap 'sigterm_handler' SIGTERM

java -Xms60m -Xmx1g -Xss256k \
      -Xgcpolicy:gencon \
      -Xshareclasses:none \
      -Xquickstart -Xcompressedrefs \
      -Xtune:virtualized \
      -XX:+UseStringDeduplication \
      -XX:-ShrinkHeapInSteps \
      -XX:TieredStopAtLevel=1 \
      -XX:+IgnoreUnrecognizedVMOptions \
      -XX:+UseCompactObjectHeaders \
      --enable-native-access=ALL-UNNAMED \
      --add-opens=java.base/java.net=ALL-UNNAMED \
      --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED \
      -jar $jar_path
