# Ubuntu LTS：软件源仍在维护，apt 可直接装 ffmpeg，无需 CentOS7 vault / RPM Fusion
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# ffmpeg：音视频；libasound2：ALSA；portaudio19-dev：PortAudio（与 Debian 文档一致）
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ffmpeg \
        libasound2 \
        portaudio19-dev \
        ca-certificates \
        locales \
    && locale-gen en_US.UTF-8 \
    && rm -rf /var/lib/apt/lists/*

# 添加 JDK 21
COPY jdk21.tar.gz /usr/local/

# 解压 JDK 21
RUN tar -xzf /usr/local/jdk21.tar.gz -C /usr/local/ && rm -f /usr/local/jdk21.tar.gz

# 设置环境变量（请根据实际解压后的目录名调整 JAVA_HOME）
ENV JAVA_HOME=/usr/local/jdk-21.0.1
ENV PATH=$JAVA_HOME/bin:$PATH

# 敏感凭证请勿写入镜像；运行时注入，例如:
# docker run -e OSS_ACCESS_KEY_ID=... -e OSS_ACCESS_KEY_SECRET=... -e ARK_API_KEY=...
# 若必须在构建期传入，可使用 ARG + ENV（仍不推荐提交到仓库）

# 统一编码为 UTF-8
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8

# 创建应用目录
RUN mkdir -p /myproject
WORKDIR /myproject

# 复制应用到容器
COPY myproject.jar myproject.jar

# 暴露端口
EXPOSE 8080

# 启动应用（仍为 Java；说明里的 CMD python app.py 适用于 Python 项目，此处保持 jar 启动）
ENTRYPOINT ["java", "-jar", "/myproject/myproject.jar"]

# 可选：若需访问宿主机声卡，运行时增加:
# docker run -d --name myproject-service --device /dev/snd ...
