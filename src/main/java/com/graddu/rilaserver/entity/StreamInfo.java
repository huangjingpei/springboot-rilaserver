package com.graddu.rilaserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "stream_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "stream_id", unique = true, nullable = false)
    private String streamId; // 流ID，如 "live/test123"
    
    @Column(name = "app_name")
    private String appName; // 应用名，如 "live"
    
    @Column(name = "stream_name")
    private String streamName; // 流名，如 "test123"
    
    @Column(name = "user_id")
    private String userId; // 推流用户ID
    
    @Column(name = "push_url")
    private String pushUrl; // 推流地址
    
    @Column(name = "play_url")
    private String playUrl; // 播放地址
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StreamStatus status; // 流状态
    
    @Column(name = "start_time")
    private LocalDateTime startTime; // 开始时间
    
    @Column(name = "end_time")
    private LocalDateTime endTime; // 结束时间
    
    @Column(name = "duration")
    private Long duration; // 持续时间（秒）
    
    @Column(name = "viewer_count")
    private Integer viewerCount; // 观看人数
    
    @Column(name = "bitrate")
    private Integer bitrate; // 码率
    
    @Column(name = "resolution")
    private String resolution; // 分辨率
    
    @Column(name = "ip_address")
    private String ipAddress; // 推流IP地址
    
    @Column(name = "user_agent")
    private String userAgent; // 用户代理
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum StreamStatus {
        CREATED,    // 已创建
        PUSHING,    // 推流中
        PLAYING,    // 播放中
        STOPPED,    // 已停止
        ERROR       // 错误
    }
} 