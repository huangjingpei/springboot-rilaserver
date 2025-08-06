#!/bin/bash

# 更新系统包
echo "更新系统包..."
sudo apt-get update -y

# 安装必需的依赖包
echo "安装依赖包..."
sudo apt-get install -y build-essential cmake libssl-dev libsdl-dev libavcodec-dev libavutil-dev ffmpeg git

# 下载ZLMediaKit源码
echo "下载ZLMediaKit源码..."
git clone https://github.com/ZLMediaKit/ZLMediaKit.git
cd ZLMediaKit
git submodule update --init

# 创建构建目录并编译项目
echo "创建构建目录并编译项目..."
mkdir build
cd build
cmake ..
make -j4

# 运行项目
echo "启动MediaServer..."
cd ../release/linux/Debug/
./MediaServer -d &

# 修改并确保配置文件config.ini的值
echo "修改config.ini配置文件..."
CONFIG_FILE="../release/linux/Debug/config.ini"

# 备份原始配置文件
cp $CONFIG_FILE ${CONFIG_FILE}.bak

# 使用sed命令更新配置项
sed -i 's/^alive_interval=.*/alive_interval=30.000000/' $CONFIG_FILE
sed -i 's/^enable=.*/enable=1/' $CONFIG_FILE
sed -i 's|^on_flow_report=.*|on_flow_report=http://127.0.0.1:8080/index/hook/on_flow_report|' $CONFIG_FILE
sed -i 's|^on_http_access=.*|on_http_access=http://127.0.0.1:8080/index/hook/on_http_access|' $CONFIG_FILE
sed -i 's|^on_play=.*|on_play=http://127.0.0.1:8080/index/hook/on_play|' $CONFIG_FILE
sed -i 's|^on_publish=.*|on_publish=http://127.0.0.1:8080/index/hook/on_publish|' $CONFIG_FILE
sed -i 's|^on_record_mp4=.*|on_record_mp4=http://127.0.0.1:8080/index/hook/on_record_mp4|' $CONFIG_FILE
sed -i 's|^on_rtsp_auth=.*|on_rtsp_auth=http://127.0.0.1:8080/index/hook/on_rtsp_auth|' $CONFIG_FILE
sed -i 's|^on_rtsp_realm=.*|on_rtsp_realm=http://127.0.0.1:8080/index/hook/on_rtsp_realm|' $CONFIG_FILE
sed -i 's|^on_shell_login=.*|on_shell_login=http://127.0.0.1:8080/index/hook/on_shell_login|' $CONFIG_FILE
sed -i 's|^on_stream_changed=.*|on_stream_changed=http://127.0.0.1:8080/index/hook/on_stream_changed|' $CONFIG_FILE
sed -i 's|^on_stream_none_reader=.*|on_stream_none_reader=http://127.0.0.1:8080/index/hook/on_stream_none_reader|' $CONFIG_FILE
sed -i 's|^on_stream_not_found=.*|on_stream_not_found=http://127.0.0.1:8080/index/hook/on_stream_not_found|' $CONFIG_FILE
sed -i 's|^on_server_started=.*|on_server_started=http://127.0.0.1:8080/index/hook/on_server_started|' $CONFIG_FILE
sed -i 's|^on_server_keepalive=.*|on_server_keepalive=http://127.0.0.1:8080/index/hook/on_server_keepalive|' $CONFIG_FILE
sed -i 's|^on_rtp_server_timeout=.*|on_rtp_server_timeout=http://127.0.0.1:8080/index/hook/on_rtp_server_timeout|' $CONFIG_FILE

echo "ZLMediaKit 部署完成！"

