package net.enjoy.springboot.registrationlogin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 播放鉴权结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayAuthResult {
    
    /**
     * 是否允许播放
     */
    private boolean allowed;
    
    /**
     * 结果代码：0-成功，其他-错误码
     */
    private int code;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 播放令牌（如果需要）
     */
    private String playToken;
    
    /**
     * 过期时间戳
     */
    private Long expireTime;
    
    /**
     * 播放地址信息
     */
    private PlayUrlInfo playUrlInfo;
    
    /**
     * 创建成功结果
     */
    public static PlayAuthResult success() {
        return new PlayAuthResult(true, 0, "播放鉴权成功", null, null, null);
    }
    
    /**
     * 创建成功结果（带播放令牌）
     */
    public static PlayAuthResult success(String playToken, Long expireTime) {
        return new PlayAuthResult(true, 0, "播放鉴权成功", playToken, expireTime, null);
    }
    
    /**
     * 创建成功结果（带播放地址信息）
     */
    public static PlayAuthResult success(PlayUrlInfo playUrlInfo) {
        return new PlayAuthResult(true, 0, "播放鉴权成功", null, null, playUrlInfo);
    }
    
    /**
     * 创建失败结果
     */
    public static PlayAuthResult failure(int code, String message) {
        return new PlayAuthResult(false, code, message, null, null, null);
    }
    
    /**
     * 播放地址信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayUrlInfo {
        private String rtmpUrl;
        private String hlsUrl;
        private String flvUrl;
        private String webrtcUrl;
    }
} 