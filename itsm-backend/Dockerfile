FROM eclipse-temurin:17.0.13_11-jre-alpine

ARG MODULE
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY ${MODULE}/build/libs/*.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xmx512m", "-Xms256m", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
