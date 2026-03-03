package com.graddu.rilaserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.graddu.rilaserver.entity.StreamInfo;
import com.graddu.rilaserver.service.StreamCleanupScheduler;
import com.graddu.rilaserver.service.StreamLimitService;
import com.graddu.rilaserver.service.StreamStatusValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流清理控制器
 * 提供僵尸流清理相关的API接口
 */
@RestController
@RequestMapping("/api/stream-cleanup")
@Tag(name = "流清理管理", description = "僵尸流清理相关接口")
public class StreamCleanupController {
    
    private static final Logger log = LoggerFactory.getLogger(StreamCleanupController.class);
    
    @Autowired
    private StreamCleanupScheduler streamCleanupScheduler;
    
    @Autowired
    private StreamStatusValidationService streamStatusValidationService;
    
    @Autowired
    private StreamLimitService streamLimitService;
    
    /**
     * 手动触发全局僵尸流清理
     */
    @Operation(summary = "手动清理所有僵尸流", description = "清理所有用户的僵尸流（CREATED状态但超时的流）")
    @PostMapping("/cleanup-all")
    public ResponseEntity<Map<String, Object>> cleanupAllZombieStreams(HttpServletRequest request) {
        try {
            // 这里可以添加权限验证，只有管理员才能执行全局清理
            // String userId = getUserIdFromToken(request);
            // if (!isAdmin(userId)) {
            //     return ResponseEntity.status(403).body(Map.of("error", "权限不足"));
            // }
            
            int cleanedCount = streamCleanupScheduler.manualCleanupZombieStreams();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "僵尸流清理完成");
            response.put("cleanedCount", cleanedCount);
            
            log.info("管理员手动清理僵尸流完成: 清理数量={}", cleanedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("手动清理僵尸流失败: error={}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "清理失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 清理指定用户的僵尸流
     */
    @Operation(summary = "清理用户僵尸流", description = "清理指定用户的僵尸流")
    @PostMapping("/cleanup-user/{userId}")
    public ResponseEntity<Map<String, Object>> cleanupUserZombieStreams(
            @PathVariable String userId,
            HttpServletRequest request) {
        try {
            // 验证当前用户是否有权限清理指定用户的流
            String currentUserId = getUserIdFromToken(request);
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "未提供有效的认证信息"));
            }
            
            // 只有管理员或用户本人才能清理
            if (!currentUserId.equals(userId) && !isAdmin(currentUserId)) {
                return ResponseEntity.status(403).body(Map.of("error", "权限不足"));
            }
            
            int cleanedCount = streamStatusValidationService.validateAndCleanZombieStreams(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户僵尸流清理完成");
            response.put("userId", userId);
            response.put("cleanedCount", cleanedCount);
            
            log.info("用户僵尸流清理完成: userId={}, 清理数量={}", userId, cleanedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("清理用户僵尸流失败: userId={}, error={}", userId, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "清理失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取指定用户的僵尸流列表
     */
    @Operation(summary = "获取用户僵尸流列表", description = "获取指定用户的僵尸流列表")
    @GetMapping("/zombie-streams/{userId}")
    public ResponseEntity<Map<String, Object>> getUserZombieStreams(
            @PathVariable String userId,
            HttpServletRequest request) {
        try {
            // 验证权限
            String currentUserId = getUserIdFromToken(request);
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "未提供有效的认证信息"));
            }
            
            if (!currentUserId.equals(userId) && !isAdmin(currentUserId)) {
                return ResponseEntity.status(403).body(Map.of("error", "权限不足"));
            }
            
            List<StreamInfo> zombieStreams = streamStatusValidationService.getZombieStreams(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("zombieStreams", zombieStreams);
            response.put("count", zombieStreams.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取用户僵尸流列表失败: userId={}, error={}", userId, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "获取失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取用户的推流统计信息
     */
    @Operation(summary = "获取用户推流统计", description = "获取用户的推流统计信息，包括当前推流数和最大推流数")
    @GetMapping("/stream-stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStreamStats(
            @PathVariable String userId,
            HttpServletRequest request) {
        try {
            // 验证权限
            String currentUserId = getUserIdFromToken(request);
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "未提供有效的认证信息"));
            }
            
            if (!currentUserId.equals(userId) && !isAdmin(currentUserId)) {
                return ResponseEntity.status(403).body(Map.of("error", "权限不足"));
            }
            
            // 先清理僵尸流
            int cleanedCount = streamStatusValidationService.validateAndCleanZombieStreams(userId);
            
            // 获取统计信息
            int currentStreams = streamLimitService.getCurrentStreamCount(userId);
            int maxStreams = streamLimitService.getMaxStreamCount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("currentStreams", currentStreams);
            response.put("maxStreams", maxStreams);
            response.put("cleanedZombieStreams", cleanedCount);
            response.put("canStartNewStream", currentStreams < maxStreams);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取用户推流统计失败: userId={}, error={}", userId, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "获取失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 从JWT token中获取用户ID
     */
    private String getUserIdFromToken(HttpServletRequest request) {
        // 这里需要实现从JWT token中提取用户ID的逻辑
        // 可以参考其他控制器中的实现
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // TODO: 实现JWT token解析逻辑
            // return jwtUtil.getUsernameFromToken(token);
        }
        return null;
    }
    
    /**
     * 检查用户是否为管理员
     */
    private boolean isAdmin(String userId) {
        // TODO: 实现管理员权限检查逻辑
        // 这里可以根据实际业务需求来实现
        return false;
    }
} 