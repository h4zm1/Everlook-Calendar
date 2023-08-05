# syntax = docker/dockerfile:1.6

FROM ubuntu:latest AS build

RUN --mount=type=secret,id=_env,dst=/etc/secrets/.env cat /etc/secrets/.env
RUN apt-get update
RUN apt-get install openjdk-17-jdk -y
COPY . .

RUN ./gradlew bootJar --no-daemon

FROM openjdk:17-jdk-slim

EXPOSE 8080

COPY --from=build /build/libs/EverlookCalendar-1.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]