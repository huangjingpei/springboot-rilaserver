package com.graddu.rilaserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import com.graddu.rilaserver.entity.ProxyClientConfig;

@Data
public class ProxyClientConfigDto {

    private String userId;
    private String liveUrl;
    private String platform;
    private String quality;
    private DataDto data;

    @lombok.Data
    public static class DataDto {
        private String platform;
        @JsonProperty("anchor_name")
        private String anchorName;
        @JsonProperty("is_live")
        private boolean isLive;
        private String title;
        private String quality;
        @JsonProperty("m3u8_url")
        private String m3u8Url;
        @JsonProperty("flv_url")
        private String flvUrl;
        @JsonProperty("record_url")
        private String recordUrl;
        @JsonProperty("new_cookies")
        private String newCookies;
        @JsonProperty("new_token")
        private String newToken;
        private Object extra;
    }

    public ProxyClientConfig toEntity() {
        ProxyClientConfig entity = new ProxyClientConfig();
        entity.setUserId(this.userId);
        entity.setLiveUrl(this.liveUrl);
        entity.setPlatformId(this.platform);
        entity.setQuality(this.quality);

        if (this.data != null) {
            entity.setPlatformName(this.data.getPlatform());
            entity.setAnchorName(this.data.getAnchorName());
            entity.setIsLive(this.data.isLive());
            entity.setTitle(this.data.getTitle());
            entity.setM3u8Url(this.data.getM3u8Url());
            entity.setFlvUrl(this.data.getFlvUrl());
            entity.setRecordUrl(this.data.getRecordUrl());
            entity.setCookies(this.data.getNewCookies());
            entity.setToken(this.data.getNewToken());
            try {
                entity.setExtraData(new ObjectMapper().writeValueAsString(this.data.getExtra()));
            } catch (Exception e) {
                entity.setExtraData(null);
            }
        }
        return entity;
    }

    public static ProxyClientConfigDto fromEntity(ProxyClientConfig entity) {
        ProxyClientConfigDto dto = new ProxyClientConfigDto();
        dto.setUserId(entity.getUserId());
        dto.setLiveUrl(entity.getLiveUrl());
        dto.setPlatform(entity.getPlatformId());
        dto.setQuality(entity.getQuality());

        DataDto dataDto = new DataDto();
        dataDto.setPlatform(entity.getPlatformName());
        dataDto.setAnchorName(entity.getAnchorName());
        dataDto.setLive(entity.getIsLive());
        dataDto.setTitle(entity.getTitle());
        dataDto.setQuality(entity.getQuality());
        dataDto.setM3u8Url(entity.getM3u8Url());
        dataDto.setFlvUrl(entity.getFlvUrl());
        dataDto.setRecordUrl(entity.getRecordUrl());
        dataDto.setNewCookies(entity.getCookies());
        dataDto.setNewToken(entity.getToken());
        try {
            if (entity.getExtraData() != null) {
                dataDto.setExtra(new ObjectMapper().readValue(entity.getExtraData(), Object.class));
            }
        } catch (Exception e) {
            dataDto.setExtra(null);
        }
        dto.setData(dataDto);
        return dto;
    }
}




