package com.graddu.rilaserver.dto;

import com.graddu.rilaserver.entity.App;
import com.graddu.rilaserver.entity.AppDownload;
import java.time.LocalDateTime;

public class AppDownloadDto {
    private Long id;
    private Long appId;
    private App.Platform platform;
    private String downloadUrl;
    private String fileSize;
    private String version;
    private String minimumOsVersion;
    private Boolean isActive;
    private Long downloadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public AppDownloadDto() {}

    public AppDownloadDto(AppDownload appDownload) {
        this.id = appDownload.getId();
        this.appId = appDownload.getApp().getId();
        this.platform = appDownload.getPlatform();
        this.downloadUrl = appDownload.getDownloadUrl();
        this.fileSize = appDownload.getFileSize();
        this.version = appDownload.getVersion();
        this.minimumOsVersion = appDownload.getMinimumOsVersion();
        this.isActive = appDownload.getIsActive();
        this.downloadCount = appDownload.getDownloadCount();
        this.createdAt = appDownload.getCreatedAt();
        this.updatedAt = appDownload.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public App.Platform getPlatform() {
        return platform;
    }

    public void setPlatform(App.Platform platform) {
        this.platform = platform;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMinimumOsVersion() {
        return minimumOsVersion;
    }

    public void setMinimumOsVersion(String minimumOsVersion) {
        this.minimumOsVersion = minimumOsVersion;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
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