FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /application

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

ARG OTEL_VERSION=1.32.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar /opt/opentelemetry-javaagent.jar

WORKDIR /application
COPY --from=builder /application/target/*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]