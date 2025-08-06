package net.enjoy.springboot.registrationlogin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.zlm")
@Data
public class ZLMediaKitConfig {
    
    // 服务器URL配置
    @Value("${app.server.host:localhost}")
    private String host;
    
    @Value("${app.server.port:8080}")
    private int httpPort;
    
    @Value("${app.server.rtmp-port:1935}")
    private int rtmpPort;
    
    // 动态生成服务器URL
    public String getServerUrl() {
        return String.format("http://%s:%d", host, httpPort);
    }
    
    // 获取基础主机地址
    public String getBaseUrl() {
        return host;
    }
    
    // 获取基础主机地址（兼容旧方法名）
    public String getHost() {
        return host;
    }
    
    // 获取HTTP端口
    public int getHttpPort() {
        return httpPort;
    }
    
    // 获取RTMP端口
    public int getRtmpPort() {
        return rtmpPort;
    }
    
    // URL模板配置
    private String pushUrlTemplate = "rtmp://{serverHost}:{rtmpPort}/{appName}/{streamName}";
    private String hlsUrlTemplate = "http://{serverHost}:{httpPort}/{appName}/{streamName}.live.m3u8";
    private String flvUrlTemplate = "http://{serverHost}:{httpPort}/{appName}/{streamName}.live.flv";
    private String playUrlTemplate = "rtmp://{serverHost}:{rtmpPort}/{appName}/{streamName}";

    // 推流地址生成
    public String generatePushUrl(String appName, String streamName) {
        return pushUrlTemplate
                .replace("{serverHost}", host)
                .replace("{rtmpPort}", String.valueOf(rtmpPort))
                .replace("{appName}", appName)
                .replace("{streamName}", streamName);
    }

    // 播放地址生成
    public String generatePlayUrl(String appName, String streamName) {
        return playUrlTemplate
                .replace("{serverHost}", host)
                .replace("{rtmpPort}", String.valueOf(rtmpPort))
                .replace("{appName}", appName)
                .replace("{streamName}", streamName);
    }
    
    // HLS播放地址生成
    public String generateHlsUrl(String appName, String streamName) {
        return hlsUrlTemplate
                .replace("{serverHost}", host)
                .replace("{httpPort}", String.valueOf(httpPort))
                .replace("{appName}", appName)
                .replace("{streamName}", streamName);
    }
    
    // FLV播放地址生成
    public String generateFlvUrl(String appName, String streamName) {
        return flvUrlTemplate
                .replace("{serverHost}", host)
                .replace("{httpPort}", String.valueOf(httpPort))
                .replace("{appName}", appName)
                .replace("{streamName}", streamName);
    }

    // 流媒体服务器配置
    private String serverHost = "localhost";
    private int streamPort = 1935;
    private String defaultApp = "live";
    
    // 推流认证配置
    private boolean enablePushAuth = true;
    private boolean enablePlayAuth = true;
    private int tokenExpireMinutes = 60;
} 