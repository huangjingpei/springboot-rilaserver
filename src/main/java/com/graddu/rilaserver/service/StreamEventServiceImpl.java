package com.graddu.rilaserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graddu.rilaserver.entity.StreamInfo;
import com.graddu.rilaserver.entity.UserSession;
import com.graddu.rilaserver.model.StreamEventMessage;
import com.graddu.rilaserver.repository.StreamInfoRepository;
import com.graddu.rilaserver.repository.UserSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
            
            // 首先尝试向特定房间的观众发送通知
            if (roomId != null) {
                log.info("尝试向房间{}发送推流开始通知: streamId={}, streamerUserId={}", roomId, streamId, streamerUserId);
                
                // 获取房间内所有观众（除了主播）
                List<RoomService.Client> roomClients = roomService.getRoomClients(roomId);
                log.info("房间{}中有{}个客户端连接", roomId, roomClients.size());
                
                for (RoomService.Client client : roomClients) {
                    log.info("检查客户端: userId={}, role={}, deviceId={}", client.getUserId(), client.getRole(), client.getSessionId());
                    
                    // 跳过主播自己，但允许proxy角色接收通知（proxy通常也是观众）
                    if (client.getUserId().equals(streamerUserId) && "anchor".equals(client.getRole())) {
                        log.info("跳过主播自己: userId={}, role={}", streamerUserId, client.getRole());
                        continue;
                    }
                    
                    try {
                        WebSocketSession session = client.getSession();
                        if (session != null && session.isOpen()) {
                            session.sendMessage(new TextMessage(messageJson));
                            notifiedCount++;
                            log.info("推流开始通知已发送给观众: userId={}, role={}, deviceId={}", client.getUserId(), client.getRole(), client.getSessionId());
                        } else {
                            log.warn("客户端会话无效: userId={}, role={}, deviceId={}, sessionOpen={}", 
                                    client.getUserId(), client.getRole(), client.getSessionId(), session != null ? session.isOpen() : false);
                        }
                    } catch (IOException e) {
                        log.error("向观众发送推流开始通知失败: userId={}, role={}, deviceId={}, error={}", 
                                client.getUserId(), client.getRole(), client.getSessionId(), e.getMessage());
                    }
                }
                
                log.info("推流开始通知已发送给{}个观众: streamId={}, streamerUserId={}, roomId={}", 
                        notifiedCount, streamId, streamerUserId, roomId);
            }
            
            // 如果房间内没有观众，或者房间ID为空，则向所有在线用户发送通知（除了主播）
            if (notifiedCount == 0) {
                log.info("房间内无观众，向所有在线用户发送推流开始通知: streamId={}, streamerUserId={}", streamId, streamerUserId);
                
                // 获取所有房间的所有客户端
                Map<String, RoomService.Room> allRooms = roomService.getRooms();
                log.info("当前有{}个房间", allRooms.size());
                
                for (Map.Entry<String, RoomService.Room> entry : allRooms.entrySet()) {
                    String currentRoomId = entry.getKey();
                    RoomService.Room room = entry.getValue();
                    
                    log.info("房间{}中有{}个客户端", currentRoomId, room.getClients().size());
                    
                    for (RoomService.Client client : room.getClients()) {
                        log.info("检查客户端: userId={}, role={}, deviceId={}, roomId={}", client.getUserId(), client.getRole(), client.getSessionId(), currentRoomId);
                        
                        // 跳过主播自己，但允许proxy角色接收通知（proxy通常也是观众）
                        if (client.getUserId().equals(streamerUserId) && "anchor".equals(client.getRole())) {
                            log.info("跳过主播自己: userId={}, role={}, roomId={}", streamerUserId, client.getRole(), currentRoomId);
                            continue;
                        }
                        
                        try {
                            WebSocketSession session = client.getSession();
                            if (session != null && session.isOpen()) {
                                session.sendMessage(new TextMessage(messageJson));
                                notifiedCount++;
                                log.info("推流开始通知已发送给观众: userId={}, role={}, deviceId={}, roomId={}", 
                                        client.getUserId(), client.getRole(), client.getSessionId(), currentRoomId);
                            } else {
                                log.warn("客户端会话无效: userId={}, role={}, deviceId={}, roomId={}, sessionOpen={}", 
                                        client.getUserId(), client.getRole(), client.getSessionId(), currentRoomId, 
                                        session != null ? session.isOpen() : false);
                            }
                        } catch (IOException e) {
                            log.error("向观众发送推流开始通知失败: userId={}, role={}, deviceId={}, roomId={}, error={}", 
                                    client.getUserId(), client.getRole(), client.getSessionId(), currentRoomId, e.getMessage());
                        }
                    }
                }
                
                log.info("推流开始通知已发送给{}个观众（全局广播）: streamId={}, streamerUserId={}", 
                        notifiedCount, streamId, streamerUserId);
            }
            
            // 如果仍然没有通知到任何人，记录警告
            if (notifiedCount == 0) {
                log.warn("没有找到任何在线的观众来接收推流开始通知: streamId={}, streamerUserId={}", streamId, streamerUserId);
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
            
            // 首先尝试向特定房间的观众发送通知
            if (roomId != null) {
                log.info("尝试向房间{}发送推流结束通知: streamId={}, streamerUserId={}", roomId, streamId, streamerUserId);
                
                // 获取房间内所有观众（除了主播）
                List<RoomService.Client> roomClients = roomService.getRoomClients(roomId);
                log.info("房间{}中有{}个客户端连接", roomId, roomClients.size());
                
                for (RoomService.Client client : roomClients) {
                    log.info("检查客户端: userId={}, role={}, deviceId={}", client.getUserId(), client.getRole(), client.getSessionId());
                    
                    // 跳过主播自己，但允许proxy角色接收通知（proxy通常也是观众）
                    if (client.getUserId().equals(streamerUserId) && "anchor".equals(client.getRole())) {
                        log.info("跳过主播自己: userId={}, role={}", streamerUserId, client.getRole());
                        continue;
                    }
                    
                    try {
                        WebSocketSession session = client.getSession();
                        if (session != null && session.isOpen()) {
                            session.sendMessage(new TextMessage(messageJson));
                            notifiedCount++;
                            log.info("推流结束通知已发送给观众: userId={}, role={}, deviceId={}", client.getUserId(), client.getRole(), client.getSessionId());
                        } else {
                            log.warn("客户端会话无效: userId={}, role={}, deviceId={}, sessionOpen={}", 
                                    client.getUserId(), client.getRole(), client.getSessionId(), session != null ? session.isOpen() : false);
                        }
                    } catch (IOException e) {
                        log.error("向观众发送推流结束通知失败: userId={}, role={}, deviceId={}, error={}", 
                                client.getUserId(), client.getRole(), client.getSessionId(), e.getMessage());
                    }
                }
                
                log.info("推流结束通知已发送给{}个观众: streamId={}, streamerUserId={}, roomId={}", 
                        notifiedCount, streamId, streamerUserId, roomId);
            }
            
            // 如果房间内没有观众，或者房间ID为空，则向所有在线用户发送通知（除了主播）
            if (notifiedCount == 0) {
                log.info("房间内无观众，向所有在线用户发送推流结束通知: streamId={}, streamerUserId={}", streamId, streamerUserId);
                
                // 获取所有房间的所有客户端
                Map<String, RoomService.Room> allRooms = roomService.getRooms();
                log.info("当前有{}个房间", allRooms.size());
                
                for (Map.Entry<String, RoomService.Room> entry : allRooms.entrySet()) {
                    String currentRoomId = entry.getKey();
                    RoomService.Room room = entry.getValue();
                    
                    log.info("房间{}中有{}个客户端", currentRoomId, room.getClients().size());
                    
                    for (RoomService.Client client : room.getClients()) {
                        log.info("检查客户端: userId={}, role={}, deviceId={}, roomId={}", client.getUserId(), client.getRole(), client.getSessionId(), currentRoomId);
                        
                        // 跳过主播自己，但允许proxy角色接收通知（proxy通常也是观众）
                        if (client.getUserId().equals(streamerUserId) && "anchor".equals(client.getRole())) {
                            log.info("跳过主播自己: userId={}, role={}, roomId={}", streamerUserId, client.getRole(), currentRoomId);
                            continue;
                        }
                        
                        try {
                            WebSocketSession session = client.getSession();
                            if (session != null && session.isOpen()) {
                                session.sendMessage(new TextMessage(messageJson));
                                notifiedCount++;
                                log.info("推流结束通知已发送给观众: userId={}, role={}, deviceId={}, roomId={}", 
                                        client.getUserId(), client.getRole(), client.getSessionId(), currentRoomId);
                            } else {
                                log.warn("客户端会话无效: userId={}, role={}, deviceId={}, roomId={}, sessionOpen={}", 
                                        client.getUserId(), client.getRole(), client.getSessionId(), currentRoomId, 
                                        session != null ? session.isOpen() : false);
                            }
                        } catch (IOException e) {
                            log.error("向观众发送推流结束通知失败: userId={}, role={}, deviceId={}, roomId={}, error={}", 
                                    client.getUserId(), client.getRole(), client.getSessionId(), currentRoomId, e.getMessage());
                        }
                    }
                }
                
                log.info("推流结束通知已发送给{}个观众（全局广播）: streamId={}, streamerUserId={}", 
                        notifiedCount, streamId, streamerUserId);
            }
            
            // 如果仍然没有通知到任何人，记录警告
            if (notifiedCount == 0) {
                log.warn("没有找到任何在线的观众来接收推流结束通知: streamId={}, streamerUserId={}", streamId, streamerUserId);
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
            log.info("开始解析streamId获取房间ID: streamId={}", streamId);
            
            // 从数据库中查找streamId对应的用户ID
            Optional<StreamInfo> streamInfoOpt = streamInfoRepository.findByStreamId(streamId);
            if (streamInfoOpt.isPresent()) {
                String userId = streamInfoOpt.get().getUserId();
                log.info("从数据库中找到streamId对应的用户ID: streamId={}, userId={}", streamId, userId);
                // 这里需要根据业务逻辑确定用户所在的房间
                // 假设用户ID就是房间ID，或者通过其他方式获取房间ID
                return userId; // 暂时返回用户ID作为房间ID
            }
            
            log.info("数据库中未找到streamId，尝试从格式解析: streamId={}", streamId);
            
            // 如果数据库中找不到，尝试从streamId格式解析
            if (streamId.startsWith("stream_")) {
                // 格式：stream_userId_timestamp
                String[] parts = streamId.split("_", 3);
                if (parts.length >= 2) {
                    String userId = parts[1];
                    log.info("从streamId格式解析出用户ID: streamId={}, userId={}", streamId, userId);
                    return userId; // 返回userId作为房间ID
                }
            } else if (streamId.contains("_")) {
                // 格式：userId_streamName_timestamp
                String[] parts = streamId.split("_", 3);
                if (parts.length >= 1) {
                    String userId = parts[0];
                    log.info("从streamId格式解析出用户ID: streamId={}, userId={}", streamId, userId);
                    return userId; // 返回userId作为房间ID
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