FROM maven:3-jdk-11 AS build
COPY settings-docker.xml /usr/share/maven/ref/
COPY async-listener /usr/src/app/async-listener
COPY in-user-api /usr/src/app/in-user-api
COPY pom.xml /usr/src/app
ARG PROSANTECONNECT_PACKAGE_GITHUB_TOKEN
RUN mvn -f /usr/src/app/pom.xml -gs /usr/share/maven/ref/settings-docker.xml -Dinternal.repo.username=${PROSANTECONNECT_PACKAGE_GITHUB_TOKEN} -DskipTests clean package

FROM openjdk:11-slim-buster
RUN apt update
COPY --from=build /usr/src/app/async-listener/target/async-listener-*.jar /usr/app/async-listener.jar
USER daemon
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/app/async-listener.jar"]