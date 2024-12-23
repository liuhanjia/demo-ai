# 使用官方的 OpenJDK 作为基础镜像
FROM openjdk:17-jdk-slim

# 定义版本的构建参数
ARG VERSION=0.0.1-SNAPSHOT

# 设置工作目录
WORKDIR /app

# 将构建后的 JAR 文件复制到容器内
COPY target/yeebo-ai-${VERSION}.jar /app/yeebo-ai.jar

# 设置容器启动时运行的命令
ENTRYPOINT ["java", "-jar", "/app/yeebo-ai.jar"]
