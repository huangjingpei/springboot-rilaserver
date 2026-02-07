package net.enjoy.springboot.registrationlogin.dto;

import lombok.Data;

@Data
public class StreamFetchResult {
    private Integer code;
    private String message;
    private String platform;
    private String quality;
    private StreamData data;
    
    @Data
    public static class StreamData {
        private String platform;
        private String anchor_name;
        private Boolean is_live;
        private String title;
        private String quality;
        private String m3u8_url;
        private String flv_url;
        private String record_url;
        private String new_cookies;
        private String new_token;
        private String extra;
    }
    
    public boolean isSuccess() {
        return code != null && code == 0;
    }
}

