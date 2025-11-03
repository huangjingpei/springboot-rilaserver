package net.enjoy.springboot.registrationlogin.dto;

import lombok.Data;

@Data
public class PythonScriptResult {
    
    private Integer code;
    private String message;
    private String platform;
    private String quality;
    private StreamData data;
    
    @Data
    public static class StreamData {
        private String platform;
        private String anchorName;
        private Boolean isLive;
        private String title;
        private String quality;
        private String m3u8Url;
        private String flvUrl;
        private String recordUrl;
        private String newCookies;
        private String newToken;
        private String extra;
    }
    
    /**
     * 检查是否执行成功
     */
    public boolean isSuccess() {
        return code != null && code == 0;
    }
    
    /**
     * 检查是否正在直播
     */
    public boolean isLive() {
        return isSuccess() && data != null && data.getIsLive() != null && data.getIsLive();
    }
}
