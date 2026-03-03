package com.graddu.rilaserver.service;

import com.graddu.rilaserver.model.StreamEventMessage;

public interface StreamEventService {
    /**
     * 推流开始时通知用户的所有在线设备
     */
    void notifyPublishStarted(String streamId, String userId, String pushUrl, String rtmpUrl, String hlsUrl, String flvUrl);
    
    /**
     * 推流结束时通知用户的所有在线设备
     */
    void notifyPublishStopped(String streamId, String userId);
    
    /**
     * 播放开始时通知用户的所有在线设备
     */
    void notifyPlayStarted(String streamId, String userId);
    
    /**
     * 播放开始时通知用户的所有在线设备（带URL参数）
     */
    void notifyPlayStarted(String streamId, String userId, String rtmpUrl, String hlsUrl, String flvUrl);
    
    /**
     * 播放结束时通知用户的所有在线设备
     */
    void notifyPlayStopped(String streamId, String userId);
} 