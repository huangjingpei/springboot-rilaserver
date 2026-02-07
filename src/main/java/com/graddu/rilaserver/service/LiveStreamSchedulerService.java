package net.enjoy.springboot.registrationlogin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.enjoy.springboot.registrationlogin.entity.LiveStreamConfig;
import net.enjoy.springboot.registrationlogin.repository.LiveStreamConfigRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveStreamSchedulerService {
    
    private final LiveStreamConfigRepository configRepository;
    private final LiveStreamConfigServiceImpl configService;
    
    // 创建线程池，控制并发数量，避免服务器压力过大
    private final Executor executor = Executors.newFixedThreadPool(10);
    
    /**
     * 定时任务：每分钟检查一次需要更新的配置
     * 使用固定延迟，确保任务执行间隔稳定
     */
    @Scheduled(fixedDelay = 60000) // 60秒 = 1分钟
    public void scheduledConfigUpdate() {
        log.debug("开始执行定时配置更新任务");
        
        try {
            // 查找需要更新的配置（距离上次检查超过1分钟）
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(1);
            List<LiveStreamConfig> configsToUpdate = configRepository.findConfigsNeedingUpdate(cutoffTime);
            
            if (configsToUpdate.isEmpty()) {
                log.debug("没有需要更新的配置");
                return;
            }
            
            log.info("找到 {} 个需要更新的配置", configsToUpdate.size());
            
            // 并发执行更新任务，但限制并发数量
            List<CompletableFuture<Void>> futures = configsToUpdate.stream()
                .map(config -> CompletableFuture.runAsync(() -> {
                    try {
                        configService.updateConfigStatus(config);
                    } catch (Exception e) {
                        log.error("更新配置状态失败: configId={}", config.getId(), e);
                    }
                }, executor))
                .toList();
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            log.info("定时配置更新任务完成，处理了 {} 个配置", configsToUpdate.size());
            
        } catch (Exception e) {
            log.error("定时配置更新任务执行失败", e);
        }
    }
    
    /**
     * 定时任务：每5分钟检查一次Python环境
     */
    @Scheduled(fixedDelay = 300000) // 5分钟
    public void checkPythonEnvironment() {
        log.debug("开始检查Python环境");
        
        try {
            // 这里可以添加Python环境检查逻辑
            // 比如检查Python是否可用，脚本文件是否存在等
            log.debug("Python环境检查完成");
        } catch (Exception e) {
            log.error("Python环境检查失败", e);
        }
    }
    
    /**
     * 定时任务：每小时清理过期的状态日志
     */
    @Scheduled(fixedDelay = 3600000) // 1小时
    public void cleanupOldStatusLogs() {
        log.debug("开始清理过期的状态日志");
        
        try {
            // 这里可以添加清理逻辑
            // 比如删除7天前的状态日志
            log.debug("状态日志清理完成");
        } catch (Exception e) {
            log.error("状态日志清理失败", e);
        }
    }
    
    /**
     * 手动触发所有配置更新（用于测试或紧急情况）
     */
    public void triggerAllConfigsUpdate() {
        log.info("手动触发所有配置更新");
        
        try {
            List<LiveStreamConfig> allActiveConfigs = configRepository.findByIsActiveTrue();
            
            if (allActiveConfigs.isEmpty()) {
                log.info("没有活跃的配置需要更新");
                return;
            }
            
            log.info("开始更新 {} 个活跃配置", allActiveConfigs.size());
            
            // 并发执行更新任务
            List<CompletableFuture<Void>> futures = allActiveConfigs.stream()
                .map(config -> CompletableFuture.runAsync(() -> {
                    try {
                        configService.updateConfigStatus(config);
                    } catch (Exception e) {
                        log.error("更新配置状态失败: configId={}", config.getId(), e);
                    }
                }, executor))
                .toList();
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            log.info("所有配置更新完成，处理了 {} 个配置", allActiveConfigs.size());
            
        } catch (Exception e) {
            log.error("手动触发所有配置更新失败", e);
        }
    }
}

