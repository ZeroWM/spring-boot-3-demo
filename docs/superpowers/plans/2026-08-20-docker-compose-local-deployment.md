# Docker Compose 本地部署实施计划

> **面向执行代理：** 必须使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans`，逐项实施并勾选本计划中的复选框。

**目标：** 将现有 Spring Boot 3 应用构建成本地 Docker 镜像，通过 Docker Compose 与 MySQL 8 一起部署，并完成 API 与数据持久化验收。

**架构：** 使用多阶段 Dockerfile 构建 Java 17 可执行 JAR，并在精简 JRE 镜像中以非 root 用户运行。Compose 在内部网络中连接 `app` 与 `mysql`，通过 MySQL 健康检查控制启动顺序，并使用命名卷持久化数据。

**技术栈：** Java 17、Spring Boot 3.1.4、Gradle 8.2.1、Docker 24、Docker Compose、MySQL 8、Flyway、curl、jq。

**设计文档：** `docs/superpowers/specs/2026-08-20-docker-compose-local-deployment-design.md`

## 全局约束

- 仅做本机 Docker Compose 部署，不推送 Docker Hub、GHCR 或其他远程镜像仓库。
- 保留现有 `application.yml`，通过 Compose 环境变量覆盖容器内数据库连接。
- 应用镜像使用 Java 17，并以非 root 用户运行。
- MySQL 使用 8.x，数据保存在 Compose 命名卷中。
- 默认宿主机端口为 `8080`，允许通过 `APP_PORT` 覆盖。
- `docker compose down` 必须保留数据；只有显式执行 `docker compose down -v` 才删除数据卷。
- 验收环境固定使用项目名 `spring-boot-3-demo-smoke` 和宿主机端口 `18080`，不得影响最终部署的数据。
- 最终 Compose 环境在验收完成后保持运行。
- 所有新增文档与交付说明使用中文；命令、文件名和代码标识符保留英文。

## 文件结构

- 新建 `Dockerfile`：负责应用多阶段构建与非 root 运行。
- 新建 `.dockerignore`：负责排除无关构建上下文。
- 新建 `compose.yaml`：负责应用、MySQL、健康检查、启动依赖和持久卷。
- 新建 `.env.example`：负责记录可选的本地配置覆盖值。
- 修改 `README.md`：负责中文部署、验证、排错、停止和数据清理说明。

---

### 任务 1：构建 Spring Boot 应用镜像

**文件：**

- 新建：`Dockerfile`
- 新建：`.dockerignore`

**接口：**

- 输入：仓库中的 `build.gradle`、`settings.gradle`、Gradle wrapper 和 `src` 目录。
- 输出：本地镜像 `spring-boot-3-demo:local`；容器入口为 `java -jar /app/app.jar`；运行用户为 `spring:spring`。

- [ ] **步骤 1：先验证当前仓库无法构建 Docker 镜像**

运行：

```bash
docker build --tag spring-boot-3-demo:local .
```

预期：失败，并提示当前目录中不存在 `Dockerfile`。该失败证明后续测试覆盖的行为是“仓库能够构建应用镜像”。

- [ ] **步骤 2：创建多阶段 Dockerfile**

创建 `Dockerfile`：

```dockerfile
FROM gradle:8.2.1-jdk17 AS builder

WORKDIR /workspace

COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle gradlew ./gradlew
RUN gradle --no-daemon dependencies

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
```

- [ ] **步骤 3：创建精简构建上下文**

创建 `.dockerignore`：

```text
.git
.gradle
.idea
build
*.iml
.env
.DS_Store
docs
```

- [ ] **步骤 4：构建镜像并验证运行元数据**

运行：

```bash
docker build --tag spring-boot-3-demo:local .
docker image inspect spring-boot-3-demo:local --format '{{.Config.User}} {{json .Config.Entrypoint}}'
```

预期：构建成功；检查结果为：

```text
spring:spring ["java","-jar","/app/app.jar"]
```

- [ ] **步骤 5：提交应用镜像文件**

```bash
git add -- Dockerfile .dockerignore
git commit -m "feat: add Docker application image"
```

---

### 任务 2：创建 Spring Boot 与 MySQL 双容器栈

**文件：**

- 新建：`compose.yaml`
- 新建：`.env.example`

**接口：**

- 输入：任务 1 输出的 `Dockerfile` 和本地镜像标签 `spring-boot-3-demo:local`。
- 输出：Compose 服务 `app`、`mysql`；命名卷 `mysql-data`；可覆盖变量 `APP_PORT`、`MYSQL_DATABASE`、`MYSQL_USER`、`MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD`。

- [ ] **步骤 1：先验证当前仓库没有可解析的 Compose 配置**

运行：

```bash
docker compose config
```

预期：失败，并提示找不到 Compose 配置文件。该失败证明后续验证覆盖的行为是“仓库能够解析并运行双容器栈”。

- [ ] **步骤 2：创建 Compose 配置**

创建 `compose.yaml`：

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE:-spring_boot_3_db}
      MYSQL_USER: ${MYSQL_USER:-mysql}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-mysql}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h localhost -u root -p$${MYSQL_ROOT_PASSWORD} --silent"]
      interval: 5s
      timeout: 5s
      retries: 20
      start_period: 20s
    restart: unless-stopped

  app:
    build:
      context: .
    image: spring-boot-3-demo:local
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE:-spring_boot_3_db}?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER:-mysql}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD:-mysql}
    ports:
      - "${APP_PORT:-8080}:8080"
    depends_on:
      mysql:
        condition: service_healthy
    restart: unless-stopped

volumes:
  mysql-data:
```

- [ ] **步骤 3：创建可选环境变量示例**

创建 `.env.example`：

```dotenv
APP_PORT=8080
MYSQL_DATABASE=spring_boot_3_db
MYSQL_USER=mysql
MYSQL_PASSWORD=mysql
MYSQL_ROOT_PASSWORD=root
```

- [ ] **步骤 4：验证默认配置和示例配置都能解析**

运行：

```bash
docker compose config
docker compose --env-file .env.example config
```

预期：两条命令都以状态码 `0` 结束；渲染结果包含 `app`、`mysql` 和 `mysql-data`。

- [ ] **步骤 5：启动隔离验收环境并等待 MySQL 健康**

运行：

```bash
docker compose -p spring-boot-3-demo-smoke down -v --remove-orphans
APP_PORT=18080 docker compose -p spring-boot-3-demo-smoke up --build -d

docker_smoke_mysql_status=""
for docker_smoke_attempt in $(seq 1 60); do
  docker_smoke_mysql_id=$(docker compose -p spring-boot-3-demo-smoke ps -q mysql)
  docker_smoke_mysql_status=$(docker inspect --format '{{.State.Health.Status}}' "$docker_smoke_mysql_id" 2>/dev/null || true)
  if [ "$docker_smoke_mysql_status" = "healthy" ]; then
    break
  fi
  sleep 2
done
test "$docker_smoke_mysql_status" = "healthy"
```

预期：MySQL 在最多 120 秒内进入 `healthy` 状态，随后 `app` 启动。

- [ ] **步骤 6：验证创建与查询 Actor**

运行：

```bash
docker_smoke_actor="docker-smoke-$(date +%s)"
printf '%s' "$docker_smoke_actor" > /tmp/spring-boot-3-demo-smoke-actor.txt
docker_smoke_post_status=""
for docker_smoke_attempt in $(seq 1 60); do
  docker_smoke_post_status=$(curl --silent --output /tmp/spring-boot-3-demo-smoke-post.txt --write-out '%{http_code}' \
    --header 'Content-Type: application/json' \
    --data "{\"username\":\"${docker_smoke_actor}\",\"displayName\":\"Docker Smoke\"}" \
    http://localhost:18080/actors || true)
  if [ "$docker_smoke_post_status" = "204" ]; then
    break
  fi
  sleep 2
done
test "$docker_smoke_post_status" = "204"

curl --fail --silent "http://localhost:18080/actors/${docker_smoke_actor}" \
  | jq --exit-status --arg username "$docker_smoke_actor" \
      '.username == $username and .displayName == "Docker Smoke"'
```

