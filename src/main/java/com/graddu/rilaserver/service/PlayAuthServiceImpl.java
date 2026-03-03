package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.PlayAuthResult;
import com.graddu.rilaserver.entity.StreamInfo;
import com.graddu.rilaserver.entity.User;
import com.graddu.rilaserver.repository.StreamInfoRepository;
import com.graddu.rilaserver.repository.UserRepository;
import com.graddu.rilaserver.utils.JwtUtil;
import com.graddu.rilaserver.entity.UserStatus;
import com.graddu.rilaserver.config.ZLMediaKitConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 播放鉴权服务实现
 */
@Service
public class PlayAuthServiceImpl implements PlayAuthService {
    
    private static final Logger log = LoggerFactory.getLogger(PlayAuthServiceImpl.class);
    
    @Autowired
    private StreamInfoRepository streamInfoRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ZLMediaKitConfig zlmediaKitConfig;
    
    @Autowired
    private SecurityService securityService;
    
    // 默认播放令牌有效期：24小时
    private static final long DEFAULT_PLAY_TOKEN_EXPIRE_SECONDS = 24 * 60 * 60;
    
    @Override
    public PlayAuthResult verifyPlayAuth(String streamId, String userId, String token, String ip, String protocol) {
        log.info("播放鉴权验证: streamId={}, userId={}, ip={}, protocol={}", streamId, userId, ip, protocol);
        
        try {
            // 1. 检查流是否存在且可播放
            if (!isStreamPlayable(streamId)) {
                log.warn("流不存在或不可播放: streamId={}", streamId);
                return PlayAuthResult.failure(1001, "流不存在或不可播放");
            }
            
            // 2. IP黑名单检查
            if (securityService != null && securityService.isIpBlocked(ip)) {
                log.warn("IP被阻止: ip={}", ip);
                return PlayAuthResult.failure(1002, "IP地址被阻止");
            }
            
            // 3. 如果提供了播放令牌，优先验证令牌
            if (StringUtils.hasText(token)) {
                if (validatePlayToken(token, streamId)) {
                    log.info("播放令牌验证成功: streamId={}, token=***", streamId);
                    return createSuccessResult(streamId);
                } else {
                    log.warn("播放令牌验证失败: streamId={}, token=***", streamId);
                    return PlayAuthResult.failure(1003, "播放令牌无效或已过期");
                }
            }
            
            // 4. 如果提供了用户ID，验证用户权限
            if (StringUtils.hasText(userId)) {
                // 验证用户是否存在且状态正常
                User user = userRepository.findByUserId(userId);
                if (user == null) {
                    log.warn("用户不存在: userId={}", userId);
                    return PlayAuthResult.failure(1004, "用户不存在");
                }
                
                if (user.getStatus() != UserStatus.ACTIVE) {
                    log.warn("用户状态异常: userId={}, status={}", userId, user.getStatus());
                    return PlayAuthResult.failure(1005, "用户状态异常: " + user.getStatus().getDescription());
                }
                
                // 检查用户是否有播放权限
                if (hasPlayPermission(userId, streamId)) {
                    log.info("用户播放权限验证成功: userId={}, streamId={}", userId, streamId);
                    // 生成播放令牌
                    String playToken = generatePlayToken(streamId, userId, DEFAULT_PLAY_TOKEN_EXPIRE_SECONDS);
                    long expireTime = System.currentTimeMillis() + DEFAULT_PLAY_TOKEN_EXPIRE_SECONDS * 1000;
                    return PlayAuthResult.success(playToken, expireTime);
                } else {
                    log.warn("用户无播放权限: userId={}, streamId={}", userId, streamId);
                    return PlayAuthResult.failure(1006, "无播放权限");
                }
            }
            
            // 5. 匿名播放策略（根据业务需求决定是否允许）
            Optional<StreamInfo> streamInfoOpt = streamInfoRepository.findByStreamId(streamId);
            if (streamInfoOpt.isPresent()) {
                StreamInfo streamInfo = streamInfoOpt.get();
                // 这里可以根据流的属性决定是否允许匿名播放
                // 例如：公开流允许匿名播放，私有流需要认证
                if (isPublicStream(streamInfo)) {
                    log.info("公开流允许匿名播放: streamId={}", streamId);
                    return createSuccessResult(streamId);
                } else {
                    log.warn("私有流需要认证: streamId={}", streamId);
                    return PlayAuthResult.failure(1007, "该流需要认证后才能播放");
                }
            }
            
            // 默认拒绝
            log.warn("播放鉴权失败，默认拒绝: streamId={}", streamId);
            return PlayAuthResult.failure(1008, "播放鉴权失败");
            
        } catch (Exception e) {
            log.error("播放鉴权异常: streamId={}, error={}", streamId, e.getMessage(), e);
            return PlayAuthResult.failure(1999, "服务器内部错误");
        }
    }
    
