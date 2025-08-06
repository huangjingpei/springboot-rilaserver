package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.entity.StreamInfo;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.repository.StreamInfoRepository;
import net.enjoy.springboot.registrationlogin.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 推流限制服务实现
 */
@Service
public class StreamLimitServiceImpl implements StreamLimitService {

    private static final Logger log = LoggerFactory.getLogger(StreamLimitServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreamInfoRepository streamInfoRepository;
    
    @Autowired
    private StreamStatusValidationService streamStatusValidationService;

    @Override
    public boolean canUserStartStream(String userId) {
        return checkStreamLimit(userId).isAllowed();
    }

    @Override
    public StreamLimitCheckResult checkStreamLimit(String userId) {
        try {
            // 查找用户
            User user = userRepository.findByUserId(userId);
            if (user == null) {
                log.warn("推流限制检查失败：用户不存在, userId={}", userId);
                return StreamLimitCheckResult.userNotFound();
            }

            // 在检查推流限制之前，先验证和清理僵尸流
            int cleanedCount = streamStatusValidationService.validateAndCleanZombieStreams(userId);
            if (cleanedCount > 0) {
                log.info("清理了{}个僵尸流: userId={}", cleanedCount, userId);
            }

            // 获取用户的最大推流数限制
            int maxStreams = user.getMaxStreams() != null ? user.getMaxStreams() : 1;
            
            // 获取用户当前推流数量（只统计PUSHING状态的流）
            int currentStreams = getCurrentStreamCount(userId);

            log.info("推流限制检查: userId={}, 当前推流数={}, 最大推流数={}", userId, currentStreams, maxStreams);

            // 检查是否超过限制
            if (currentStreams >= maxStreams) {
                return StreamLimitCheckResult.limitExceeded(currentStreams, maxStreams);
            }

            return StreamLimitCheckResult.allowed(currentStreams, maxStreams);

        } catch (Exception e) {
            log.error("推流限制检查异常: userId={}, error={}", userId, e.getMessage(), e);
            return new StreamLimitCheckResult(false, "系统错误", "SYSTEM_ERROR", 0, 0);
        }
    }

    @Override
    public int getCurrentStreamCount(String userId) {
        try {
            // 统计用户当前正在推流的数量（只统计PUSHING状态的流）
            return streamInfoRepository.findActiveStreamsByUserId(userId).size();
        } catch (Exception e) {
            log.error("获取用户当前推流数量失败: userId={}, error={}", userId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int getMaxStreamCount(String userId) {
        try {
            User user = userRepository.findByUserId(userId);
            if (user == null) {
                return 0;
            }
            return user.getMaxStreams() != null ? user.getMaxStreams() : 1;
        } catch (Exception e) {
            log.error("获取用户最大推流数限制失败: userId={}, error={}", userId, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 手动清理用户的僵尸流
     */
    public int cleanZombieStreams(String userId) {
        return streamStatusValidationService.validateAndCleanZombieStreams(userId);
    }
    
    /**
     * 获取用户的僵尸流列表
     */
    public List<StreamInfo> getZombieStreams(String userId) {
        return streamStatusValidationService.getZombieStreams(userId);
    }
} 