package net.enjoy.springboot.registrationlogin.dto;

import net.enjoy.springboot.registrationlogin.entity.AppReview;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AppReviewDto {
    private Long id;
    private Long appId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private BigDecimal rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isHelpful;
    private Integer helpfulCount;
    private Boolean isVerifiedPurchase;

    // 构造函数
    public AppReviewDto() {}

    public AppReviewDto(AppReview appReview) {
        this.id = appReview.getId();
        this.appId = appReview.getApp().getId();
        this.userId = appReview.getUser().getId();
        this.userName = appReview.getUser().getName();
        this.userAvatar = appReview.getUser().getName(); // 假设User实体有avatar字段
        this.rating = appReview.getRating();
        this.comment = appReview.getComment();
        this.createdAt = appReview.getCreatedAt();
        this.updatedAt = appReview.getUpdatedAt();
        this.isHelpful = appReview.getIsHelpful();
        this.helpfulCount = appReview.getHelpfulCount();
        this.isVerifiedPurchase = appReview.getIsVerifiedPurchase();
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public Boolean getIsHelpful() {
        return isHelpful;
    }

    public void setIsHelpful(Boolean isHelpful) {
        this.isHelpful = isHelpful;
    }

    public Integer getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public Boolean getIsVerifiedPurchase() {
        return isVerifiedPurchase;
    }

    public void setIsVerifiedPurchase(Boolean isVerifiedPurchase) {
        this.isVerifiedPurchase = isVerifiedPurchase;
    }
} 