FROM openjdk:17-jdk-slim
VOLUME /tmp
ADD build/libs/inception-serving-sb.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]