预期：POST 返回 `204`；GET 返回 `200`，且 `jq` 断言通过。

- [ ] **步骤 7：重启应用并验证 MySQL 数据仍然存在**

运行：

```bash
docker compose -p spring-boot-3-demo-smoke restart app

docker_smoke_actor=$(< /tmp/spring-boot-3-demo-smoke-actor.txt)
docker_smoke_get_status=""
for docker_smoke_attempt in $(seq 1 60); do
  docker_smoke_get_status=$(curl --silent --output /tmp/spring-boot-3-demo-smoke-get.json --write-out '%{http_code}' \
    "http://localhost:18080/actors/${docker_smoke_actor}" || true)
  if [ "$docker_smoke_get_status" = "200" ]; then
    break
  fi
  sleep 2
done
test "$docker_smoke_get_status" = "200"

jq --exit-status --arg username "$docker_smoke_actor" \
  '.username == $username and .displayName == "Docker Smoke"' \
  /tmp/spring-boot-3-demo-smoke-get.json
```

预期：应用重启后 GET 仍然返回相同 Actor，证明数据保存在 MySQL 卷中。

- [ ] **步骤 8：删除隔离验收环境**

运行：

```bash
docker compose -p spring-boot-3-demo-smoke down -v
test -z "$(docker compose -p spring-boot-3-demo-smoke ps -q)"
```

预期：仅删除 `spring-boot-3-demo-smoke` 项目的容器、网络和测试卷；最终部署尚未创建，不受影响。

- [ ] **步骤 9：提交 Compose 文件**

```bash
git add -- compose.yaml .env.example
git commit -m "feat: add local Docker Compose deployment"
```

---

### 任务 3：编写中文部署与运维文档

**文件：**

- 修改：`README.md`

**接口：**

- 输入：任务 2 输出的 Compose 服务、环境变量和管理命令。
- 输出：面向本地使用者的中文启动、验证、排错、停止和数据重置说明。

- [ ] **步骤 1：将 README 更新为完整中文部署说明**

将 `README.md` 更新为：

````markdown
# spring-boot-3-demo

使用 DDD 模式演示 Spring Boot 3、JPA、MyBatis 3、REST、JUnit 5 和 Spring Security。

## Docker Compose 本地部署

### 前置条件

- Docker 24 或更高版本
- Docker Compose v2

Compose 会同时启动 Spring Boot 应用和 MySQL 8。默认账号仅用于本地开发，不能作为生产环境凭据。

### 启动

```bash
docker compose up --build -d
```

查看运行状态：

```bash
docker compose ps
```

应用默认地址：`http://localhost:8080`

### 验证接口

创建 Actor：

```bash
curl --request POST \
  --header 'Content-Type: application/json' \
  --data '{"username":"Lufy","displayName":"MonkeyDMomoda"}' \
  http://localhost:8080/actors
```

查询 Actor：

```bash
curl http://localhost:8080/actors/Lufy
```

### 自定义配置

复制配置示例：

```bash
cp .env.example .env
```

可在 `.env` 中修改应用端口、数据库名和本地数据库账号。修改后重新执行：

```bash
docker compose up --build -d
```

### 日志与排错

```bash
docker compose logs -f app
docker compose logs mysql
```

如果 `8080` 端口已被占用，可以临时使用其他端口：

```bash
APP_PORT=18080 docker compose up --build -d
```

### 停止

停止并删除容器，但保留 MySQL 数据：

