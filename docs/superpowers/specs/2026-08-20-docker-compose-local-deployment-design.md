# Docker Compose 本地部署设计

## 背景

当前 Spring Boot 应用通过 Gradle 或 IDE 直接运行，并连接 `localhost:3306` 上的 MySQL。进入容器后，`localhost` 指向应用容器自身，因此现有数据源地址无法访问独立的 MySQL 容器。仓库目前没有 Dockerfile、Compose 定义或容器部署文档。

## 目标

- 将 Spring Boot 应用构建成本地 Docker 镜像。
- 使用一条 Docker Compose 命令同时启动应用和 MySQL 8。
- 在正常重建容器后保留 MySQL 数据。
- 保持通过 IDE 和 Gradle 直接启动的现有行为不变。
- 在保留最终本地部署运行前，验证 HTTP API 和数据库持久化能力。

## 非目标

- 将镜像发布到 Docker Hub、GHCR 或其他镜像仓库。
- 部署到远程服务器或云平台。
- 引入 Kubernetes、Docker Swarm 等编排系统。
- 仅为了容器健康检查而引入 Spring Boot Actuator。

## 架构

Compose 应用在默认内部网络中包含两个服务：

- `app`：本地构建的 Spring Boot 镜像，对外提供容器端口 `8080`。
- `mysql`：官方 MySQL 8 镜像，只在 Compose 内部网络中提供端口 `3306`。

`app` 服务通过 `mysql:3306` 连接数据库。Compose 命名卷保存 `/var/lib/mysql` 中的数据，因此执行 `docker compose down` 后数据仍然保留。只有操作人员明确执行 `docker compose down -v` 时才会删除该数据卷。

MySQL 使用 `mysqladmin ping` 健康检查。Compose 仅在 MySQL 状态为健康后启动 `app`。随后由 Flyway 应用现有迁移并创建 `actors` 表。

## 镜像构建

Dockerfile 使用两个阶段：

1. Java 17 构建阶段运行 Gradle `bootJar` 任务。
2. Java 17 JRE 运行阶段只复制可执行 JAR，并通过非 root 用户运行。

运行镜像暴露端口 `8080`，通过 `java -jar` 启动 JAR。`.dockerignore` 从构建上下文中排除 Git 元数据、Gradle 缓存、构建产物、IDE 文件和本地环境文件。

## 配置

保留已提交的 `application.yml`，以维持直接在本地启动的行为。Compose 使用以下 Spring Boot 标准环境变量覆盖数据库配置：

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

数据源地址使用 Compose 服务名 `mysql`，并保留应用现有的 MySQL 兼容参数。

宿主机应用端口默认为 `8080`，可通过 `APP_PORT` 修改。数据库设置可通过以下变量修改：

- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`

仓库提供包含本地开发值的 `.env.example`。Compose 同时提供仅适用于本地的默认值，因此无需创建 `.env` 文件也可以直接执行 `docker compose up --build -d`。README 会明确说明这些默认值不能作为生产环境凭据。

两个服务都使用 `restart: unless-stopped`，使其能够在 Docker daemon 重启后恢复，同时允许操作人员主动停止。

## 文件

- `Dockerfile`：多阶段应用镜像构建。
- `.dockerignore`：精简且可复现的构建上下文。
- `compose.yaml`：应用、MySQL、健康检查、网络依赖和持久化卷。
- `.env.example`：可选本地配置覆盖示例。
- `README.md`：构建、启动、验证、日志、停止和数据重置说明。

Spring Boot 环境变量会覆盖现有 YAML 配置，因此无需修改生产 Java 类。

## 故障处理

- 如果 MySQL 无法初始化，其健康检查会保持不健康，`app` 不会提前启动。
- 如果 Flyway 或应用启动失败，应用容器会退出，`docker compose logs app` 会显示 Spring Boot 错误。
- 如果宿主机端口 `8080` 已被占用，可以设置其他 `APP_PORT`，无需重建镜像。
- README 将 `docker compose ps`、`docker compose logs -f app` 和 `docker compose logs mysql` 作为主要诊断命令。
- 正常停止会保留数据库卷。删除数据必须显式添加 `-v`，并在文档中标注为破坏性操作。

## 验证

实施验收采用以下顺序：

1. 运行 `./gradlew test`，要求完整 Java 测试套件通过。
2. 运行 `docker compose config`，要求 Compose 配置成功解析。
3. 使用 `docker compose -p spring-boot-3-demo-smoke up --build -d`，在宿主机端口 `18080` 启动名为 `spring-boot-3-demo-smoke` 的隔离验收环境。
4. 使用条件检查等待 MySQL 健康和 Spring Boot 启动，不使用固定时间休眠。
5. 发送 `POST /actors`，要求返回 HTTP `204`。
6. 发送 `GET /actors/{username}`，要求返回 HTTP `200`，且用户名和显示名称与提交内容一致。
7. 只重启应用服务并再次执行 GET 请求，证明数据库卷保留了 Actor 数据。
8. 使用 `docker compose -p spring-boot-3-demo-smoke down -v` 删除隔离验收环境及其专用测试卷。
9. 使用 `docker compose up --build -d` 启动最终本地部署；除非端口被占用，否则使用 `8080`。
10. 报告已构建镜像、运行容器、映射端口、访问地址和管理命令，并保持最终部署运行。

验收环境使用独立的 Compose 项目名，保证其容器、网络和数据卷与最终本地部署隔离。删除验收数据卷不会删除最终部署的数据。

## 交付结果

最终结果是一套可复现的本地 Compose 部署，不会创建或推送远程镜像。Docker Compose 保持运行期间，可通过 `http://localhost:${APP_PORT}` 访问应用。
