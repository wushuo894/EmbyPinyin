FROM eclipse-temurin:17-jre
COPY target/EmbyPinyin-jar-with-dependencies.jar /usr/app/EmbyPinyin-jar-with-dependencies.jar
WORKDIR /usr/app
VOLUME /config
ENV PORT="9198"
ENV CONFIG="/config"
ENV TZ="Asia/Shanghai"
EXPOSE $PORT
RUN mkdir /usr/java
RUN ln -s /opt/java/openjdk /usr/java/openjdk-17
CMD ["java", "-jar", "EmbyPinyin-jar-with-dependencies.jar"]
