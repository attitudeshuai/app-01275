FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 配置阿里云 Maven 镜像
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>\
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">\
  <mirrors>\
    <mirror>\
      <id>aliyun</id>\
      <mirrorOf>central</mirrorOf>\
      <url>https://maven.aliyun.com/repository/central</url>\
    </mirror>\
  </mirrors>\
</settings>' > /root/.m2/settings.xml

# 先复制 pom.xml 下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:resolve -B

# 再复制源码编译
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app

# 安装 mysql-client 以支持数据备份功能 (mysqldump/mysql)
RUN apt-get update && \
    apt-get install -y --no-install-recommends default-mysql-client && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
