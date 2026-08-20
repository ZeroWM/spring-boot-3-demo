FROM gradle:8.2.1-jdk17 AS builder

WORKDIR /workspace

COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle src src

RUN gradle --no-daemon bootJar

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system --gid 10001 spring \
    && useradd --system --uid 10001 --gid spring --home-dir /app --shell /usr/sbin/nologin spring

COPY --from=builder --chown=spring:spring /workspace/build/libs/*.jar /app/app.jar

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
