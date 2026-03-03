package com.graddu.rilaserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.graddu.rilaserver.entity.LiveStreamConfig;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
public class LiveStreamConfigDto {
    
    private Long id;
    
    @NotBlank(message = "客户端ID不能为空")
    @Size(max = 255, message = "客户端ID长度不能超过255个字符")
    private String clientId;
    
    @NotBlank(message = "直播间地址不能为空")
    @Size(max = 1024, message = "直播间地址长度不能超过1024个字符")
    private String liveUrl;
    
    @NotBlank(message = "直播间名称不能为空")
    @Size(max = 255, message = "直播间名称长度不能超过255个字符")
    private String streamName;
    
    @NotNull(message = "直播开始时间不能为空")
    private String startTime; // 格式: HH:mm:ss
    
    @NotNull(message = "直播结束时间不能为空")
    private String endTime; // 格式: HH:mm:ss
    
    @NotBlank(message = "直播平台不能为空")
    @Size(max = 50, message = "直播平台长度不能超过50个字符")
    private String platform;
    
    @Size(max = 20, message = "清晰度长度不能超过20个字符")
    private String quality = "HD";
    
    private Boolean isActive = true;
    private Boolean isLive = false;
    private String lastCheckTime;
    private String lastUpdateTime;
    private String anchorName;
    private String title;
    private String m3u8Url;
    private String flvUrl;
    private String recordUrl;
    private String cookies;
    private String token;
    private String extraData;
    private String errorMessage;
    private Integer retryCount = 0;
    private String createdAt;
    private String updatedAt;
    
    // 转换为实体
    public LiveStreamConfig toEntity() {
        LiveStreamConfig entity = new LiveStreamConfig();
        entity.setId(this.id);
        entity.setClientId(this.clientId);
        entity.setLiveUrl(this.liveUrl);
        entity.setStreamName(this.streamName);
        entity.setStartTime(LocalTime.parse(this.startTime, DateTimeFormatter.ofPattern("HH:mm:ss")));
        entity.setEndTime(LocalTime.parse(this.endTime, DateTimeFormatter.ofPattern("HH:mm:ss")));
        entity.setPlatform(this.platform);
        entity.setQuality(this.quality);
        entity.setIsActive(this.isActive);
        entity.setIsLive(this.isLive);
        entity.setAnchorName(this.anchorName);
        entity.setTitle(this.title);
        entity.setM3u8Url(this.m3u8Url);
        entity.setFlvUrl(this.flvUrl);
        entity.setRecordUrl(this.recordUrl);
        entity.setCookies(this.cookies);
        entity.setToken(this.token);
        entity.setExtraData(this.extraData);
        entity.setErrorMessage(this.errorMessage);
        entity.setRetryCount(this.retryCount);
        return entity;
    }
    
    // 从实体转换
    public static LiveStreamConfigDto fromEntity(LiveStreamConfig entity) {
        LiveStreamConfigDto dto = new LiveStreamConfigDto();
        dto.setId(entity.getId());
        dto.setClientId(entity.getClientId());
        dto.setLiveUrl(entity.getLiveUrl());
        dto.setStreamName(entity.getStreamName());
        dto.setStartTime(entity.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        dto.setEndTime(entity.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        dto.setPlatform(entity.getPlatform());
        dto.setQuality(entity.getQuality());
        dto.setIsActive(entity.getIsActive());
        dto.setIsLive(entity.getIsLive());
        dto.setLastCheckTime(entity.getLastCheckTime() != null ? 
            entity.getLastCheckTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.setLastUpdateTime(entity.getLastUpdateTime() != null ? 
            entity.getLastUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.setAnchorName(entity.getAnchorName());
        dto.setTitle(entity.getTitle());
        dto.setM3u8Url(entity.getM3u8Url());
        dto.setFlvUrl(entity.getFlvUrl());
        dto.setRecordUrl(entity.getRecordUrl());
        dto.setCookies(entity.getCookies());
        dto.setToken(entity.getToken());
        dto.setExtraData(entity.getExtraData());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setRetryCount(entity.getRetryCount());
        dto.setCreatedAt(entity.getCreatedAt() != null ? 
            entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? 
            entity.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        return dto;
    }
}

