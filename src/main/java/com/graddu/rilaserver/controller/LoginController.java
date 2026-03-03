package com.graddu.rilaserver.controller;

import lombok.Getter;
import lombok.Setter;
import com.graddu.rilaserver.entity.User;
import com.graddu.rilaserver.entity.UserSession;
import com.graddu.rilaserver.entity.UserDevice;
import com.graddu.rilaserver.entity.UserStatus;
import com.graddu.rilaserver.repository.UserRepository;
import com.graddu.rilaserver.repository.UserSessionRepository;
import com.graddu.rilaserver.service.SecurityService;
import com.graddu.rilaserver.service.SpringSessionService;
import com.graddu.rilaserver.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSessionRepository userSessionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private SpringSessionService springSessionService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            // 1. 检查用户状态（早期检查）
            if (!securityService.checkUserStatus(req.getUserId())) {
                User user = userRepository.findByUserId(req.getUserId());
                if (user == null) {
                    securityService.recordLoginAttempt(req.getUserId(), ipAddress, userAgent, false, "用户不存在");
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "用户不存在");
                    errorResponse.put("code", "USER_NOT_FOUND");
                    return ResponseEntity.badRequest().body(errorResponse);
                } else {
                    securityService.recordLoginAttempt(req.getUserId(), ipAddress, userAgent, false, "用户状态异常: " + user.getStatus());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "用户账户状态异常: " + user.getStatus().getDescription());
                    errorResponse.put("code", "USER_STATUS_ERROR");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
            
            // 2. 执行安全检查（锁定、IP阻止等）
            if (!securityService.performSecurityChecks(req.getUserId(), ipAddress)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                if (securityService.isAccountLocked(req.getUserId())) {
                    errorResponse.put("message", "账户已被锁定，请稍后再试");
                    errorResponse.put("code", "ACCOUNT_LOCKED");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                if (securityService.isIpBlocked(ipAddress)) {
                    errorResponse.put("message", "IP地址已被阻止，请稍后再试");
                    errorResponse.put("code", "IP_BLOCKED");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                securityService.recordLoginAttempt(req.getUserId(), ipAddress, userAgent, false, "安全检查失败");
                errorResponse.put("message", "安全检查失败");
                errorResponse.put("code", "SECURITY_CHECK_FAILED");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 3. 获取用户信息（此时用户已确认存在且状态正常）
            User user = userRepository.findByUserId(req.getUserId());
            
            // 4. 验证密码
            if (user.getPassword() != null && !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                securityService.recordLoginAttempt(req.getUserId(), ipAddress, userAgent, false, "密码错误");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "密码错误");
                errorResponse.put("code", "INVALID_PASSWORD");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 5. 验证用户类型（如果指定了type）
            if (req.getType() != null && !req.getType().equals(user.getType())) {
                securityService.recordLoginAttempt(req.getUserId(), ipAddress, userAgent, false, "用户类型不匹配");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户类型不匹配");
                errorResponse.put("code", "USER_TYPE_MISMATCH");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 6. 检查设备数限制（使用Spring Session）
            String deviceId = req.getDeviceId() != null ? req.getDeviceId() : UUID.randomUUID().toString();
            int currentSessions = springSessionService.getActiveSessionCount(user.getUserId());
            if (currentSessions >= user.getMaxDevices()) {
                securityService.recordLoginAttempt(req.getUserId(), ipAddress, userAgent, false, "设备数超限");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "已达最大在线设备数限制：" + user.getMaxDevices() + "台。请先在其他设备上登出");
                errorResponse.put("code", "DEVICE_LIMIT_EXCEEDED");
                errorResponse.put("maxDevices", user.getMaxDevices());
                errorResponse.put("currentDevices", currentSessions);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 7. 设备管理
            String deviceName = req.getDeviceName() != null ? req.getDeviceName() : "未知设备";
            String deviceType = req.getDeviceType() != null ? req.getDeviceType() : "web";
            
            // 注册设备
            UserDevice device = securityService.registerDevice(user.getUserId(), deviceId, deviceName, deviceType, ipAddress, userAgent);
            
            // 8. 处理会话（Spring Session会自动管理）
            // 不再需要手动管理UserSession表
            
            // 9. 更新用户最后登录时间
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // 10. 重置登录失败次数
            securityService.resetFailedAttempts(user.getUserId());

            // 11. 记录成功登录
            securityService.recordLoginAttempt(req.getUserId(), ipAddress, userAgent, true, null);

            // 12. 生成JWT Token
            String token = jwtUtil.generateToken(user.getUserId() + "|" + user.getType());
            
            // 13. 返回登录成功信息
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "登录成功");
            successResponse.put("token", token);
            successResponse.put("deviceId", deviceId);
            successResponse.put("userId", user.getUserId());
            successResponse.put("userName", user.getName());
            successResponse.put("userType", user.getType());
            successResponse.put("maxDevices", user.getMaxDevices());
            successResponse.put("currentDevices", springSessionService.getActiveSessionCount(user.getUserId()));
            successResponse.put("deviceName", deviceName);
            successResponse.put("loginTime", LocalDateTime.now());
            
            // 14. 记录审计日志
            securityService.logAction(user.getUserId(), "USER_LOGIN", "LOGIN", ipAddress, userAgent, 
                    "用户登录成功，设备: " + deviceName, "SUCCESS");
            
            return ResponseEntity.ok().body(successResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "系统内部错误: " + e.getMessage());
            errorResponse.put("code", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest req, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        // 使用Spring Session管理登出
        // 获取当前会话并使其过期
        String sessionId = request.getSession().getId();
        springSessionService.forceLogoutSession(sessionId);
        
        // 记录审计日志
        securityService.logAction(req.getUserId(), "USER_LOGOUT", "LOGOUT", ipAddress, userAgent, 
                "用户登出", "SUCCESS");
        
        return ResponseEntity.ok("已登出");
    }

    @GetMapping("/user/sessions/{userId}")
    public ResponseEntity<?> getUserSessions(@PathVariable String userId) {
        // 获取用户的所有Spring Session会话信息
        SpringSessionService.UserSessionStats sessionStats = springSessionService.getUserSessionStats(userId);
        return ResponseEntity.ok(sessionStats);
    }
    
    @GetMapping("/user/devices/{userId}")
    public ResponseEntity<?> getUserDevices(@PathVariable String userId) {
        // 获取用户的所有设备信息
        return ResponseEntity.ok(securityService.getUserDevices(userId));
    }
    
    @DeleteMapping("/user/devices/{userId}/{deviceId}")
    public ResponseEntity<?> deactivateDevice(@PathVariable String userId, @PathVariable String deviceId) {
        securityService.deactivateDevice(userId, deviceId);
        return ResponseEntity.ok("设备已停用");
    }

    @GetMapping("/user/status/{userId}")
    public ResponseEntity<?> getUserStatus(@PathVariable String userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }
        
        // 构建状态信息
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("userId", user.getUserId());
        statusInfo.put("status", user.getStatus());
        statusInfo.put("failedLoginAttempts", user.getFailedLoginAttempts());
        statusInfo.put("accountLockedUntil", user.getAccountLockedUntil());
        statusInfo.put("isAccountLocked", securityService.isAccountLocked(userId));
        statusInfo.put("lastLoginAt", user.getLastLoginAt());
        statusInfo.put("createdAt", user.getCreatedAt());
        
        return ResponseEntity.ok(statusInfo);
    }
    
    @PostMapping("/user/unlock/{userId}")
    public ResponseEntity<?> unlockUser(@PathVariable String userId) {
        securityService.resetFailedAttempts(userId);
        return ResponseEntity.ok("账户已解锁");
    }

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

    // DTO
    @Setter
    @Getter
    public static class LoginRequest {
        private String userId;
        private String password;
        private String deviceId;
        private String deviceName;
        private String deviceType;
        private String type; // 可选的用户类型验证
    }
    
    @Setter
    @Getter
    public static class LogoutRequest {
        private String userId;
        private String deviceId;
    }
    
    @Getter
    @Setter
    public static class LoginResponse {
        private String token;
        private String deviceId;
        private String deviceName;
        private String userId;
        private String userName;
        private String userType;
        private Integer maxDevices;
        private Integer currentDevices;
        
        public LoginResponse(String token, String deviceId) {
            this.token = token;
            this.deviceId = deviceId;
        }
    }
}