    @Override
    public String generatePlayToken(String streamId, String userId, long expireSeconds) {
        try {
            // 使用JWT生成播放令牌，包含流ID、用户ID、过期时间等信息
            String payload = String.format("play|%s|%s|%d", streamId, userId, 
                System.currentTimeMillis() + expireSeconds * 1000);
            return jwtUtil.generateToken(payload);
        } catch (Exception e) {
            log.error("生成播放令牌失败: streamId={}, userId={}, error={}", streamId, userId, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public boolean validatePlayToken(String token, String streamId) {
        try {
            if (!StringUtils.hasText(token)) {
                return false;
            }
            
            // 解析JWT令牌
            String payload = jwtUtil.getUsernameFromToken(token);
            if (!StringUtils.hasText(payload)) {
                return false;
            }
            
            // 验证令牌格式：play|streamId|userId|expireTime
            String[] parts = payload.split("\\|");
            if (parts.length != 4 || !"play".equals(parts[0])) {
                return false;
            }
            
            String tokenStreamId = parts[1];
            String userId = parts[2];
            long expireTime = Long.parseLong(parts[3]);
            
            // 验证流ID匹配
            if (!streamId.equals(tokenStreamId)) {
                log.warn("播放令牌流ID不匹配: token streamId={}, request streamId={}", tokenStreamId, streamId);
                return false;
            }
            
            // 验证是否过期
            if (System.currentTimeMillis() > expireTime) {
                log.warn("播放令牌已过期: streamId={}, userId={}", streamId, userId);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("验证播放令牌异常: token=***, streamId={}, error={}", streamId, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isStreamPlayable(String streamId) {
        try {
            Optional<StreamInfo> streamInfoOpt = streamInfoRepository.findByStreamId(streamId);
            if (streamInfoOpt.isEmpty()) {
                return false;
            }
            
            StreamInfo streamInfo = streamInfoOpt.get();
            // 只有推流中的流才能播放
            // 注意：流状态应该只反映推流者状态，PLAYING不应该作为流状态
            return streamInfo.getStatus() == StreamInfo.StreamStatus.PUSHING;
                   
        } catch (Exception e) {
            log.error("检查流状态异常: streamId={}, error={}", streamId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean hasPlayPermission(String userId, String streamId) {
        try {
            Optional<StreamInfo> streamInfoOpt = streamInfoRepository.findByStreamId(streamId);
            if (streamInfoOpt.isEmpty()) {
                return false;
            }
            
            StreamInfo streamInfo = streamInfoOpt.get();
            
            // 1. 流的所有者总是有播放权限
            if (userId.equals(streamInfo.getUserId())) {
                return true;
            }
            
            // 2. 检查用户是否存在且状态正常
            User user = userRepository.findByUserId(userId);
            if (user == null || user.getStatus() != UserStatus.ACTIVE) {
                return false;
            }
            
            // 3. 这里可以扩展更复杂的权限逻辑
            // 例如：基于角色的权限控制、付费用户权限、好友关系等
            
            // 4. 公开流允许所有已认证用户播放
            if (isPublicStream(streamInfo)) {
                return true;
            }
            
            // 5. 其他权限判断逻辑...
            // TODO: 实现更复杂的权限控制逻辑
            
            return false;
            
        } catch (Exception e) {
            log.error("检查播放权限异常: userId={}, streamId={}, error={}", userId, streamId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 判断是否为公开流
     */
    private boolean isPublicStream(StreamInfo streamInfo) {
        // 这里可以根据流的属性或配置来判断
        // 目前简单认为所有流都是公开的，实际业务中可以增加流的可见性配置
        return true;
    }
    
    /**
     * 创建成功结果，包含播放地址信息
     */
    private PlayAuthResult createSuccessResult(String streamId) {
        // 生成播放地址
        PlayAuthResult.PlayUrlInfo playUrlInfo = new PlayAuthResult.PlayUrlInfo();
        playUrlInfo.setRtmpUrl(String.format("rtmp://%s:%d/live/%s", zlmediaKitConfig.getBaseUrl(), zlmediaKitConfig.getRtmpPort(), streamId));
        playUrlInfo.setHlsUrl(String.format("http://%s:%d/live/%s.live.m3u8", zlmediaKitConfig.getBaseUrl(), zlmediaKitConfig.getHttpPort(), streamId));
        playUrlInfo.setFlvUrl(String.format("http://%s:%d/live/%s.live.flv", zlmediaKitConfig.getBaseUrl(), zlmediaKitConfig.getHttpPort(), streamId));
        playUrlInfo.setWebrtcUrl(String.format("webrtc://%s:8080/live/%s", zlmediaKitConfig.getBaseUrl(), streamId));
        
        return PlayAuthResult.success(playUrlInfo);
    }
} 