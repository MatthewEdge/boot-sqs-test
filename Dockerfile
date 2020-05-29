FROM gradle:jdk11 as builder
WORKDIR /usr/src/app

COPY build.gradle settings.gradle /usr/src/app/
RUN gradle compileJava

COPY ./src /usr/src/app/src
RUN gradle build

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /usr/src/app/build/libs/app.jar /app/app.jar

CMD java -jar /app/app.jar
