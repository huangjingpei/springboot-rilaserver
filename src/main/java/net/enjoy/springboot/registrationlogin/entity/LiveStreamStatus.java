package net.enjoy.springboot.registrationlogin.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "live_stream_status")
@Data
public class LiveStreamStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    @Column(name = "client_id", nullable = false)
    private String clientId;
    
    @Column(name = "platform", length = 50)
    private String platform;
    
    @Column(name = "anchor_name", length = 255)
    private String anchorName;
    
    @Column(name = "is_live")
    private Boolean isLive = false;
    
    @Column(name = "title", length = 500)
    private String title;
    
    @Column(name = "quality", length = 20)
    private String quality;
    
    @Column(name = "m3u8_url", length = 2048)
    private String m3u8Url;
    
    @Column(name = "flv_url", length = 2048)
    private String flvUrl;
    
    @Column(name = "record_url", length = 2048)
    private String recordUrl;
    
    @Column(name = "cookies", columnDefinition = "TEXT")
    private String cookies;
    
    @Column(name = "token", columnDefinition = "TEXT")
    private String token;
    
    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;
    
    @Column(name = "status_code")
    private Integer statusCode = 0;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;
    
    /**
     * 检查是否成功获取到流信息
     */
    public boolean isSuccess() {
        return statusCode == 0 && isLive != null && isLive;
    }
    
    /**
     * 获取主要播放地址（优先M3U8，其次FLV）
     */
    public String getPrimaryPlayUrl() {
        if (m3u8Url != null && !m3u8Url.trim().isEmpty()) {
            return m3u8Url;
        }
        return flvUrl;
    }
}
