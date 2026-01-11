FROM docker.io/alpine:3.18

# 安装 OpenJDK 17
RUN apk update && apk add --no-cache openjdk17-jre tzdata && \
    ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    rm -rf /var/cache/apk/*

# 创建目录
RUN mkdir -p /app/mfutu

# 第一步：复制文件（无 --chown）
COPY target/mfutu-app-0.0.1-SNAPSHOT.jar /app/mfutu/mfutu-app.jar

# 第二步：创建用户并设置权限（分开执行）
RUN addgroup -S mfutu && adduser -S mfutu -G mfutu
RUN chown -R mfutu:mfutu /app

# 切换到用户
USER mfutu
WORKDIR /app/mfutu

EXPOSE 8080

CMD ["java", "-jar", "mfutu-app.jar"]
