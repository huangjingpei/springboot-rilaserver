package net.enjoy.springboot.registrationlogin.service;

import org.springframework.web.socket.WebSocketSession;

/**
 * 流状态同步服务接口
 * 用于处理新加入客户端的推流状态同步
 */
public interface StreamStatusSyncService {
    
    /**
     * 为新加入的客户端同步当前的推流状态
     * @param session WebSocket会话
     * @param userId 用户ID
     * @param roomId 房间ID（可选，如果需要按房间同步）
     */
    void syncCurrentStreamStatus(WebSocketSession session, String userId, String roomId);
    
    /**
     * 为新加入的客户端同步指定用户的推流状态
     * @param session WebSocket会话
     * @param targetUserId 目标用户ID
     */
    void syncUserStreamStatus(WebSocketSession session, String targetUserId);
    
    /**
     * 为新加入的客户端同步所有活跃推流状态
     * @param session WebSocket会话
     */
    void syncAllActiveStreams(WebSocketSession session);
    
    /**
     * 检查用户是否有查看指定流状态的权限
     * @param userId 请求用户ID
     * @param streamUserId 流所属用户ID
     * @return 是否有权限
     */
    boolean hasViewPermission(String userId, String streamUserId);
} 