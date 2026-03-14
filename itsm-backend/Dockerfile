FROM eclipse-temurin:17-jre-alpine

ARG MODULE
WORKDIR /app

COPY ${MODULE}/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
