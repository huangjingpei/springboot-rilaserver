package net.enjoy.springboot.registrationlogin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "apps")
public class App {
    
    // 应用类型枚举
    public enum AppType {
        GAME("游戏"),
        TOOL("工具"),
        SOCIAL("社交"),
        EDUCATION("教育"),
        ENTERTAINMENT("娱乐"),
        PRODUCTIVITY("生产力"),
        UTILITY("实用工具"),
        OTHER("其他");
        
        private final String displayName;
        
        AppType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 平台枚举
    public enum Platform {
        WINDOWS("Windows"),
        MAC("macOS"),
        LINUX("Linux"),
        ANDROID("Android"),
        IOS("iOS"),
        WEB("Web"),
        CROSS_PLATFORM("跨平台");
        
        private final String displayName;
        
        Platform(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "应用标识符不能为空")
    @Size(max = 100, message = "应用标识符长度不能超过100个字符")
    @Column(unique = true)
    private String appId; // 应用唯一标识符

    @NotBlank(message = "应用名称不能为空")
    @Size(max = 200, message = "应用名称长度不能超过200个字符")
    private String name; // 应用显示名称

    @Size(max = 500, message = "应用描述长度不能超过500个字符")
    private String description; // 应用描述

    @Size(max = 100, message = "应用版本长度不能超过100个字符")
    private String currentVersion; // 当前版本

    @Size(max = 100, message = "最低版本长度不能超过100个字符")
    private String minVersion; // 最低支持版本

    @Size(max = 100, message = "推荐版本长度不能超过100个字符")
    private String recommendedVersion; // 推荐版本

    @Size(max = 200, message = "应用图标URL长度不能超过200个字符")
    private String iconUrl; // 应用图标URL

    @Size(max = 200, message = "应用官网URL长度不能超过200个字符")
    private String websiteUrl; // 应用官网URL

    @Size(max = 100, message = "开发者名称长度不能超过100个字符")
    private String developer; // 开发者名称

    @Size(max = 200, message = "开发者邮箱长度不能超过200个字符")
    private String developerEmail; // 开发者邮箱

    @Size(max = 200, message = "许可证信息长度不能超过200个字符")
    private String license; // 许可证信息

    @Column(columnDefinition = "TEXT")
    private String features; // 应用特性（JSON格式）

    @Column(columnDefinition = "TEXT")
    private String changelog; // 更新日志

    @NotNull(message = "是否激活不能为空")
    private Boolean isActive = true; // 是否激活

    @NotNull(message = "是否公开不能为空")
    private Boolean isPublic = true; // 是否公开

    @NotNull(message = "是否强制更新不能为空")
    private Boolean isMandatoryUpdate = false; // 是否强制更新

    @Size(max = 50, message = "应用分类长度不能超过50个字符")
    private String category; // 应用分类

    @Size(max = 50, message = "应用标签长度不能超过50个字符")
    private String tags; // 应用标签（逗号分隔）

    @Enumerated(EnumType.STRING)
    private AppType appType; // 应用类型

    @Enumerated(EnumType.STRING)
    private Platform platform; // 支持平台

    private Boolean isFree = true; // 是否免费
    private Boolean isFeatured = false; // 是否推荐
    private Long downloadCount = 0L; // 下载次数
    private Double rating = 0.0; // 评分
    private Long ratingCount = 0L; // 评分次数

    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastUpdateDate; // 最后更新时间

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (lastUpdateDate == null) {
            lastUpdateDate = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    public String getRecommendedVersion() {
        return recommendedVersion;
    }

    public void setRecommendedVersion(String recommendedVersion) {
        this.recommendedVersion = recommendedVersion;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getDeveloperEmail() {
        return developerEmail;
    }

    public void setDeveloperEmail(String developerEmail) {
        this.developerEmail = developerEmail;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getIsMandatoryUpdate() {
        return isMandatoryUpdate;
    }

    public void setIsMandatoryUpdate(Boolean mandatoryUpdate) {
        isMandatoryUpdate = mandatoryUpdate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public AppType getAppType() {
        return appType;
    }

    public void setAppType(AppType appType) {
        this.appType = appType;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Instant lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
} 