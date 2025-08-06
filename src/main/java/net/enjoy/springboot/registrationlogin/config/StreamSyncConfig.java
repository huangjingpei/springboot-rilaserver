package net.enjoy.springboot.registrationlogin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 流状态同步配置
 */
@Configuration
@ConfigurationProperties(prefix = "stream.sync")
public class StreamSyncConfig {
    
    /**
     * 是否启用新客户端状态同步
     */
    private boolean enabled = true;
    
    /**
     * 同步策略：user_only(仅用户自己的流), public_all(所有公开流), room_based(基于房间)
     */
    private String strategy = "user_only";
    
    /**
     * 是否同步历史流状态（已结束的流）
     */
    private boolean includeHistoryStreams = false;
    
    /**
     * 最大同步流数量限制
     */
    private int maxSyncStreams = 50;
    
    /**
     * 同步延迟（毫秒），避免连接建立时立即发送大量消息
     */
    private long syncDelayMs = 1000;
    
    /**
     * 是否同步其他用户的公开流
     */
    private boolean syncPublicStreams = true;
    
    /**
     * 权限检查模式：strict(严格模式), loose(宽松模式), none(无检查)
     */
    private String permissionMode = "loose";

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public boolean isIncludeHistoryStreams() {
        return includeHistoryStreams;
    }

    public void setIncludeHistoryStreams(boolean includeHistoryStreams) {
        this.includeHistoryStreams = includeHistoryStreams;
    }

    public int getMaxSyncStreams() {
        return maxSyncStreams;
    }

    public void setMaxSyncStreams(int maxSyncStreams) {
        this.maxSyncStreams = maxSyncStreams;
    }

    public long getSyncDelayMs() {
        return syncDelayMs;
    }

    public void setSyncDelayMs(long syncDelayMs) {
        this.syncDelayMs = syncDelayMs;
    }

    public boolean isSyncPublicStreams() {
        return syncPublicStreams;
    }

    public void setSyncPublicStreams(boolean syncPublicStreams) {
        this.syncPublicStreams = syncPublicStreams;
    }

    public String getPermissionMode() {
        return permissionMode;
    }

    public void setPermissionMode(String permissionMode) {
        this.permissionMode = permissionMode;
    }
} 