FROM openjdk:17.0.2

EXPOSE 8080

ADD ./build/libs/jigglog-0.0.1-SNAPSHOT.jar app.jar
ENV	USE_PROFILE docker

ENTRYPOINT ["java", "-jar", "/app.jar"]