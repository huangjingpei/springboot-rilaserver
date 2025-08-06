package net.enjoy.springboot.registrationlogin.zlmediakit.controller;

import net.enjoy.springboot.registrationlogin.entity.StreamInfo;
import net.enjoy.springboot.registrationlogin.repository.StreamInfoRepository;
import net.enjoy.springboot.registrationlogin.utils.JwtUtil;
import net.enjoy.springboot.registrationlogin.zlmediakit.config.ExZlmProperties;
import net.enjoy.springboot.registrationlogin.config.ZLMediaKitConfig;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.service.StreamService;
import net.enjoy.springboot.registrationlogin.service.UserService;
import io.github.lunasaw.zlm.api.ZlmRestService;
import io.github.lunasaw.zlm.entity.ServerResponse;
import io.github.lunasaw.zlm.entity.Version;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "zlm-Service", description = "zlm-Service")
@RequestMapping("/zlm")
@RestController
@RequiredArgsConstructor
public class ZlmRestController {

    private final ExZlmProperties exZlmProperties;

    @Autowired
    private ZLMediaKitConfig zlmediaKitConfig;

    @Autowired
    private StreamService streamService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StreamInfoRepository streamInfoRepository;

    @Autowired
    private net.enjoy.springboot.registrationlogin.service.StreamLimitService streamLimitService;

    private static final Logger log = LoggerFactory.getLogger(ZlmRestController.class);

    /**
     * 创建标准错误响应
     */
    private Map<String, Object> createErrorResponse(String message, String code) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("code", code);
        errorResponse.put("timestamp", net.enjoy.springboot.registrationlogin.config.TimeZoneConfig.formatDateTime(
                net.enjoy.springboot.registrationlogin.config.TimeZoneConfig.getNow()));
        return errorResponse;
    }

    /**
     * 创建标准成功响应
     */
    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("data", data);
        successResponse.put("timestamp", net.enjoy.springboot.registrationlogin.config.TimeZoneConfig.formatDateTime(
                net.enjoy.springboot.registrationlogin.config.TimeZoneConfig.getNow()));
        return successResponse;
    }

    /**
     * 从JWT token中提取用户名
     */
    private String getUsernameFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        try {
            // 使用JwtUtil解析token
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            log.error("解析JWT token失败", e);
            return null;
        }
    }

    @GetMapping("/getApiList")
    public ServerResponse<List<String>> getApiList() {
        // 从配置中动态获取host和secret
        String host = exZlmProperties.getNodes().get(0).getHost();
        String secret = exZlmProperties.getNodes().get(0).getSecret();
        return ZlmRestService.getApiList(host, secret);
    }

    @GetMapping("/getProperties")
    public ServerResponse<ExZlmProperties> getProperties() {
        ServerResponse<ExZlmProperties> result = new ServerResponse<>();
        result.setData(exZlmProperties);
        return result;
    }

    @GetMapping("/getVersion")
    public ServerResponse<Version> getVersion() {
        // 从配置中动态获取host和secret，避免硬编码
        String host = exZlmProperties.getNodes().get(0).getHost();
        String secret = exZlmProperties.getNodes().get(0).getSecret();
        return ZlmRestService.getVersion(host, secret);
    }

    /**
     * 获取一个由服务端管理的、安全的推流地址
     * 该接口需要用户通过JWT认证
     * @param request HttpServletRequest对象
     * @return 包含streamId和pushUrl的Map
     */
    @GetMapping("/getPushUrl")
    public ResponseEntity<Map<String, Object>> getPushUrl(HttpServletRequest request) {
        try {
            // 从JWT token中获取用户名
            String username = getUsernameFromToken(request);
            if (username == null) {
                Map<String, Object> errorResponse = createErrorResponse("未提供有效的认证信息", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // 从用户名中提取纯userId（去掉类型后缀）
            String userId = username;
            if (username.contains("|")) {
                userId = username.substring(0, username.lastIndexOf("|"));
            }
            
            log.info("从JWT token提取的用户ID: {}", userId);
            
            // 查找用户
            User user = userService.findUserByEmail(userId);
            if (user == null) {
                log.error("用户不存在: {}", userId);
                Map<String, Object> errorResponse = createErrorResponse("用户账户不存在: " + userId, "USER_NOT_FOUND");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            log.info("找到用户: {}", user.getUserId());
            
            // 检查推流数量限制
            var streamLimitResult = streamLimitService.checkStreamLimit(user.getUserId());
            if (!streamLimitResult.isAllowed()) {
                log.warn("推流数量限制检查失败: userId={}, reason={}", user.getUserId(), streamLimitResult.getMessage());
                Map<String, Object> errorResponse = createErrorResponse(streamLimitResult.getMessage(), streamLimitResult.getCode());
                errorResponse.put("currentStreams", streamLimitResult.getCurrentStreams());
                errorResponse.put("maxStreams", streamLimitResult.getMaxStreams());
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            log.info("推流数量限制检查通过: userId={}, 当前推流数={}/{}", 
                    user.getUserId(), streamLimitResult.getCurrentStreams(), streamLimitResult.getMaxStreams());
            
            // 生成推流地址
            String streamId = "stream_" + user.getUserId() + "_" + System.currentTimeMillis();
            
            // 使用配置的动态主机地址生成推流URL
            String pushUrl = zlmediaKitConfig.generatePushUrl("live", streamId);
            
            // 保存流信息
            StreamInfo streamInfo = new StreamInfo();
            streamInfo.setStreamId(streamId);
            streamInfo.setUserId(user.getUserId());
            streamInfo.setPushUrl(pushUrl);
            streamInfo.setStatus(StreamInfo.StreamStatus.CREATED);
            streamInfo.setCreatedAt(LocalDateTime.now());
            streamInfoRepository.save(streamInfo);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("streamId", streamId);
            responseData.put("pushUrl", pushUrl);
            responseData.put("userId", user.getUserId());
            
            Map<String, Object> successResponse = createSuccessResponse(responseData);
            return ResponseEntity.ok(successResponse);
            
        } catch (Exception e) {
            log.error("获取推流地址失败", e);
            Map<String, Object> errorResponse = createErrorResponse("获取推流地址失败: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/testUser")
    public ResponseEntity<Map<String, Object>> testUser() {
        // 测试用户查找
        String testUserId = "huangjingpei@gmail.com";
        User user = userService.findUserByEmail(testUserId);
        
        Map<String, Object> responseData = new HashMap<>();
        if (user != null) {
            responseData.put("found", true);
            responseData.put("userId", user.getUserId());
            responseData.put("name", user.getName());
            responseData.put("status", user.getStatus());
        } else {
            responseData.put("found", false);
            responseData.put("searchedUserId", testUserId);
        }
        
        Map<String, Object> successResponse = createSuccessResponse(responseData);
        return ResponseEntity.ok(successResponse);
    }

    @GetMapping("/testAuth")
    public ResponseEntity<Map<String, Object>> testAuth(Authentication authentication) {
        Map<String, Object> responseData = new HashMap<>();
        
        if (authentication != null) {
            responseData.put("authenticated", true);
            responseData.put("name", authentication.getName());
            responseData.put("authorities", authentication.getAuthorities());
            responseData.put("details", authentication.getDetails());
        } else {
            responseData.put("authenticated", false);
            responseData.put("message", "No authentication found");
        }
        
        Map<String, Object> successResponse = createSuccessResponse(responseData);
        return ResponseEntity.ok(successResponse);
    }
}