```bash
docker compose down
```

### 清空数据

以下命令会永久删除本项目的 MySQL 数据卷：

```bash
docker compose down -v
```
````

- [ ] **步骤 2：验证 README 中的 Compose 配置示例**

运行：

```bash
docker compose config
docker compose --env-file .env.example config
rg -n "docker compose up --build -d|docker compose down|docker compose down -v|APP_PORT" README.md
```

预期：两次配置解析成功；`rg` 找到启动、停止、清除数据和端口覆盖说明。

- [ ] **步骤 3：提交中文部署文档**

```bash
git add -- README.md
git commit -m "docs: document local Docker Compose deployment"
```

---

### 任务 4：完整验收并保持最终本地部署运行

**文件：**

- 不修改仓库文件。

**接口：**

- 输入：任务 1 至任务 3 的镜像、Compose 配置和操作文档。
- 输出：通过完整测试并保持运行的最终本地 Compose 环境，以及实际访问端口和容器状态。

- [ ] **步骤 1：运行完整 Java 测试和 Compose 静态验证**

运行：

```bash
./gradlew test
docker compose config
git diff --check
```

预期：Gradle 输出 `BUILD SUCCESSFUL`；Compose 配置解析成功；`git diff --check` 无输出并返回 `0`。

- [ ] **步骤 2：选择可用端口并启动最终环境**

运行：

```bash
docker_final_port=""
for docker_port_candidate in $(seq 8080 8100); do
  if ! lsof -nP -iTCP:"$docker_port_candidate" -sTCP:LISTEN >/dev/null 2>&1; then
    docker_final_port="$docker_port_candidate"
    break
  fi
done
test -n "$docker_final_port"
APP_PORT="$docker_final_port" docker compose up --build -d
```

预期：从 `8080` 到 `8100` 中选择第一个可用端口。两个 Compose 服务均被创建。

- [ ] **步骤 3：等待最终应用可用并验证 API**

运行：

```bash
docker_app_port=$(docker compose port app 8080 | awk -F: 'NR == 1 {print $NF}')
docker_final_actor="docker-final-$(date +%s)"
docker_final_status=""

for docker_final_attempt in $(seq 1 60); do
  docker_final_status=$(curl --silent --output /tmp/spring-boot-3-demo-final-post.txt --write-out '%{http_code}' \
    --header 'Content-Type: application/json' \
    --data "{\"username\":\"${docker_final_actor}\",\"displayName\":\"Docker Final\"}" \
    "http://localhost:${docker_app_port}/actors" || true)
  if [ "$docker_final_status" = "204" ]; then
    break
  fi
  sleep 2
done
test "$docker_final_status" = "204"

curl --fail --silent "http://localhost:${docker_app_port}/actors/${docker_final_actor}" \
  | jq --exit-status --arg username "$docker_final_actor" \
      '.username == $username and .displayName == "Docker Final"'
```

预期：POST 返回 `204`；GET 返回 `200` 且内容断言通过。

- [ ] **步骤 4：确认最终镜像、容器和 Git 状态**

运行：

```bash
docker image inspect spring-boot-3-demo:local --format '{{.Id}} {{.Config.User}}'
docker compose ps
docker compose port app 8080
git status --short --branch
```

预期：镜像存在且用户为 `spring:spring`；`app` 与 `mysql` 都在运行；输出实际宿主机端口；Git 工作区没有未提交的实施文件。

- [ ] **步骤 5：交付运行信息**

最终报告必须包含：

```text
镜像：spring-boot-3-demo:local
服务：app、mysql
访问地址：填写 `docker compose port app 8080` 返回端口对应的完整 localhost URL
状态：最终 Compose 环境保持运行
停止命令：docker compose down
日志命令：docker compose logs -f app
清空数据命令：docker compose down -v（会永久删除 MySQL 数据）
```

不得执行 `docker compose down`，除非用户随后明确要求停止部署。
