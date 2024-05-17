FROM maven:3.9.5-eclipse-temurin-21-alpine as build
WORKDIR /workspace/app

COPY pom.xml .

RUN mvn -B -e -C -T 1C org.apache.maven.plugins:maven-dependency-plugin:3.6.1:go-offline

COPY . .
RUN mvn clean package -Dmaven.test.skip=true


FROM openjdk:21-buster
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build /workspace/app/target/observer-1.3-SNAPSHOT.jar observer.jar
ENTRYPOINT ["java","-jar", "observer.jar"]
