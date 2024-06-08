FROM openjdk:17-jdk
COPY target/EmbyPinyin-jar-with-dependencies.jar /usr/app/EmbyPinyin-jar-with-dependencies.jar
WORKDIR /usr/app
ENV PORT="9198"
ENV HOST="http://192.168.5.4:8096"
ENV KEY="c30e784137134792b2907b78f5c23b60"
ENV ITEM="电影,番剧"
ENV CRON="0 1 * * *"
ENV RUN="TRUE"
ENV TZ="Asia/Shanghai"
EXPOSE 9198
CMD ["java", "-jar", "EmbyPinyin-jar-with-dependencies.jar"]
