package net.enjoy.springboot.registrationlogin.controller;

import net.enjoy.springboot.registrationlogin.dto.LoginDto;
import net.enjoy.springboot.registrationlogin.dto.UserDto;
import net.enjoy.springboot.registrationlogin.entity.StreamInfo;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.repository.UserRepository;
import net.enjoy.springboot.registrationlogin.service.SecurityService;
import net.enjoy.springboot.registrationlogin.service.SpringSessionService;
import net.enjoy.springboot.registrationlogin.service.StreamService;
import net.enjoy.springboot.registrationlogin.service.UserService;
import net.enjoy.springboot.registrationlogin.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final StreamService streamService;
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private SpringSessionService springSessionService;

    public AdminController(UserService userService, StreamService streamService) {
        this.userService = userService;
        this.streamService = streamService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String email = loginDto.getEmail();
        
        // 1. 检查用户状态（早期检查）
        if (!securityService.checkUserStatus(email)) {
            User user = userRepository.findByUserId(email);
            if (user == null) {
                securityService.recordLoginAttempt(email, ipAddress, userAgent, false, "用户不存在");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户不存在");
                errorResponse.put("code", "USER_NOT_FOUND");
                return ResponseEntity.badRequest().body(errorResponse);
            } else {
                securityService.recordLoginAttempt(email, ipAddress, userAgent, false, "用户状态异常: " + user.getStatus());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户账户状态异常: " + user.getStatus().getDescription());
                errorResponse.put("code", "USER_STATUS_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        // 2. 执行安全检查（锁定、IP阻止等）
        if (!securityService.performSecurityChecks(email, ipAddress)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            if (securityService.isAccountLocked(email)) {
                errorResponse.put("message", "账户已被锁定，请稍后再试");
                errorResponse.put("code", "ACCOUNT_LOCKED");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (securityService.isIpBlocked(ipAddress)) {
                errorResponse.put("message", "IP地址已被阻止，请稍后再试");
                errorResponse.put("code", "IP_BLOCKED");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            securityService.recordLoginAttempt(email, ipAddress, userAgent, false, "安全检查失败");
            errorResponse.put("message", "安全检查失败");
            errorResponse.put("code", "SECURITY_CHECK_FAILED");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // 3. 获取用户信息（此时用户已确认存在且状态正常）
        User user = userRepository.findByUserId(email);
        
        // 4. 验证密码
        if (user.getPassword() != null && !passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            securityService.recordLoginAttempt(email, ipAddress, userAgent, false, "密码错误");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "密码错误");
            errorResponse.put("code", "INVALID_PASSWORD");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // 5. 更新用户最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 6. 重置登录失败次数
        securityService.resetFailedAttempts(user.getUserId());

        // 7. 记录成功登录
        securityService.recordLoginAttempt(email, ipAddress, userAgent, true, null);

        // 8. 生成JWT Token
        String token = jwtUtil.generateToken(user.getUserId() + "|" + user.getType());
        
        // 9. 返回登录成功信息
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", "登录成功");
        successResponse.put("userId", user.getUserId());
        successResponse.put("userName", user.getName());
        successResponse.put("userType", user.getType());
        successResponse.put("loginTime", LocalDateTime.now());
        
        // 10. 记录审计日志
        securityService.logAction(user.getUserId(), "ADMIN_LOGIN", "LOGIN", ipAddress, userAgent, 
                "管理员登录成功", "SUCCESS");
        
        // 11. 将token放在响应头中
        return ResponseEntity.ok()
                .header("Authorization", token)
                .body(successResponse);
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

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/streams")
    public ResponseEntity<Map<String, StreamInfo>> getStreams() {
        // return ResponseEntity.ok(streamService.getStreamList());
        return ResponseEntity.ok(null); // Placeholder
    }
}