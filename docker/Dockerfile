FROM wushuo894/eclipse-temurin:25-jre-alpine

COPY docker/run.sh /run.sh
COPY docker/exec.sh /exec.sh
COPY target/emby-pinyin-jar-with-dependencies.jar /usr/app/emby-pinyin-jar-with-dependencies.jar
WORKDIR /usr/app
VOLUME /config
ENV PUID=0 PGID=0 UMASK=022
ENV PORT=9198 CONFIG=/config TZ=Asia/Shanghai
EXPOSE $PORT
RUN chmod +x /exec.sh /run.sh
CMD ["/exec.sh"]
