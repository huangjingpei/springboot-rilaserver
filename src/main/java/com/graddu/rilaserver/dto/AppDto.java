package net.enjoy.springboot.registrationlogin.dto;

import net.enjoy.springboot.registrationlogin.entity.App;
import java.time.Instant;
import java.util.List;

public class AppDto {
    private Long id;
    private String appId;
    private String name;
    private String description;
    private String currentVersion;
    private String minVersion;
    private String recommendedVersion;
    private String iconUrl;
    private String websiteUrl;
    private String developer;
    private String developerEmail;
    private String license;
    private String features;
    private String changelog;
    private Boolean isActive;
    private Boolean isPublic;
    private Boolean isMandatoryUpdate;
    private String category;
    private String tags;
    private App.AppType appType;
    private App.Platform platform;
    private Boolean isFree;
    private Boolean isFeatured;
    private Long downloadCount;
    private Double rating;
    private Long ratingCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastUpdateDate;

    // 构造函数
    public AppDto() {}

    public AppDto(App app) {
        this.id = app.getId();
        this.appId = app.getAppId();
        this.name = app.getName();
        this.description = app.getDescription();
        this.currentVersion = app.getCurrentVersion();
        this.minVersion = app.getMinVersion();
        this.recommendedVersion = app.getRecommendedVersion();
        this.iconUrl = app.getIconUrl();
        this.websiteUrl = app.getWebsiteUrl();
        this.developer = app.getDeveloper();
        this.developerEmail = app.getDeveloperEmail();
        this.license = app.getLicense();
        this.features = app.getFeatures();
        this.changelog = app.getChangelog();
        this.isActive = app.getIsActive();
        this.isPublic = app.getIsPublic();
        this.isMandatoryUpdate = app.getIsMandatoryUpdate();
        this.category = app.getCategory();
        this.tags = app.getTags();
        this.appType = app.getAppType();
        this.platform = app.getPlatform();
        this.isFree = app.getIsFree();
        this.isFeatured = app.getIsFeatured();
        this.downloadCount = app.getDownloadCount();
        this.rating = app.getRating();
        this.ratingCount = app.getRatingCount();
        this.createdAt = app.getCreatedAt();
        this.updatedAt = app.getUpdatedAt();
        this.lastUpdateDate = app.getLastUpdateDate();
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

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public void setIsMandatoryUpdate(Boolean isMandatoryUpdate) {
        this.isMandatoryUpdate = isMandatoryUpdate;
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

    public App.AppType getAppType() {
        return appType;
    }

    public void setAppType(App.AppType appType) {
        this.appType = appType;
    }

    public App.Platform getPlatform() {
        return platform;
    }

    public void setPlatform(App.Platform platform) {
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