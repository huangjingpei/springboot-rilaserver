package net.enjoy.springboot.registrationlogin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.enjoy.springboot.registrationlogin.entity.StreamInfo;
import net.enjoy.springboot.registrationlogin.entity.UserSession;
import net.enjoy.springboot.registrationlogin.model.StreamEventMessage;
import net.enjoy.springboot.registrationlogin.repository.StreamInfoRepository;
import net.enjoy.springboot.registrationlogin.repository.UserSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StreamEventServiceImpl implements StreamEventService {
    
    private static final Logger log = LoggerFactory.getLogger(StreamEventServiceImpl.class);
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    @Autowired
    private StreamInfoRepository streamInfoRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void notifyPublishStarted(String streamId, String userId, String pushUrl, String rtmpUrl, String hlsUrl, String flvUrl) {
        try {
            // 1. 首先更新或创建流信息记录
            StreamInfo streamInfo = createOrUpdateStreamInfo(streamId, userId, pushUrl, rtmpUrl, hlsUrl, flvUrl, StreamInfo.StreamStatus.PUSHING);
            
            // 2. 创建事件消息
            StreamEventMessage message = StreamEventMessage.publishStarted(streamId, userId, pushUrl, rtmpUrl, hlsUrl, flvUrl);
            String messageJson = objectMapper.writeValueAsString(message);
            
            log.info("推流开始: userId={}, streamId={}", userId, streamId);
            
            // 3. 只通知拉流方（观众），不通知推流方（主播）
            // 这里可以通过房间服务获取所有观众，然后向他们发送推流开始通知
            notifyViewersAboutStreamStart(streamId, userId, messageJson);
            
        } catch (Exception e) {
            log.error("处理推流开始通知失败: userId={}, streamId={}, error={}", userId, streamId, e.getMessage(), e);
        }
    }
    
    @Override
    public void notifyPublishStopped(String streamId, String userId) {
        try {
            // 1. 更新流信息状态
            updateStreamInfoStatus(streamId, StreamInfo.StreamStatus.STOPPED);
            
            // 2. 创建事件消息
            StreamEventMessage message = StreamEventMessage.publishStopped(streamId, userId);
            String messageJson = objectMapper.writeValueAsString(message);
            
            log.info("推流结束: userId={}, streamId={}", userId, streamId);
            
            // 3. 只通知拉流方（观众），不通知推流方（主播）
            // 这里可以通过房间服务获取所有观众，然后向他们发送推流结束通知
            notifyViewersAboutStreamStop(streamId, userId, messageJson);
            
        } catch (Exception e) {
            log.error("处理推流结束通知失败: userId={}, streamId={}, error={}", userId, streamId, e.getMessage(), e);
        }
    }
    
    @Override
    public void notifyPlayStarted(String streamId, String userId) {
        try {
            // 注意：不再更新流信息状态，播放不应该改变流的推流状态
            // 流状态应该只反映推流者的状态，而不是观看者的状态
            
            // 创建事件消息
            StreamEventMessage message = StreamEventMessage.playStarted(streamId, userId);
            String messageJson = objectMapper.writeValueAsString(message);
            
            log.info("播放开始通知: userId={}, streamId={}", userId, streamId);
            
            // 向用户的在线设备发送消息
            sendMessageToUserDevices(userId, messageJson, "播放开始");
            
        } catch (Exception e) {
            log.error("处理播放开始通知失败: userId={}, streamId={}, error={}", userId, streamId, e.getMessage(), e);
        }
    }
    
    @Override
    public void notifyPlayStarted(String streamId, String userId, String rtmpUrl, String hlsUrl, String flvUrl) {
        try {
            // 注意：不再更新流信息状态，播放不应该改变流的推流状态
            // 流状态应该只反映推流者的状态，而不是观看者的状态
            
            // 创建事件消息（带URL参数）
            StreamEventMessage message = StreamEventMessage.playStarted(streamId, userId, rtmpUrl, hlsUrl, flvUrl);
            String messageJson = objectMapper.writeValueAsString(message);
            
            log.info("播放开始通知（带URL）: userId={}, streamId={}, rtmpUrl={}, hlsUrl={}, flvUrl={}", 
                    userId, streamId, rtmpUrl, hlsUrl, flvUrl);
            
            // 向用户的在线设备发送消息
            sendMessageToUserDevices(userId, messageJson, "播放开始");
            
        } catch (Exception e) {
            log.error("处理播放开始通知失败: userId={}, streamId={}, error={}", userId, streamId, e.getMessage(), e);
        }
    }
    
    @Override
    public void notifyPlayStopped(String streamId, String userId) {
        try {
            // 注意：不再更新流信息状态，播放停止不应该改变流的推流状态
            // 流状态应该只反映推流者的状态，而不是观看者的状态
            
            // 创建事件消息
            StreamEventMessage message = StreamEventMessage.playStopped(streamId, userId);
            String messageJson = objectMapper.writeValueAsString(message);
            
            log.info("播放结束通知: userId={}, streamId={}", userId, streamId);
            
            // 向用户的在线设备发送消息
            sendMessageToUserDevices(userId, messageJson, "播放结束");
            
        } catch (Exception e) {
            log.error("处理播放结束通知失败: userId={}, streamId={}, error={}", userId, streamId, e.getMessage(), e);
        }
    }
    
    /**
     * 创建或更新流信息记录
     */
    private StreamInfo createOrUpdateStreamInfo(String streamId, String userId, String pushUrl, 
                                               String rtmpUrl, String hlsUrl, String flvUrl, 
                                               StreamInfo.StreamStatus status) {
        // 解析streamId获取appName和streamName
        String[] parts = streamId.split("/", 2);
        String appName = parts.length > 0 ? parts[0] : "live";
        String streamName = parts.length > 1 ? parts[1] : streamId;
        
        // 查找现有记录
        Optional<StreamInfo> existingStream = streamInfoRepository.findByStreamId(streamId);
        
        StreamInfo streamInfo;
        if (existingStream.isPresent()) {
            streamInfo = existingStream.get();
            streamInfo.setStatus(status);
            streamInfo.setUpdatedAt(LocalDateTime.now());
        } else {
            streamInfo = new StreamInfo();
            streamInfo.setStreamId(streamId);
            streamInfo.setAppName(appName);
            streamInfo.setStreamName(streamName);
            streamInfo.setUserId(userId);
            streamInfo.setPushUrl(pushUrl);
            streamInfo.setPlayUrl(rtmpUrl); // 使用RTMP作为主要播放地址
            streamInfo.setStatus(status);
            streamInfo.setStartTime(LocalDateTime.now());
        }
        
        return streamInfoRepository.save(streamInfo);
    }
    
    /**
     * 更新流信息状态
     */
    private void updateStreamInfoStatus(String streamId, StreamInfo.StreamStatus status) {
        Optional<StreamInfo> existingStream = streamInfoRepository.findByStreamId(streamId);
        if (existingStream.isPresent()) {
            StreamInfo streamInfo = existingStream.get();
            streamInfo.setStatus(status);
            streamInfo.setUpdatedAt(LocalDateTime.now());
            
            if (status == StreamInfo.StreamStatus.STOPPED) {
                streamInfo.setEndTime(LocalDateTime.now());
                // 计算持续时间
                if (streamInfo.getStartTime() != null) {
                    long duration = java.time.Duration.between(streamInfo.getStartTime(), streamInfo.getEndTime()).getSeconds();
                    streamInfo.setDuration(duration);
                }
            }
            
            streamInfoRepository.save(streamInfo);
        }
    }
    
    /**
     * 向用户的在线设备发送WebSocket消息
     */
    private void sendMessageToUserDevices(String userId, String messageJson, String eventType) {
        // 获取用户的所有WebSocket连接（通过RoomService）
        List<RoomService.Client> userClients = roomService.getUserClients(userId);
        
        log.debug("向用户设备发送{}消息: userId={}, WebSocket连接数={}", eventType, userId, userClients.size());
        
        // 向用户的所有WebSocket连接发送消息
        for (RoomService.Client client : userClients) {
            try {
                WebSocketSession session = client.getSession();
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(messageJson));
                    log.debug("{}消息已发送: userId={}, deviceId={}", eventType, userId, client.getSessionId());
                }
            } catch (IOException e) {
                log.error("发送{}消息失败: userId={}, deviceId={}, error={}", eventType, userId, client.getSessionId(), e.getMessage());
            }
        }
    }
    
    /**
     * 通知观众推流开始
     */
    private void notifyViewersAboutStreamStart(String streamId, String streamerUserId, String messageJson) {
        try {
            // 从streamId解析房间ID
            String roomId = extractRoomIdFromStreamId(streamId);
            
            int notifiedCount = 0;
            
            if (roomId != null) {
                // 获取房间内所有观众（除了主播）
                List<RoomService.Client> roomClients = roomService.getRoomClients(roomId);
                
                for (RoomService.Client client : roomClients) {
                    // 跳过主播自己
                    if (client.getUserId().equals(streamerUserId)) {
                        continue;
                    }
                    
                    try {
                        WebSocketSession session = client.getSession();
                        if (session != null && session.isOpen()) {
                            session.sendMessage(new TextMessage(messageJson));
                            notifiedCount++;
                            log.debug("推流开始通知已发送给观众: userId={}, deviceId={}", client.getUserId(), client.getSessionId());
                        }
                    } catch (IOException e) {
                        log.error("向观众发送推流开始通知失败: userId={}, deviceId={}, error={}", 
                                client.getUserId(), client.getSessionId(), e.getMessage());
                    }
                }
                
                log.info("推流开始通知已发送给{}个观众: streamId={}, streamerUserId={}, roomId={}", 
                        notifiedCount, streamId, streamerUserId, roomId);
            } else {
                // 如果无法获取房间ID，则向所有在线用户发送通知（除了主播）
                log.warn("无法获取房间ID，向所有在线用户发送推流开始通知: streamId={}, streamerUserId={}", streamId, streamerUserId);
                
                // 获取所有房间的所有客户端
                for (RoomService.Room room : roomService.getRooms().values()) {
                    for (RoomService.Client client : room.getClients()) {
                        // 跳过主播自己
                        if (client.getUserId().equals(streamerUserId)) {
                            continue;
                        }
                        
                        try {
                            WebSocketSession session = client.getSession();
                            if (session != null && session.isOpen()) {
                                session.sendMessage(new TextMessage(messageJson));
                                notifiedCount++;
                                log.debug("推流开始通知已发送给观众: userId={}, deviceId={}", client.getUserId(), client.getSessionId());
                            }
                        } catch (IOException e) {
                            log.error("向观众发送推流开始通知失败: userId={}, deviceId={}, error={}", 
                                    client.getUserId(), client.getSessionId(), e.getMessage());
                        }
                    }
                }
                
                log.info("推流开始通知已发送给{}个观众（全局广播）: streamId={}, streamerUserId={}", 
                        notifiedCount, streamId, streamerUserId);
            }
            
        } catch (Exception e) {
            log.error("通知观众推流开始失败: streamId={}, streamerUserId={}, error={}", streamId, streamerUserId, e.getMessage(), e);
        }
    }
    
    /**
     * 通知观众推流结束
     */
    private void notifyViewersAboutStreamStop(String streamId, String streamerUserId, String messageJson) {
        try {
            // 从streamId解析房间ID
            String roomId = extractRoomIdFromStreamId(streamId);
            
            int notifiedCount = 0;
            
            if (roomId != null) {
                // 获取房间内所有观众（除了主播）
                List<RoomService.Client> roomClients = roomService.getRoomClients(roomId);
                
                for (RoomService.Client client : roomClients) {
                    // 跳过主播自己
                    if (client.getUserId().equals(streamerUserId)) {
                        continue;
                    }
                    
                    try {
                        WebSocketSession session = client.getSession();
                        if (session != null && session.isOpen()) {
                            session.sendMessage(new TextMessage(messageJson));
                            notifiedCount++;
                            log.debug("推流结束通知已发送给观众: userId={}, deviceId={}", client.getUserId(), client.getSessionId());
                        }
                    } catch (IOException e) {
                        log.error("向观众发送推流结束通知失败: userId={}, deviceId={}, error={}", 
                                client.getUserId(), client.getSessionId(), e.getMessage());
                    }
                }
                
                log.info("推流结束通知已发送给{}个观众: streamId={}, streamerUserId={}, roomId={}", 
                        notifiedCount, streamId, streamerUserId, roomId);
            } else {
                // 如果无法获取房间ID，则向所有在线用户发送通知（除了主播）
                log.warn("无法获取房间ID，向所有在线用户发送推流结束通知: streamId={}, streamerUserId={}", streamId, streamerUserId);
                
                // 获取所有房间的所有客户端
                for (RoomService.Room room : roomService.getRooms().values()) {
                    for (RoomService.Client client : room.getClients()) {
                        // 跳过主播自己
                        if (client.getUserId().equals(streamerUserId)) {
                            continue;
                        }
                        
                        try {
                            WebSocketSession session = client.getSession();
                            if (session != null && session.isOpen()) {
                                session.sendMessage(new TextMessage(messageJson));
                                notifiedCount++;
                                log.debug("推流结束通知已发送给观众: userId={}, deviceId={}", client.getUserId(), client.getSessionId());
                            }
                        } catch (IOException e) {
                            log.error("向观众发送推流结束通知失败: userId={}, deviceId={}, error={}", 
                                    client.getUserId(), client.getSessionId(), e.getMessage());
                        }
                    }
                }
                
                log.info("推流结束通知已发送给{}个观众（全局广播）: streamId={}, streamerUserId={}", 
                        notifiedCount, streamId, streamerUserId);
            }
            
        } catch (Exception e) {
            log.error("通知观众推流结束失败: streamId={}, streamerUserId={}, error={}", streamId, streamerUserId, e.getMessage(), e);
        }
    }
    
    /**
     * 从streamId中提取房间ID
     * 根据实际的streamId生成规则，需要从数据库中查找对应的用户ID，然后通过用户ID关联到房间
     */
    private String extractRoomIdFromStreamId(String streamId) {
        try {
            // 从数据库中查找streamId对应的用户ID
            Optional<StreamInfo> streamInfoOpt = streamInfoRepository.findByStreamId(streamId);
            if (streamInfoOpt.isPresent()) {
                String userId = streamInfoOpt.get().getUserId();
                // 这里需要根据业务逻辑确定用户所在的房间
                // 假设用户ID就是房间ID，或者通过其他方式获取房间ID
                return userId; // 暂时返回用户ID作为房间ID
            }
            
            // 如果数据库中找不到，尝试从streamId格式解析
            if (streamId.startsWith("stream_")) {
                // 格式：stream_userId_timestamp
                String[] parts = streamId.split("_", 3);
                if (parts.length >= 2) {
                    return parts[1]; // 返回userId作为房间ID
                }
            } else if (streamId.contains("_")) {
                // 格式：userId_streamName_timestamp
                String[] parts = streamId.split("_", 3);
                if (parts.length >= 1) {
                    return parts[0]; // 返回userId作为房间ID
                }
            } else if (streamId.contains("/")) {
                // 格式：live/uuid，这种情况下无法直接获取房间ID
                // 需要从数据库中查找对应的用户ID
                log.warn("无法从streamId格式直接获取房间ID: streamId={}", streamId);
                return null;
            }
            
            log.warn("无法解析streamId格式: streamId={}", streamId);
            return null;
            
        } catch (Exception e) {
            log.error("从streamId提取房间ID失败: streamId={}, error={}", streamId, e.getMessage(), e);
            return null;
        }
    }
}