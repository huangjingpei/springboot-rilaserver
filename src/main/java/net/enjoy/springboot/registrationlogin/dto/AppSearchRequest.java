package net.enjoy.springboot.registrationlogin.dto;

import net.enjoy.springboot.registrationlogin.entity.App;
import java.math.BigDecimal;
import java.util.List;

public class AppSearchRequest {
    private String keyword;
    private App.AppType type;  // 单个类型
    private App.Platform platform;  // 单个平台
    private List<App.AppType> types;
    private List<App.Platform> platforms;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private BigDecimal maxRating;
    private Boolean isFree;
    private Boolean isFeatured;
    private String sortBy; // "rating", "price", "downloadCount", "createdAt"
    private String sortOrder; // "asc", "desc"
    private Integer page = 0;
    private Integer size = 20;

    // 构造函数
    public AppSearchRequest() {}

    // Getters and Setters
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public App.AppType getType() {
        return type;
    }

    public void setType(App.AppType type) {
        this.type = type;
    }

    public App.Platform getPlatform() {
        return platform;
    }

    public void setPlatform(App.Platform platform) {
        this.platform = platform;
    }

    public List<App.AppType> getTypes() {
        return types;
    }

    public void setTypes(List<App.AppType> types) {
        this.types = types;
    }

    public List<App.Platform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<App.Platform> platforms) {
        this.platforms = platforms;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getMinRating() {
        return minRating;
    }

    public void setMinRating(BigDecimal minRating) {
        this.minRating = minRating;
    }

    public BigDecimal getMaxRating() {
        return maxRating;
    }

    public void setMaxRating(BigDecimal maxRating) {
        this.maxRating = maxRating;
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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
} 