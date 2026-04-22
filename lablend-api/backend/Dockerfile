# Multi-stage build
FROM maven:3.9.14-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]   
#ENTRYPOINT ["java", \
#    "-Dcom.sun.management.jmxremote", \
#    "-Dcom.sun.management.jmxremote.port=9010", \
#    "-Dcom.sun.management.jmxremote.rmi.port=9010", \
#    "-Dcom.sun.management.jmxremote.local.only=false", \
#    "-Dcom.sun.management.jmxremote.authenticate=false", \
#    "-Dcom.sun.management.jmxremote.ssl=false", \
#    "-Djava.rmi.server.hostname=127.0.0.1", \
#    "-jar", "/app/app.jar"] 
# This is used for VisualVM profiling, but it can be ignored for normal use.