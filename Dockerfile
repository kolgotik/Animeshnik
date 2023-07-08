FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean package

FROM eclipse-temurin:17-jdk-alpine

VOLUME /tmp

COPY target/*.jar Animeshnik-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/Animeshnik-0.0.1-SNAPSHOT.jar"]
EXPOSE 8080
