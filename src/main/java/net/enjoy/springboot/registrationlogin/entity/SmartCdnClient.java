package net.enjoy.springboot.registrationlogin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "smartcdn_client")
public class SmartCdnClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 128)
    private String clientId;

    @Column(name = "lan_id", nullable = false, length = 128)
    private String lanId;

    @Column(name = "lan_ip", length = 64)
    private String lanIp;

    @Column(name = "mediamtx_http_url", length = 512)
    private String mediamtxHttpUrl;

    @Column(name = "mediamtx_rtmp_url_prefix", length = 512)
    private String mediamtxRtmpUrlPrefix;

    @Column(name = "capabilities", length = 512)
    private String capabilities;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        lastSeenAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getLanId() {
        return lanId;
    }

    public void setLanId(String lanId) {
        this.lanId = lanId;
    }

    public String getLanIp() {
        return lanIp;
    }

    public void setLanIp(String lanIp) {
        this.lanIp = lanIp;
    }

    public String getMediamtxHttpUrl() {
        return mediamtxHttpUrl;
    }

    public void setMediamtxHttpUrl(String mediamtxHttpUrl) {
        this.mediamtxHttpUrl = mediamtxHttpUrl;
    }

    public String getMediamtxRtmpUrlPrefix() {
        return mediamtxRtmpUrlPrefix;
    }

    public void setMediamtxRtmpUrlPrefix(String mediamtxRtmpUrlPrefix) {
        this.mediamtxRtmpUrlPrefix = mediamtxRtmpUrlPrefix;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

