package net.enjoy.springboot.registrationlogin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 流清理定时任务
 * 定期清理僵尸流和过期流
 */
@Service
public class StreamCleanupScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(StreamCleanupScheduler.class);
    
    @Autowired
    private StreamStatusValidationService streamStatusValidationService;
    
    /**
     * 是否启用定时清理任务
     */
    @Value("${stream.cleanup.scheduler.enabled:true}")
    private boolean cleanupSchedulerEnabled;
    
    /**
     * 定时清理僵尸流（每小时执行一次）
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void cleanupZombieStreams() {
        if (!cleanupSchedulerEnabled) {
            log.debug("流清理定时任务已禁用");
            return;
        }
        
        try {
            log.info("开始执行僵尸流清理定时任务");
            int cleanedCount = streamStatusValidationService.cleanAllZombieStreams();
            log.info("僵尸流清理定时任务完成: 清理数量={}", cleanedCount);
            
        } catch (Exception e) {
            log.error("僵尸流清理定时任务执行失败: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 手动触发僵尸流清理
     * @return 清理的僵尸流数量
     */
    public int manualCleanupZombieStreams() {
        try {
            log.info("手动触发僵尸流清理");
            int cleanedCount = streamStatusValidationService.cleanAllZombieStreams();
            log.info("手动僵尸流清理完成: 清理数量={}", cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            log.error("手动僵尸流清理失败: error={}", e.getMessage(), e);
            return 0;
        }
    }
} 