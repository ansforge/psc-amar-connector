FROM maven:3-jdk-11 AS build
COPY settings-docker.xml /usr/share/maven/ref/
COPY async-listener /usr/src/app/async-listener
COPY in-user-api /usr/src/app/in-user-api
COPY psc-api /usr/src/app/psc-api
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml -gs /usr/share/maven/ref/settings-docker.xml -DskipTests clean package

FROM openjdk:11-slim-buster
RUN echo "deb [trusted=yes] http://repo.proxy-dev-forge.asip.hst.fluxus.net/artifactory/debian.org buster main" > /etc/apt/sources.list \
    && echo "deb [trusted=yes] http://repo.proxy-dev-forge.asip.hst.fluxus.net/artifactory/debian.org buster-updates main" >> /etc/apt/sources.list \
    && apt update \
COPY --from=build /usr/src/app/async-listener/target/async-listener-*.jar /usr/app/async-listener.jar
RUN chown -R daemon: /app
USER daemon
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/app/async-listener.jar"]
