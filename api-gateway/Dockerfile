FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/api-gateway-1.0.0.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8077

ENTRYPOINT ["java", "-jar", "app.jar"]