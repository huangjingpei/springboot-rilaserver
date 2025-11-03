# 下载源码
git clone  https://github.com/ZLMediaKit/ZLMediaKit.git
cd ZLMediaKit
git submodule update --init

# 安装编译器
sudo apt-get install build-essential

# 安装cmake 
sudo apt-get install cmake

# 安装依赖库
sudo apt-get install libssl-dev
sudo apt-get install libsdl-dev
sudo apt-get install libavcodec-dev
sudo apt-get install libavutil-dev
sudo apt-get install ffmpeg

# 构建和编译项目
mkdir build
cd build
cmake ..
make -j4

# 运行项目
cd ..
cd release/linux/Debug/
./MediaServer -d &


# 修改并确保配置文件config.ini的值
alive_interval=30.000000
enable=1
on_flow_report=http://127.0.0.1:8080/index/hook/on_flow_report
on_http_access=http://127.0.0.1:8080/index/hook/on_http_access
on_play=http://127.0.0.1:8080/index/hook/on_play
on_publish=http://127.0.0.1:8080/index/hook/on_publish
on_record_mp4=http://127.0.0.1:8080/index/hook/on_record_mp4
on_rtsp_auth=http://127.0.0.1:8080/index/hook/on_rtsp_auth
on_rtsp_realm=http://127.0.0.1:8080/index/hook/on_rtsp_realm
on_shell_login=http://127.0.0.1:8080/index/hook/on_shell_login
on_stream_changed=http://127.0.0.1:8080/index/hook/on_stream_changed
on_stream_none_reader=http://127.0.0.1:8080/index/hook/on_stream_none_reader
on_stream_not_found=http://127.0.0.1:8080/index/hook/on_stream_not_found
on_server_started=http://127.0.0.1:8080/index/hook/on_server_started
on_server_keepalive=http://127.0.0.1:8080/index/hook/on_server_keepalive
on_rtp_server_timeout=http://127.0.0.1:8080/index/hook/on_rtp_server_timeout