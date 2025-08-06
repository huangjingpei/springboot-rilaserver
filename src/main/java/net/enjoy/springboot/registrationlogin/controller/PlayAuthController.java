package net.enjoy.springboot.registrationlogin.controller;

import net.enjoy.springboot.registrationlogin.dto.PlayAuthResult;
import net.enjoy.springboot.registrationlogin.service.PlayAuthService;
import net.enjoy.springboot.registrationlogin.utils.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 播放鉴权控制器
 * 提供播放地址获取和播放令牌生成等接口
 */
@Tag(name = "播放鉴权", description = "播放鉴权相关接口")
@RestController
@RequestMapping("/api/play")
public class PlayAuthController {
    
    private static final Logger log = LoggerFactory.getLogger(PlayAuthController.class);
    
    @Autowired
    private PlayAuthService playAuthService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取播放地址（需要认证）
     */
    @Operation(summary = "获取播放地址", description = "获取指定流的播放地址，需要用户认证")
    @GetMapping("/getPlayUrl/{streamId}")
    public ResponseEntity<?> getPlayUrl(@PathVariable String streamId, HttpServletRequest request) {
        try {
            // 从JWT token中获取用户信息
            String userId = getUserIdFromToken(request);
            if (userId == null) {
                return ResponseEntity.status(401).body("未提供有效的认证信息");
            }
            
            String clientIp = getClientIpAddress(request);
            
            // 进行播放鉴权
            PlayAuthResult authResult = playAuthService.verifyPlayAuth(streamId, userId, null, clientIp, "http");
            
            if (authResult.isAllowed()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("streamId", streamId);
                response.put("userId", userId);
                
                if (authResult.getPlayUrlInfo() != null) {
                    response.put("rtmpUrl", authResult.getPlayUrlInfo().getRtmpUrl());
                    response.put("hlsUrl", authResult.getPlayUrlInfo().getHlsUrl());
                    response.put("flvUrl", authResult.getPlayUrlInfo().getFlvUrl());
                    response.put("webrtcUrl", authResult.getPlayUrlInfo().getWebrtcUrl());
                }
                
                if (authResult.getPlayToken() != null) {
                    response.put("playToken", authResult.getPlayToken());
                    response.put("expireTime", authResult.getExpireTime());
                }
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "code", authResult.getCode(),
                    "message", authResult.getMessage()
                ));
            }
            
        } catch (Exception e) {
            log.error("获取播放地址失败: streamId={}, error={}", streamId, e.getMessage(), e);
            return ResponseEntity.status(500).body("服务器内部错误");
        }
    }
    
    /**
     * 生成播放令牌（需要认证）
     */
    @Operation(summary = "生成播放令牌", description = "为指定流生成播放令牌")
    @PostMapping("/generatePlayToken")
    public ResponseEntity<?> generatePlayToken(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String streamId = (String) request.get("streamId");
            Integer expireHours = (Integer) request.get("expireHours");
            
            if (streamId == null || streamId.isEmpty()) {
                return ResponseEntity.badRequest().body("streamId不能为空");
            }
            
            // 从JWT token中获取用户信息
            String userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return ResponseEntity.status(401).body("未提供有效的认证信息");
            }
            
            // 检查用户是否有生成播放令牌的权限
            if (!playAuthService.hasPlayPermission(userId, streamId)) {
                return ResponseEntity.status(403).body("无权限为该流生成播放令牌");
            }
            
            // 默认24小时过期
            long expireSeconds = (expireHours != null && expireHours > 0) ? 
                expireHours * 3600L : 24 * 3600L;
            
            String playToken = playAuthService.generatePlayToken(streamId, userId, expireSeconds);
            
            if (playToken != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("playToken", playToken);
                response.put("streamId", streamId);
                response.put("userId", userId);
                response.put("expireTime", System.currentTimeMillis() + expireSeconds * 1000);
                response.put("expireHours", expireSeconds / 3600);
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(500).body("生成播放令牌失败");
            }
            
        } catch (Exception e) {
            log.error("生成播放令牌失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(500).body("服务器内部错误");
        }
    }
    
    /**
     * 验证播放令牌
     */
    @Operation(summary = "验证播放令牌", description = "验证播放令牌是否有效")
    @PostMapping("/validatePlayToken")
    public ResponseEntity<?> validatePlayToken(@RequestBody Map<String, String> request) {
        try {
            String playToken = request.get("playToken");
            String streamId = request.get("streamId");
            
            if (playToken == null || streamId == null) {
                return ResponseEntity.badRequest().body("playToken和streamId不能为空");
            }
            
            boolean isValid = playAuthService.validatePlayToken(playToken, streamId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("streamId", streamId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("验证播放令牌失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(500).body("服务器内部错误");
        }
    }
    
    /**
     * 检查流状态
     */
    @Operation(summary = "检查流状态", description = "检查流是否存在且可播放")
    @GetMapping("/checkStream/{streamId}")
    public ResponseEntity<?> checkStream(@PathVariable String streamId) {
        try {
            boolean isPlayable = playAuthService.isStreamPlayable(streamId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("streamId", streamId);
            response.put("playable", isPlayable);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("检查流状态失败: streamId={}, error={}", streamId, e.getMessage(), e);
            return ResponseEntity.status(500).body("服务器内部错误");
        }
    }
    
    /**
     * 从JWT token中获取用户ID
     */
    private String getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        try {
            String payload = jwtUtil.getUsernameFromToken(token);
            if (payload != null && payload.contains("|")) {
                return payload.substring(0, payload.lastIndexOf("|"));
            }
            return payload;
        } catch (Exception e) {
            log.error("解析JWT token失败", e);
            return null;
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
} 