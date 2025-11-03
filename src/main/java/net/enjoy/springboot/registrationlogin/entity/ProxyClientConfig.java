package net.enjoy.springboot.registrationlogin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "proxy_client_configs")
@Data
public class ProxyClientConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Size(max = 1024)
    @Column(name = "live_url")
    private String liveUrl;

    @Size(max = 50)
    @Column(name = "platform_id")
    private String platformId;

    @Size(max = 100)
    @Column(name = "platform_name")
    private String platformName;

    @Size(max = 255)
    @Column(name = "anchor_name")
    private String anchorName;

    @Column(name = "is_live")
    private Boolean isLive = false;

    @Size(max = 500)
    private String title;

    @Size(max = 50)
    private String quality;

    @Size(max = 2048)
    @Column(name = "m3u8_url")
    private String m3u8Url;

    @Size(max = 2048)
    @Column(name = "flv_url")
    private String flvUrl;

    @Size(max = 2048)
    @Column(name = "record_url")
    private String recordUrl;

    @Lob
    private String cookies;

    @Lob
    private String token;

    @Lob
    @Column(name = "extra_data")
    private String extraData;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
