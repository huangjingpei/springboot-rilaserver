package net.enjoy.springboot.registrationlogin.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamEventMessage {
    private String type = "streamEvent";
    private String event; // "publish_started", "publish_stopped", "play_started", "play_stopped"
    private String streamId;
    private String userId;
    private String pushUrl;
    private String rtmpUrl;
    private String hlsUrl;
    private String flvUrl;
    private String status;
    private Long timestamp;
    /**
     * 同步类型（用于标识状态同步消息）
     */
    private String syncType;
    
    public static StreamEventMessage publishStarted(String streamId, String userId, String pushUrl, String rtmpUrl, String hlsUrl, String flvUrl) {
        StreamEventMessage message = new StreamEventMessage();
        message.setEvent("publish_started");
        message.setStreamId(streamId);
        message.setUserId(userId);
        message.setPushUrl(pushUrl);
        message.setRtmpUrl(rtmpUrl);
        message.setHlsUrl(hlsUrl);
        message.setFlvUrl(flvUrl);
        message.setStatus("PUSHING");
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    public static StreamEventMessage publishStopped(String streamId, String userId) {
        StreamEventMessage message = new StreamEventMessage();
        message.setEvent("publish_stopped");
        message.setStreamId(streamId);
        message.setUserId(userId);
        message.setStatus("STOPPED");
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    public static StreamEventMessage playStarted(String streamId, String userId) {
        StreamEventMessage message = new StreamEventMessage();
        message.setEvent("play_started");
        message.setStreamId(streamId);
        message.setUserId(userId);
        // 播放事件不设置流状态，因为这是观看者事件，不是流状态变更
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    public static StreamEventMessage playStarted(String streamId, String userId, String rtmpUrl, String hlsUrl, String flvUrl) {
        StreamEventMessage message = new StreamEventMessage();
        message.setEvent("play_started");
        message.setStreamId(streamId);
        message.setUserId(userId);
        message.setRtmpUrl(rtmpUrl);
        message.setHlsUrl(hlsUrl);
        message.setFlvUrl(flvUrl);
        // 播放事件不设置流状态，因为这是观看者事件，不是流状态变更
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    public static StreamEventMessage playStopped(String streamId, String userId) {
        StreamEventMessage message = new StreamEventMessage();
        message.setEvent("play_stopped");
        message.setStreamId(streamId);
        message.setUserId(userId);
        // 播放停止事件不设置流状态，因为这是观看者事件，不是流状态变更
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
} 