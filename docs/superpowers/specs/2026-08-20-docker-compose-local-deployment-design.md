# Docker Compose Local Deployment Design

## Context

The Spring Boot application currently runs directly from Gradle or an IDE and connects to MySQL at `localhost:3306`. Inside a container, `localhost` refers to that container, so the existing datasource URL cannot reach a separate MySQL container. The repository has no Dockerfile, Compose definition, or container deployment documentation.

## Goals

- Build the Spring Boot application as a local Docker image.
- Start the application and MySQL 8 together with one Docker Compose command.
- Preserve MySQL data across normal container recreation.
- Keep direct IDE and Gradle startup behavior unchanged.
- Verify the deployed HTTP API and database persistence before leaving the local deployment running.

## Non-goals

- Publishing an image to Docker Hub, GHCR, or another registry.
- Deploying to a remote host or cloud platform.
- Adding orchestration systems such as Kubernetes or Docker Swarm.
- Adding Spring Boot Actuator solely for container health checks.

## Architecture

The Compose application has two services on its default internal network:

- `app`: a locally built Spring Boot image exposing container port `8080`.
- `mysql`: the official MySQL 8 image exposing port `3306` only to the internal Compose network.

The `app` service connects to `mysql:3306`. A named Compose volume stores `/var/lib/mysql`, so `docker compose down` preserves data. The volume is removed only when the operator explicitly uses `docker compose down -v`.

MySQL has a `mysqladmin ping` health check. Compose starts `app` only after MySQL reports healthy. Flyway then applies the existing migration and creates the `actors` table.

## Image Build

The Dockerfile uses two stages:

1. A Java 17 build stage runs the Gradle `bootJar` task.
2. A Java 17 JRE stage copies only the executable JAR and runs it as a non-root user.

The runtime image exposes port `8080` and starts the JAR with `java -jar`. A `.dockerignore` excludes Git metadata, Gradle caches, build outputs, IDE files, and local environment files from the build context.

## Configuration

The committed `application.yml` remains unchanged for direct local startup. Compose overrides it with standard Spring Boot environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

The datasource URL uses the Compose service name `mysql` and retains the MySQL compatibility parameters already used by the application.

The host application port defaults to `8080` and can be changed through `APP_PORT`. Database settings can be changed through:

- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`

The repository includes `.env.example` with local-development values. Compose also supplies local-only defaults so `docker compose up --build -d` works without creating a `.env` file. The README states that these defaults are not production credentials.

Both services use `restart: unless-stopped` so they recover from Docker daemon restarts while remaining easy to stop intentionally.

## Files

- `Dockerfile`: multi-stage application image build.
- `.dockerignore`: minimal and reproducible build context.
- `compose.yaml`: application, MySQL, health check, network dependency, and persistent volume.
- `.env.example`: documented optional local overrides.
- `README.md`: build, startup, verification, logs, shutdown, and data-reset instructions.

No production Java classes need to change because Spring Boot environment variables override the existing YAML configuration.

## Failure Handling

- If MySQL cannot initialize, its health check stays unhealthy and `app` does not start.
- If Flyway or application startup fails, the container exits and `docker compose logs app` shows the Spring Boot failure.
- If host port `8080` is occupied, the operator can set `APP_PORT` to another port without rebuilding the image.
- `docker compose ps`, `docker compose logs -f app`, and `docker compose logs mysql` are the primary diagnostic commands documented in the README.
- Normal shutdown preserves the database volume. Data deletion requires the explicit `-v` flag and is documented as destructive.

## Verification

Implementation acceptance uses this sequence:

1. Run `./gradlew test` and require the full Java test suite to pass.
2. Run `docker compose config` and require successful configuration rendering.
3. Start an isolated smoke-test project named `spring-boot-3-demo-smoke` on host port `18080` with `docker compose -p spring-boot-3-demo-smoke up --build -d`.
4. Wait for MySQL health and Spring Boot startup using condition checks, not fixed sleeps.
5. Send `POST /actors` and require HTTP `204`.
6. Send `GET /actors/{username}` and require HTTP `200` with the submitted username and display name.
7. Restart only the application service and repeat the GET request to prove the database volume retained the actor.
8. Remove the isolated smoke-test project and its dedicated test volume with `docker compose -p spring-boot-3-demo-smoke down -v`.
9. Start the final local deployment with `docker compose up --build -d`, using port `8080` unless it is occupied.
10. Report the built image, running containers, mapped port, access URL, and management commands. Leave the final deployment running.

The smoke-test project name guarantees its containers, network, and volume are separate from the final local deployment. Removing its volume cannot remove the final deployment's data.

## Delivery

The completed result is a reproducible local Compose deployment. It does not create or push a remote image. The final application is reachable at `http://localhost:${APP_PORT}` while Docker Compose remains running.
