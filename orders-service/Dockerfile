# syntax=docker/dockerfile:1.6
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./pom.xml
COPY domain-common/pom.xml domain-common/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY orders-service/pom.xml orders-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY warehouse-service/pom.xml warehouse-service/pom.xml
COPY shipping-service/pom.xml shipping-service/pom.xml
COPY notifications-service/pom.xml notifications-service/pom.xml
RUN mvn -B -q -pl orders-service -am dependency:go-offline -DskipTests || true
COPY domain-common/src domain-common/src
COPY orders-service/src orders-service/src
RUN mvn -B -q -pl orders-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/orders-service/target/orders-service-*.jar app.jar
ENV JAVA_OPTS=""
EXPOSE 8081
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
