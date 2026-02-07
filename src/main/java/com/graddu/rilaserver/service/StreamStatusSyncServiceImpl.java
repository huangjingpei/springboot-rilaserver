package net.enjoy.springboot.registrationlogin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.enjoy.springboot.registrationlogin.entity.StreamInfo;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.model.StreamEventMessage;
import net.enjoy.springboot.registrationlogin.repository.StreamInfoRepository;
import net.enjoy.springboot.registrationlogin.repository.UserRepository;
import net.enjoy.springboot.registrationlogin.config.StreamSyncConfig;
import org.springframework.scheduling.annotation.Async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import net.enjoy.springboot.registrationlogin.config.ZLMediaKitConfig;

/**
 * 流状态同步服务实现
 */
@Service
public class StreamStatusSyncServiceImpl implements StreamStatusSyncService {
    
    private static final Logger log = LoggerFactory.getLogger(StreamStatusSyncServiceImpl.class);
    
    @Autowired
    private StreamInfoRepository streamInfoRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private StreamSyncConfig streamSyncConfig;

    @Autowired
    private ZLMediaKitConfig zlmediaKitConfig;
    
    @Override
    public void syncCurrentStreamStatus(WebSocketSession session, String userId, String roomId) {
        // 检查是否启用状态同步
        if (!streamSyncConfig.isEnabled()) {
            log.debug("流状态同步已禁用，跳过同步: userId={}", userId);
            return;
        }
        
        // 异步执行同步，避免阻塞WebSocket连接建立
        if (streamSyncConfig.getSyncDelayMs() > 0) {
            syncCurrentStreamStatusAsync(session, userId, roomId);
        } else {
            doSyncCurrentStreamStatus(session, userId, roomId);
        }
    }
    
