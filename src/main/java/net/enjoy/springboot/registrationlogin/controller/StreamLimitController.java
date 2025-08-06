package net.enjoy.springboot.registrationlogin.controller;

import net.enjoy.springboot.registrationlogin.service.StreamLimitService;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stream-limits")
public class StreamLimitController {

    @Autowired
    private StreamLimitService streamLimitService;

    @Autowired
    private UserService userService;

    /**
     * 检查用户推流限制
     */
    @GetMapping("/check/{userId}")
    public ResponseEntity<Map<String, Object>> checkStreamLimit(@PathVariable String userId) {
        try {
            var result = streamLimitService.checkStreamLimit(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("allowed", result.isAllowed());
            response.put("message", result.getMessage());
            response.put("code", result.getCode());
            response.put("currentStreams", result.getCurrentStreams());
            response.put("maxStreams", result.getMaxStreams());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检查推流限制失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取用户推流统计
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getStreamStats(@PathVariable String userId) {
        try {
            User user = userService.findUserByEmail(userId);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            int currentStreams = streamLimitService.getCurrentStreamCount(userId);
            int maxStreams = streamLimitService.getMaxStreamCount(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("userId", userId);
            stats.put("userName", user.getName());
            stats.put("userType", user.getType());
            stats.put("currentStreams", currentStreams);
            stats.put("maxStreams", maxStreams);
            stats.put("maxDevices", user.getMaxDevices());
            stats.put("usagePercentage", Math.round((double) currentStreams / maxStreams * 100));
            stats.put("canStartNewStream", streamLimitService.canUserStartStream(userId));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取推流统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 更新用户推流限制（管理员功能）
     */
    @PutMapping("/update/{userId}")
    public ResponseEntity<Map<String, Object>> updateStreamLimit(
            @PathVariable String userId, 
            @RequestParam int maxStreams) {
        try {
            User user = userService.findUserByEmail(userId);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 验证推流数限制范围
            if (maxStreams < 1 || maxStreams > 100) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "推流数限制必须在1-100之间");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            user.setMaxStreams(maxStreams);
            userService.updateUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "推流限制更新成功");
            response.put("userId", userId);
            response.put("newMaxStreams", maxStreams);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "更新推流限制失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 