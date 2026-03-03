package com.graddu.rilaserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "live_stream_tasks")
@Data
public class LiveStreamTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "客户端ID不能为空")
    @Column(name = "client_id", nullable = false)
    private String clientId;
    
    @NotNull(message = "任务索引不能为空")
    @Column(name = "task_index", nullable = false)
    private Integer taskIndex;
    
    @NotBlank(message = "直播间地址不能为空")
    @Column(name = "live_url", nullable = false, length = 1024)
    private String liveUrl;
    
    @Column(name = "room_name", length = 255)
    private String roomName;
    
    @Column(name = "platform", length = 50)
    private String platform;
    
    @Column(name = "quality", length = 20)
    private String quality = "HD";
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_check_time")
    private java.time.LocalDateTime lastCheckTime;
    
    @Column(name = "next_check_time")
    private java.time.LocalDateTime nextCheckTime;
    
    @Column(name = "check_interval")
    private Integer checkInterval = 60; // 默认60秒
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    /**
     * 检查是否在直播时间范围内
     */
    public boolean isInLiveTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // 如果没有设置时间范围，则始终允许
        }
        
        LocalTime now = LocalTime.now();
        if (startTime.isBefore(endTime)) {
            // 同一天内的时间范围
            return !now.isBefore(startTime) && !now.isAfter(endTime);
        } else {
            // 跨天的时间范围（如22:00-06:00）
            return !now.isBefore(startTime) || !now.isAfter(endTime);
        }
    }
    
    /**
     * 计算下次检查时间
     */
    public void calculateNextCheckTime() {
        if (lastCheckTime == null) {
            this.nextCheckTime = java.time.LocalDateTime.now();
        } else {
            this.nextCheckTime = lastCheckTime.plus(checkInterval, ChronoUnit.SECONDS);
        }
    }
    
    /**
     * 检查是否需要执行检查
     */
    public boolean needsCheck() {
        return isActive && isInLiveTimeRange() && 
               (nextCheckTime == null || java.time.LocalDateTime.now().isAfter(nextCheckTime));
    }
}
