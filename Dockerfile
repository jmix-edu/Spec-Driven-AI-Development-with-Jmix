# Runtime-only image. Build the JAR on the host first:
#   ./gradlew -Pvaadin.productionMode=true bootJar
FROM eclipse-temurin:21-jre

RUN groupadd --system booking && useradd --system --gid booking --no-create-home booking

WORKDIR /app
COPY build/libs/booking-*.jar app.jar
# File storage (a volume in docker-compose) and the Jmix work/temp dirs must be writable by the app user.
RUN mkdir -p /app/filestorage /app/.jmix && chown -R booking:booking /app
USER booking

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