    /**
     * 异步执行状态同步
     */
    @Async("taskExecutor")
    public void syncCurrentStreamStatusAsync(WebSocketSession session, String userId, String roomId) {
        try {
            // 延迟执行，确保WebSocket连接完全建立
            Thread.sleep(streamSyncConfig.getSyncDelayMs());
            doSyncCurrentStreamStatus(session, userId, roomId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("状态同步延迟被中断: userId={}", userId);
        } catch (Exception e) {
            log.error("异步状态同步失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 执行实际的状态同步逻辑
     */
    private void doSyncCurrentStreamStatus(WebSocketSession session, String userId, String roomId) {
        try {
            log.info("为新加入客户端同步推流状态: userId={}, roomId={}, strategy={}", 
                userId, roomId, streamSyncConfig.getStrategy());
            
            switch (streamSyncConfig.getStrategy()) {
                case "user_only":
                    syncUserStreamStatus(session, userId);
                    break;
                case "public_all":
                    syncUserAndPublicStreams(session, userId);
                    break;
                case "room_based":
                    syncRoomStreamStatus(session, userId, roomId);
                    break;
                default:
                    syncUserStreamStatus(session, userId);
                    break;
            }
            
        } catch (Exception e) {
            log.error("同步推流状态失败: userId={}, roomId={}, error={}", userId, roomId, e.getMessage(), e);
        }
    }
    
    @Override
    public void syncUserStreamStatus(WebSocketSession session, String targetUserId) {
        try {
            log.info("同步指定用户推流状态: targetUserId={}, sessionId={}", targetUserId, session.getId());
            
            // 查找目标用户的活跃推流
            List<StreamInfo> activeStreams = streamInfoRepository.findActiveStreamsByUserId(targetUserId);
            
            for (StreamInfo streamInfo : activeStreams) {
                sendStreamStatusToClient(session, streamInfo, "stream_status_sync");
            }
            
            log.info("已同步用户推流状态: targetUserId={}, streamCount={}", targetUserId, activeStreams.size());
            
        } catch (Exception e) {
            log.error("同步用户推流状态失败: targetUserId={}, error={}", targetUserId, e.getMessage(), e);
        }
    }
    
    @Override
    public void syncAllActiveStreams(WebSocketSession session) {
        try {
            log.info("同步所有活跃推流状态: sessionId={}", session.getId());
            
            // 查找所有活跃推流
            List<StreamInfo> activeStreams = streamInfoRepository.findActiveStreams();
            
            for (StreamInfo streamInfo : activeStreams) {
                sendStreamStatusToClient(session, streamInfo, "stream_status_sync");
            }
            
            log.info("已同步所有活跃推流状态: streamCount={}", activeStreams.size());
            
        } catch (Exception e) {
            log.error("同步所有活跃推流状态失败: error={}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean hasViewPermission(String userId, String streamUserId) {
        try {
            // 1. 用户总是可以查看自己的推流状态
            if (userId.equals(streamUserId)) {
                return true;
            }
            
            // 2. 检查用户是否存在且状态正常
            User user = userRepository.findByUserId(userId);
            if (user == null) {
                return false;
            }
            
            // 3. 这里可以扩展更复杂的权限逻辑
            // 例如：好友关系、同组用户、管理员权限等
            
            // 4. 目前默认允许查看所有公开推流（可根据业务需求调整）
            return true;
            
        } catch (Exception e) {
            log.error("检查查看权限失败: userId={}, streamUserId={}, error={}", userId, streamUserId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 同步房间相关的推流状态
     */
    private void syncRoomStreamStatus(WebSocketSession session, String userId, String roomId) {
        try {
            // 这里可以根据房间概念来同步相关用户的推流
            // 当前项目中房间主要用于WebSocket管理，可以考虑以下策略：
            
            // 策略1：同步用户自己的推流状态
            syncUserStreamStatus(session, userId);
            
            // 策略2：如果需要，可以扩展为同步房间内所有用户的推流状态
            // 但需要先定义房间与用户推流的关联关系
            
            log.info("已同步房间推流状态: userId={}, roomId={}", userId, roomId);
            
        } catch (Exception e) {
            log.error("同步房间推流状态失败: userId={}, roomId={}, error={}", userId, roomId, e.getMessage(), e);
        }
    }
    
    /**
     * 同步用户自己的推流状态 + 公开推流状态
     */
    private void syncUserAndPublicStreams(WebSocketSession session, String userId) {
        try {
            // 1. 同步用户自己的推流状态
            List<StreamInfo> userStreams = streamInfoRepository.findActiveStreamsByUserId(userId);
            for (StreamInfo streamInfo : userStreams) {
                sendStreamStatusToClient(session, streamInfo, "user_stream_sync");
            }
            
            // 2. 同步其他用户的公开推流状态（如果有权限）
            List<StreamInfo> allActiveStreams = streamInfoRepository.findActiveStreams();
            int publicStreamCount = 0;
            
            for (StreamInfo streamInfo : allActiveStreams) {
                // 跳过用户自己的推流（已经同步过）
                if (userId.equals(streamInfo.getUserId())) {
                    continue;
                }
                
                // 检查是否有查看权限
                if (hasViewPermission(userId, streamInfo.getUserId())) {
                    sendStreamStatusToClient(session, streamInfo, "public_stream_sync");
                    publicStreamCount++;
                }
            }
            
            log.info("已同步用户和公开推流状态: userId={}, userStreams={}, publicStreams={}", 
                userId, userStreams.size(), publicStreamCount);
            
        } catch (Exception e) {
            log.error("同步用户和公开推流状态失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 向客户端发送推流状态消息
     */
    private void sendStreamStatusToClient(WebSocketSession session, StreamInfo streamInfo, String syncType) {
        if (!session.isOpen()) {
            log.warn("WebSocket会话已关闭，跳过状态同步: sessionId={}", session.getId());
            return;
        }
        
        try {
            // 使用配置的动态主机地址生成各种播放URL
            String rtmpUrl = zlmediaKitConfig.generatePlayUrl("live", streamInfo.getStreamId());
            String hlsUrl = zlmediaKitConfig.generateHlsUrl("live", streamInfo.getStreamId());
            String flvUrl = zlmediaKitConfig.generateFlvUrl("live", streamInfo.getStreamId());
            
            // 只同步推流中的状态，不再同步播放状态
            StreamEventMessage message;
            if (streamInfo.getStatus() == StreamInfo.StreamStatus.PUSHING) {
                message = StreamEventMessage.publishStarted(
                    streamInfo.getStreamId(),
                    streamInfo.getUserId(),
                    streamInfo.getPushUrl(),
                    rtmpUrl,
                    hlsUrl,
                    flvUrl
                );
                // 标记为状态同步消息
                message.setSyncType(syncType);
                // 设置时间戳
                message.setTimestamp(convertToTimestamp(streamInfo.getStartTime()));
                
            } else {
                // 其他状态（包括PLAYING）暂不同步
                // 因为流状态应该只反映推流者状态，不应该有PLAYING状态
                log.debug("跳过非推流状态的同步: streamId={}, status={}", streamInfo.getStreamId(), streamInfo.getStatus());
                return;
            }
            
            String messageJson = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(messageJson));
            
            log.debug("已发送推流状态同步消息: streamId={}, userId={}, syncType={}, sessionId={}", 
                streamInfo.getStreamId(), streamInfo.getUserId(), syncType, session.getId());
                
        } catch (IOException e) {
            log.error("发送推流状态同步消息失败: streamId={}, userId={}, sessionId={}, error={}", 
                streamInfo.getStreamId(), streamInfo.getUserId(), session.getId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("创建推流状态同步消息失败: streamId={}, userId={}, error={}", 
                streamInfo.getStreamId(), streamInfo.getUserId(), e.getMessage(), e);
        }
    }
    
    /**
     * 将LocalDateTime转换为时间戳（毫秒）
     */
    private Long convertToTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return net.enjoy.springboot.registrationlogin.config.TimeZoneConfig.toTimestamp(dateTime);
    }
} 