FROM eclipse-temurin:17-jre
COPY target/EmbyPinyin-jar-with-dependencies.jar /usr/app/EmbyPinyin-jar-with-dependencies.jar
WORKDIR /usr/app
ENV PORT="9198"
ENV HOST="http://192.168.5.4:8096"
ENV KEY=""
ENV ITEM="电影,番剧"
ENV CRON="0 1 * * *"
ENV RUN="TRUE"
ENV TZ="Asia/Shanghai"
RUN mkdir /usr/java
RUN ln -s /opt/java/openjdk /usr/java/openjdk-17
EXPOSE 9198
CMD ["java", "-jar", "EmbyPinyin-jar-with-dependencies.jar"]
