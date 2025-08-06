package net.enjoy.springboot.registrationlogin.dto;

import net.enjoy.springboot.registrationlogin.entity.App;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AppDto {
    private Long id;
    private String name;
    private String description;
    private String shortDescription;
    private String appIcon;
    private List<String> screenshots;
    private BigDecimal rating;
    private Integer ratingCount;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private App.AppType type;
    private List<App.Platform> platforms;
    private Long downloadCount;
    private String fileSize;
    private String version;
    private String developer;
    private LocalDateTime releaseDate;
    private LocalDateTime lastUpdated;
    private Boolean isFeatured;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AppDownloadDto> downloads;
    private List<AppReviewDto> reviews;

    // 构造函数
    public AppDto() {}

    public AppDto(App app) {
        this.id = app.getId();
        this.name = app.getName();
        this.description = app.getDescription();
        this.shortDescription = app.getShortDescription();
        this.appIcon = app.getAppIcon();
        this.screenshots = app.getScreenshots();
        this.rating = app.getRating();
        this.ratingCount = app.getRatingCount();
        this.price = app.getPrice();
        this.originalPrice = app.getOriginalPrice();
        this.type = app.getType();
        this.platforms = app.getPlatforms();
        this.downloadCount = app.getDownloadCount();
        this.fileSize = app.getFileSize();
        this.version = app.getVersion();
        this.developer = app.getDeveloper();
        this.releaseDate = app.getReleaseDate();
        this.lastUpdated = app.getLastUpdated();
        this.isFeatured = app.getIsFeatured();
        this.isActive = app.getIsActive();
        this.createdAt = app.getCreatedAt();
        this.updatedAt = app.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public List<String> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<String> screenshots) {
        this.screenshots = screenshots;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public App.AppType getType() {
        return type;
    }

    public void setType(App.AppType type) {
        this.type = type;
    }

    public List<App.Platform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<App.Platform> platforms) {
        this.platforms = platforms;
    }

    public Long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
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

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public List<AppDownloadDto> getDownloads() {
        return downloads;
    }

    public void setDownloads(List<AppDownloadDto> downloads) {
        this.downloads = downloads;
    }

    public List<AppReviewDto> getReviews() {
        return reviews;
    }

    public void setReviews(List<AppReviewDto> reviews) {
        this.reviews = reviews;
    }
} 