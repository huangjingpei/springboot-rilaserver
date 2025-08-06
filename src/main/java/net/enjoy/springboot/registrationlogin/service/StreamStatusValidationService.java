package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.config.ZLMediaKitConfig;
import net.enjoy.springboot.registrationlogin.entity.StreamInfo;
import net.enjoy.springboot.registrationlogin.repository.StreamInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 流状态验证服务
 * 用于验证和清理僵尸流（状态为CREATED但实际没有推流的流）
 */
@Service
public class StreamStatusValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(StreamStatusValidationService.class);
    
    @Autowired
    private StreamInfoRepository streamInfoRepository;
    
    @Autowired
    private ZLMediaKitConfig zlmediaKitConfig;
    
    /**
     * 僵尸流超时时间（分钟），超过这个时间的CREATED状态流被认为是僵尸流
     */
    @Value("${stream.zombie.timeout.minutes:30}")
    private int zombieTimeoutMinutes;
    
    /**
     * 验证并清理用户的僵尸流
     * @param userId 用户ID
     * @return 清理的僵尸流数量
     */
    public int validateAndCleanZombieStreams(String userId) {
        try {
            List<StreamInfo> zombieStreams = getZombieStreams(userId);
            int cleanedCount = 0;
            
            for (StreamInfo streamInfo : zombieStreams) {
                if (cleanZombieStream(streamInfo)) {
                    cleanedCount++;
                }
            }
            
            log.info("用户僵尸流清理完成: userId={}, 清理数量={}", userId, cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            log.error("清理用户僵尸流失败: userId={}, error={}", userId, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 获取用户的僵尸流列表
     * @param userId 用户ID
     * @return 僵尸流列表
     */
    public List<StreamInfo> getZombieStreams(String userId) {
        try {
            // 查找用户所有CREATED状态的流
            List<StreamInfo> createdStreams = streamInfoRepository.findByUserIdAndStatus(userId, StreamInfo.StreamStatus.CREATED);
            
            // 过滤出僵尸流（超过超时时间的CREATED状态流）
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(zombieTimeoutMinutes);
            
            return createdStreams.stream()
                    .filter(stream -> {
                        // 如果创建时间超过超时时间，认为是僵尸流
                        if (stream.getCreatedAt() != null && stream.getCreatedAt().isBefore(timeoutThreshold)) {
                            return true;
                        }
                        
                        // 如果更新时间超过超时时间，也认为是僵尸流
                        if (stream.getUpdatedAt() != null && stream.getUpdatedAt().isBefore(timeoutThreshold)) {
                            return true;
                        }
                        
                        return false;
                    })
                    .toList();
                    
        } catch (Exception e) {
            log.error("获取用户僵尸流列表失败: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 清理单个僵尸流
     * @param streamInfo 流信息
     * @return 是否清理成功
     */
    private boolean cleanZombieStream(StreamInfo streamInfo) {
        try {
            log.info("清理僵尸流: streamId={}, userId={}, createdAt={}", 
                    streamInfo.getStreamId(), streamInfo.getUserId(), streamInfo.getCreatedAt());
            
            // 更新流状态为STOPPED
            streamInfo.setStatus(StreamInfo.StreamStatus.STOPPED);
            streamInfo.setUpdatedAt(LocalDateTime.now());
            streamInfo.setEndTime(LocalDateTime.now());
            
            // 计算持续时间（如果开始时间存在）
            if (streamInfo.getStartTime() != null) {
                long duration = java.time.Duration.between(streamInfo.getStartTime(), streamInfo.getEndTime()).getSeconds();
                streamInfo.setDuration(duration);
            }
            
            streamInfoRepository.save(streamInfo);
            
            log.info("僵尸流清理成功: streamId={}, userId={}", streamInfo.getStreamId(), streamInfo.getUserId());
            return true;
            
        } catch (Exception e) {
            log.error("清理僵尸流失败: streamId={}, userId={}, error={}", 
                    streamInfo.getStreamId(), streamInfo.getUserId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 验证流是否真实存在（通过ZLMediaKit API）
     * 注意：这个方法需要ZLMediaKit提供查询流状态的API
     * @param streamId 流ID
     * @return 流是否存在
     */
    public boolean isStreamActuallyActive(String streamId) {
        try {
            // 这里需要调用ZLMediaKit的API来查询流状态
            // 由于当前代码中没有看到相关的API调用，暂时返回false
            // 实际实现时需要根据ZLMediaKit的API来查询
            
            // TODO: 实现ZLMediaKit流状态查询
            // 例如：调用 http://zlm-server:8080/index/api/getMediaList 来查询活跃流列表
            
            log.debug("流状态验证功能待实现: streamId={}", streamId);
            return false;
            
        } catch (Exception e) {
            log.error("验证流状态失败: streamId={}, error={}", streamId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 批量清理所有用户的僵尸流
     * @return 清理的僵尸流总数
     */
    public int cleanAllZombieStreams() {
        try {
            // 查找所有CREATED状态的流
            List<StreamInfo> allCreatedStreams = streamInfoRepository.findByStatus(StreamInfo.StreamStatus.CREATED);
            
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(zombieTimeoutMinutes);
            int cleanedCount = 0;
            
            for (StreamInfo streamInfo : allCreatedStreams) {
                // 检查是否为僵尸流
                boolean isZombie = false;
                if (streamInfo.getCreatedAt() != null && streamInfo.getCreatedAt().isBefore(timeoutThreshold)) {
                    isZombie = true;
                } else if (streamInfo.getUpdatedAt() != null && streamInfo.getUpdatedAt().isBefore(timeoutThreshold)) {
                    isZombie = true;
                }
                
                if (isZombie && cleanZombieStream(streamInfo)) {
                    cleanedCount++;
                }
            }
            
            log.info("全局僵尸流清理完成: 清理数量={}", cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            log.error("全局僵尸流清理失败: error={}", e.getMessage(), e);
            return 0;
        }
    }
} 