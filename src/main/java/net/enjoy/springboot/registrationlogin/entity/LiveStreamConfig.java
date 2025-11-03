package net.enjoy.springboot.registrationlogin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "live_stream_configs")
@Data
public class LiveStreamConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "客户端ID不能为空")
    @Size(max = 255, message = "客户端ID长度不能超过255个字符")
    @Column(name = "client_id", nullable = false)
    private String clientId;
    
    @NotBlank(message = "直播间地址不能为空")
    @Size(max = 1024, message = "直播间地址长度不能超过1024个字符")
    @Column(name = "live_url", nullable = false)
    private String liveUrl;
    
    @NotBlank(message = "直播间名称不能为空")
    @Size(max = 255, message = "直播间名称长度不能超过255个字符")
    @Column(name = "stream_name", nullable = false)
    private String streamName;
    
    @NotNull(message = "直播开始时间不能为空")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @NotNull(message = "直播结束时间不能为空")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @NotBlank(message = "直播平台不能为空")
    @Size(max = 50, message = "直播平台长度不能超过50个字符")
    @Column(name = "platform", nullable = false)
    private String platform;
    
    @Size(max = 20, message = "清晰度长度不能超过20个字符")
    @Column(name = "quality")
    private String quality = "HD";
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_live")
    private Boolean isLive = false;
    
    @Column(name = "last_check_time")
    private java.time.LocalDateTime lastCheckTime;
    
    @Column(name = "last_update_time")
    private java.time.LocalDateTime lastUpdateTime;
    
    @Size(max = 255, message = "主播名称长度不能超过255个字符")
    @Column(name = "anchor_name")
    private String anchorName;
    
    @Size(max = 500, message = "直播标题长度不能超过500个字符")
    @Column(name = "title")
    private String title;
    
    @Size(max = 2048, message = "M3U8地址长度不能超过2048个字符")
    @Column(name = "m3u8_url")
    private String m3u8Url;
    
    @Size(max = 2048, message = "FLV地址长度不能超过2048个字符")
    @Column(name = "flv_url")
    private String flvUrl;
    
    @Size(max = 2048, message = "录播地址长度不能超过2048个字符")
    @Column(name = "record_url")
    private String recordUrl;
    
    @Lob
    @Column(name = "cookies")
    private String cookies;
    
    @Lob
    @Column(name = "token")
    private String token;
    
    @Lob
    @Column(name = "extra_data")
    private String extraData;
    
    @Lob
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    /**
     * 检查当前时间是否在直播时间范围内
     */
    public boolean isInLiveTimeRange() {
        LocalTime now = LocalTime.now();
        
        // 如果开始时间小于结束时间，说明是同一天
        if (startTime.isBefore(endTime)) {
            return !now.isBefore(startTime) && !now.isAfter(endTime);
        } else {
            // 如果开始时间大于结束时间，说明跨天（比如22:00-06:00）
            return !now.isBefore(startTime) || !now.isAfter(endTime);
        }
    }
    
    /**
     * 检查是否需要更新（距离上次检查超过1分钟）
     */
    public boolean needsUpdate() {
        if (lastCheckTime == null) {
            return true;
        }
        
        long minutesSinceLastCheck = ChronoUnit.MINUTES.between(lastCheckTime, java.time.LocalDateTime.now());
        return minutesSinceLastCheck >= 1;
    }
}
