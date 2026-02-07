package net.enjoy.springboot.registrationlogin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.entity.UserSession;
import net.enjoy.springboot.registrationlogin.model.WebSocketMessage;
import net.enjoy.springboot.registrationlogin.repository.UserRepository;
import net.enjoy.springboot.registrationlogin.repository.UserSessionRepository;
import net.enjoy.springboot.registrationlogin.service.RoomService;
import net.enjoy.springboot.registrationlogin.service.StreamStatusSyncService;
import net.enjoy.springboot.registrationlogin.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class WebSocketController extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);
    private final RoomService roomService;
    private final ObjectMapper objectMapper;
    
    // 添加客户端缓存，避免重复查找
    private final Map<String, RoomService.Client> sessionClientCache = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRoomCache = new ConcurrentHashMap<>();
    
    // 异步消息处理线程池
    private final ExecutorService messageExecutor = Executors.newFixedThreadPool(10, 
        r -> new Thread(r, "WebSocket-Message-" + System.currentTimeMillis()));
    
    // 弹幕速率限制：每个用户每秒最多5条弹幕
    private final Map<String, Long> userLastMessageTime = new ConcurrentHashMap<>();
    private final Map<String, Integer> userMessageCount = new ConcurrentHashMap<>();
    private static final int MAX_MESSAGES_PER_SECOND = 5;
    
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSessionRepository userSessionRepository;
    @Autowired
    private StreamStatusSyncService streamStatusSyncService;

    public WebSocketController(RoomService roomService, ObjectMapper objectMapper) {
        this.roomService = roomService;
        this.objectMapper = objectMapper;
    }

    @MessageMapping("/chat")
    public void handleChat(@Payload WebSocketMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String role = Objects.requireNonNull(headerAccessor.getUser()).getName();
        String roomId = headerAccessor.getFirstNativeHeader("roomId");
        
        if (roomId != null) {
            // messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
        }
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String role = headers.getUser().getName();
        String roomId = headers.getFirstNativeHeader("roomId");
        String sessionId = headers.getSessionId();

        if (roomId != null) {
            RoomService.Room room = roomService.getOrCreateRoom(roomId);
            RoomService.Client client = new RoomService.Client();
            client.setSessionId(sessionId);
            client.setRole(role);
            client.setAssignedId(room.getNextUserId());
            room.getClients().add(client);

            // 发送欢迎消息
            WebSocketMessage welcomeMessage = new WebSocketMessage();
            welcomeMessage.setType("userEvent");
            welcomeMessage.setMessage("Welcome (" + role + ") to room " + roomId);
            welcomeMessage.setAssignedId(client.getAssignedId());
            welcomeMessage.setRole(role);
            welcomeMessage.setAction("join");
            
            // messagingTemplate.convertAndSend("/topic/room/" + roomId, welcomeMessage);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String role = headers.getUser().getName();
        String roomId = headers.getFirstNativeHeader("roomId");
        String sessionId = headers.getSessionId();

        if (roomId != null) {
            RoomService.Room room = roomService.getOrCreateRoom(roomId);
            room.getClients().removeIf(client -> 
                client.getSessionId().equals(sessionId));

            // 发送离开消息
            WebSocketMessage leaveMessage = new WebSocketMessage();
            leaveMessage.setType("userEvent");
            leaveMessage.setAction("leave");
            leaveMessage.setRole(role);
            
            // messagingTemplate.convertAndSend("/topic/room/" + roomId, leaveMessage);

            roomService.removeRoomIfEmpty(roomId);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = null;
        String roomId = null;
        
        try {
            // 解析token，获取userId
            String query = session.getUri().getQuery();
            Map<String, String> params = parseQuery(query);
            String token = params.get("token");
            String deviceId = params.get("deviceId");
            roomId = params.get("roomId");
            if (deviceId == null) deviceId = session.getId();

            String payload = jwtUtil.getUsernameFromToken(token); // userId|role
            String[] arr = payload.split("\\|");
            userId = arr[0];
            String role = arr.length > 1 ? arr[1] : null;

            // 创建final变量供lambda使用
            final String finalUserId = userId;

            User user = userRepository.findByUserId(userId);
            if (user == null) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("用户不存在"));
                return;
            }
            int onlineCount = userSessionRepository.countByUserIdAndStatus(userId, "online");
            if (onlineCount >= user.getMaxDevices()*2) {
                //session.close(CloseStatus.NOT_ACCEPTABLE.withReason("已达最大在线设备数"));
                //return;
            }

            // 允许连接，记录/更新 user_session
            UserSession userSession = userSessionRepository.findByUserIdAndDeviceId(userId, deviceId);
            if (userSession == null) userSession = new UserSession();
            userSession.setUserId(userId);
            userSession.setDeviceId(deviceId);
            userSession.setLoginTime(java.time.LocalDateTime.now());
            userSession.setStatus("online");
            userSessionRepository.save(userSession);

            log.info("[WebSocket] 用户连接: userId={}, role={}, roomId={}, sessionId={}", userId, role, roomId, session.getId());

            RoomService.Room room = roomService.getOrCreateRoom(roomId);

            // userId唯一校验
            if (room.getClients().stream().anyMatch(c -> c.getUserId().equals(finalUserId))) {
                log.warn("[WebSocket] userId重复: userId={}, roomId={}", userId, roomId);
//                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"User already connected\"}"));
//                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("User already connected"));
//                return;
            }

            // 分配assignedId
            int assignedId = room.getNextUserId();
            room.setNextUserId(assignedId + 1);

            // 注册client
            RoomService.Client client = new RoomService.Client();
            client.setSessionId(session.getId());
            client.setUserId(userId);
            client.setRole(role);
            client.setAssignedId(assignedId);
            client.setSession(session);
            room.getClients().add(client);
            
            // 添加到缓存
            sessionClientCache.put(session.getId(), client);
            sessionRoomCache.put(session.getId(), roomId);

            log.info("[WebSocket] 用户注册: userId={}, assignedId={}, role={}, roomId={}, 当前房间人数={}", userId, assignedId, role, roomId, room.getClients().size());

            // 发送欢迎消息
            session.sendMessage(new TextMessage("{\"type\":\"system\",\"message\":\"Welcome " + userId + " (" + role + ") to room " + roomId + "\",\"assignedId\":" + assignedId + ",\"role\":\"" + role + "\",\"action\":\"join\"}"));

            // 通知场控
            if ("anchor".equals(role) || "proxy".equals(role)) {
                log.info("[WebSocket] {} 加入房间，通知controller: userId={}, assignedId={}, roomId={}", role, userId, assignedId, roomId);
                broadcastToControllers(room, String.format("{\"type\":\"userEvent\",\"action\":\"join\",\"userId\":\"%s\",\"assignedId\":%d,\"role\":\"%s\"}", userId, assignedId, role));
            }

            // 场控推送用户列表
            if ("controller".equals(role)) {
                String userList = room.getClients().stream()
                        .map(c -> String.format("{\"userId\":\"%s\",\"assignedId\":%d,\"role\":\"%s\"}", c.getUserId(), c.getAssignedId(), c.getRole()))
                        .collect(Collectors.joining(","));
                session.sendMessage(new TextMessage("{\"type\":\"userList\",\"users\":[" + userList + "]}"));
                log.info("[WebSocket] controller进入，推送用户列表: roomId={}, userList={}", roomId, userList);
            }
            
            // ✨ 新增：为新加入的客户端同步当前推流状态
            try {
                streamStatusSyncService.syncCurrentStreamStatus(session, userId, roomId);
                log.info("[WebSocket] 已为新客户端同步推流状态: userId={}, roomId={}", userId, roomId);
            } catch (Exception e) {
                log.error("[WebSocket] 同步推流状态失败: userId={}, roomId={}, error={}", userId, roomId, e.getMessage(), e);
            }
            
        } catch (Exception e) {
            log.error("[WebSocket] 连接建立失败: userId={}, roomId={}, error={}", userId, roomId, e.getMessage(), e);
            // 确保连接关闭
            if (session.isOpen()) {
                session.close(CloseStatus.SERVER_ERROR.withReason("连接建立失败"));
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();

        // 使用缓存快速获取客户端信息
        RoomService.Client client = sessionClientCache.get(sessionId);
        String roomId = sessionRoomCache.get(sessionId);

        if (client == null || roomId == null) {
            log.warn("[WebSocket] 客户端缓存未找到: sessionId={}", sessionId);
            return;
        }

        String role = client.getRole();
        String userId = client.getUserId();
        int assignedId = client.getAssignedId();

        // 只有在debug模式下才记录详细日志
        if (log.isDebugEnabled()) {
            log.debug("[WebSocket] 收到消息: userId={}, role={}, roomId={}", userId, role, roomId);
        }

        try {
            Map<String, Object> msg = objectMapper.readValue(message.getPayload(), Map.class);

            // 只处理弹幕消息，并异步处理
            if ((role.equals("proxy") || role.equals("anchor")) && "bullet".equals(msg.get("type"))) {
                Object contentObj = msg.get("content");
                String content;
                String subType = msg.get("subType").toString();
                // // 处理不同类型的content字段
                // if (contentObj == null) {
                //     content = "";
                // } else {
                //     Class<?> clazz = contentObj.getClass();
                //     if (clazz == String.class) {
                //         content = (String) contentObj;
                //     } else if (Map.class.isAssignableFrom(clazz)) {
                //         // 如果是JSON对象，转换为JSON字符串
                //         content = objectMapper.writeValueAsString(contentObj);
                //     } else if (clazz.isArray()) {
                //         // 如果是JSON数组，转换为JSON字符串
                //         content = objectMapper.writeValueAsString(contentObj);
                //     } else {
                //         // 其他类型，转换为字符串
                //         content = contentObj.toString();
                //     }
                // }

                // 检查速率限制
                if (!checkRateLimit(userId)) {
                    log.warn("[WebSocket] 弹幕发送过于频繁: userId={}", userId);
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"发送过于频繁，请稍后再试\"}"));
                    return;
                }

                // 异步处理弹幕消息，避免阻塞主线程
                messageExecutor.submit(() -> {
                    try {
                        processBulletMessage(userId, subType, assignedId, contentObj, roomId);
                    } catch (Exception e) {
                        log.error("[WebSocket] 异步处理弹幕异常: userId={}, error={}", userId, e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            log.error("[WebSocket] 消息处理异常: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    // 异步处理弹幕消息
    private void processBulletMessage(String userId, String subType, int assignedId, Object content, String roomId) {
        try {
            // 使用ObjectMapper构建JSON消息，确保content字段的JSON结构正确
            Map<String, Object> bulletMsgMap = new HashMap<>();
            bulletMsgMap.put("type", "bullet");
            bulletMsgMap.put("userId", userId);
            bulletMsgMap.put("subType", subType);
            bulletMsgMap.put("assignedId", assignedId);
            bulletMsgMap.put("content", content);

            // // 尝试将content解析为JSON对象或数组，如果失败则作为字符串处理
            // try {
            //     Object contentObj = objectMapper.readValue(content, Object.class);
            //     bulletMsgMap.put("content", contentObj);
            // } catch (Exception e) {
            //     // 如果解析失败，直接使用原始content字符串
            //     bulletMsgMap.put("content", content);
            // }
            
            String bulletMsg = objectMapper.writeValueAsString(bulletMsgMap);
            TextMessage bulletTextMsg = new TextMessage(bulletMsg);
            
            // 获取房间并批量发送
            RoomService.Room room = roomService.getOrCreateRoom(roomId);
            int sentCount = 0;
            
            for (RoomService.Client c : room.getClients()) {
                if ((c.getRole().equals("anchor") || c.getRole().equals("controller")) && c.getSession().isOpen()) {
                    try {
                        c.getSession().sendMessage(bulletTextMsg);
                        sentCount++;
                    } catch (Exception e) {
                        log.warn("[WebSocket] 发送消息失败: toUserId={}, error={}", c.getUserId(), e.getMessage());
                    }
                }
            }
            
            // 只记录统计信息
            if (sentCount > 0) {
                log.info("[WebSocket] 弹幕转发: from={}, room={}, sent={}", userId, roomId, sentCount);
            }
        } catch (Exception e) {
            log.error("[WebSocket] 构建弹幕消息失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            // 优先从缓存获取信息
            String roomId = sessionRoomCache.get(session.getId());
            RoomService.Client client = sessionClientCache.get(session.getId());
            
            // 如果缓存中没有，说明连接建立失败，直接清理缓存并返回
            if (roomId == null && client == null) {
                sessionClientCache.remove(session.getId());
                sessionRoomCache.remove(session.getId());
                log.warn("[WebSocket] 连接关闭但未找到缓存信息，可能是连接建立失败: sessionId={}", session.getId());
                return;
            }
            
            // 解析token，获取userId、deviceId（可能为null）
            String userId = null;
            String deviceId = null;
            
            try {
                String query = session.getUri().getQuery();
                if (query != null) {
                    Map<String, String> params = parseQuery(query);
                    String token = params.get("token");
                    deviceId = params.get("deviceId");
                    if (deviceId == null) deviceId = session.getId();
                    
                    if (token != null) {
                        String payload = jwtUtil.getUsernameFromToken(token); // userId|role
                        if (payload != null) {
                            String[] arr = payload.split("\\|");
                            if (arr.length > 0) {
                                userId = arr[0];
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("[WebSocket] 解析连接信息失败: {}", e.getMessage());
            }
            
            // 更新用户会话状态
            if (userId != null && deviceId != null) {
                try {
                    UserSession userSession = userSessionRepository.findByUserIdAndDeviceId(userId, deviceId);
                    if (userSession != null) {
                        userSession.setStatus("offline");
                        userSessionRepository.save(userSession);
                    }
                } catch (Exception e) {
                    log.warn("[WebSocket] 更新用户会话状态失败: {}", e.getMessage());
                }
            }

            // 处理房间和客户端清理
            if (roomId != null) {
                try {
                    RoomService.Room room = roomService.getOrCreateRoom(roomId);
                    if (client != null) {
                        int assignedId = client.getAssignedId();
                        String role = client.getRole();
                        String clientUserId = client.getUserId();
                        
                        room.getClients().remove(client);
                        log.info("[WebSocket] 用户断开: userId={}, assignedId={}, role={}, roomId={}, 当前房间人数={}", 
                                clientUserId, assignedId, role, roomId, room.getClients().size());
                        
                        // 通知场控
                        if ("anchor".equals(role) || "proxy".equals(role)) {
                            log.info("[WebSocket] {} 离开房间，通知controller: userId={}, assignedId={}, roomId={}", 
                                    role, clientUserId, assignedId, roomId);
                            broadcastToControllers(room, String.format(
                                    "{\"type\":\"userEvent\",\"action\":\"leave\",\"userId\":\"%s\",\"assignedId\":%d,\"role\":\"%s\"}", 
                                    clientUserId, assignedId, role));
                        }
                        
                        roomService.removeRoomIfEmpty(roomId);
                    }
                } catch (Exception e) {
                    log.error("[WebSocket] 处理房间清理失败: roomId={}, error={}", roomId, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("[WebSocket] afterConnectionClosed处理失败: {}", e.getMessage(), e);
        } finally {
            // 确保清理缓存
            sessionClientCache.remove(session.getId());
            sessionRoomCache.remove(session.getId());
        }
    }

    // 工具方法
    private void broadcastToControllers(RoomService.Room room, String msg) {
        for (RoomService.Client c : room.getClients()) {
            if ("controller".equals(c.getRole()) && c.getSession().isOpen()) {
                try {
                    c.getSession().sendMessage(new TextMessage(msg));
                } catch (Exception ignored) {}
            }
        }
    }
    private RoomService.Client findClientBySession(WebSocketSession session) {
        // 遍历所有房间查找
        for (RoomService.Room room : roomService.getRooms().values()) {
            for (RoomService.Client c : room.getClients()) {
                if (c.getSession().getId().equals(session.getId())) return c;
            }
        }
        return null;
    }
    private String findRoomIdBySession(WebSocketSession session) {
        for (Map.Entry<String, RoomService.Room> entry : roomService.getRooms().entrySet()) {
            for (RoomService.Client c : entry.getValue().getClients()) {
                if (c.getSession().getId().equals(session.getId())) return entry.getKey();
            }
        }
        return null;
    }
    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }

    // 速率限制检查
    private boolean checkRateLimit(String userId) {
        Long lastMessageTime = userLastMessageTime.get(userId);
        if (lastMessageTime == null) {
            userLastMessageTime.put(userId, System.currentTimeMillis());
            userMessageCount.put(userId, 1);
            return true;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTime < 1000) { // 1秒内
            int currentCount = userMessageCount.getOrDefault(userId, 0) + 1;
            if (currentCount > MAX_MESSAGES_PER_SECOND) {
                return false;
            }
            userMessageCount.put(userId, currentCount);
        } else {
            userLastMessageTime.put(userId, currentTime);
            userMessageCount.put(userId, 1);
        }
        return true;
    }
} 