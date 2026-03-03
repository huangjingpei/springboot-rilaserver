package com.graddu.rilaserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class LiveStreamTaskDto {
    
    private Long id;
    
    @NotBlank(message = "客户端ID不能为空")
    private String clientId;
    
    @NotNull(message = "任务索引不能为空")
    private Integer taskIndex;
    
    @NotBlank(message = "直播间地址不能为空")
    private String liveUrl;
    
    private String roomName;
    private String platform;
    private String quality = "HD";
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive = true;
    private Integer checkInterval = 60;
    
    // 状态信息
    private Boolean isLive;
    private String title;
    private String anchorName;
    private String m3u8Url;
    private String flvUrl;
    private String recordUrl;
    private Integer statusCode;
    private String errorMessage;
    private java.time.LocalDateTime lastCheckTime;
    private java.time.LocalDateTime nextCheckTime;
    
    /**
     * Excel表格上传的批量任务DTO
     */
    @Data
    public static class BatchTaskDto {
        @NotBlank(message = "客户端ID不能为空")
        private String clientId;
        
        @NotNull(message = "任务列表不能为空")
        private List<LiveStreamTaskDto> tasks;
    }
    
    /**
     * 任务状态查询响应DTO
     */
    @Data
    public static class StatusResponseDto {
        private String clientId;
        private List<LiveStreamTaskDto> tasks;
        private Integer totalCount;
        private Integer activeCount;
        private Integer liveCount;
    }
}
