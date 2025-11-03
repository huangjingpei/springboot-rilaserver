package net.enjoy.springboot.registrationlogin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.enjoy.springboot.registrationlogin.dto.LiveStreamConfigDto;
import net.enjoy.springboot.registrationlogin.dto.StreamFetchResult;
import net.enjoy.springboot.registrationlogin.entity.LiveStreamConfig;
import net.enjoy.springboot.registrationlogin.entity.LiveStreamStatusLog;
import net.enjoy.springboot.registrationlogin.repository.LiveStreamConfigRepository;
import net.enjoy.springboot.registrationlogin.repository.LiveStreamStatusLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveStreamConfigServiceImpl implements LiveStreamConfigService {
    
    private final LiveStreamConfigRepository configRepository;
    private final LiveStreamStatusLogRepository statusLogRepository;
    private final PythonScriptService pythonScriptService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "live_stream_config:";
    private static final String CACHE_KEY_CLIENT_PREFIX = "live_stream_client:";
    private static final int CACHE_DURATION = 3600; // 1小时
    
    @Override
    @Transactional
    public List<LiveStreamConfigDto> saveOrUpdateConfigsFromExcel(String clientId, byte[] excelData) {
        // TODO: 实现Excel解析逻辑
        log.info("批量保存/更新直播间配置: clientId={}, excelSize={}", clientId, excelData.length);
        
        // 临时返回空列表，后续实现Excel解析
        return List.of();
    }
    
    @Override
    @Transactional
    public LiveStreamConfigDto saveOrUpdateConfig(LiveStreamConfigDto configDto) {
        log.info("保存/更新直播间配置: clientId={}, url={}", configDto.getClientId(), configDto.getLiveUrl());
        
        // 检查是否已存在相同客户端ID和URL的配置
        LiveStreamConfig existingConfig = configRepository.findByClientIdAndLiveUrl(
            configDto.getClientId(), configDto.getLiveUrl());
        
        LiveStreamConfig config;
        if (existingConfig != null) {
            // 更新现有配置
            config = existingConfig;
            updateConfigFromDto(config, configDto);
            log.info("更新现有配置: id={}", config.getId());
        } else {
            // 创建新配置
            config = configDto.toEntity();
            log.info("创建新配置: clientId={}, url={}", config.getClientId(), config.getLiveUrl());
        }
        
        config = configRepository.save(config);
        updateCache(config);
        
        return LiveStreamConfigDto.fromEntity(config);
    }
    
    @Override
    public LiveStreamConfigDto getConfigById(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        
        // 先从缓存获取
        LiveStreamConfig cached = (LiveStreamConfig) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return LiveStreamConfigDto.fromEntity(cached);
        }
        
        // 从数据库获取
        LiveStreamConfig config = configRepository.findById(id).orElse(null);
        if (config != null) {
            updateCache(config);
            return LiveStreamConfigDto.fromEntity(config);
        }
        
        return null;
    }
    
    @Override
    public List<LiveStreamConfigDto> getConfigsByClientId(String clientId) {
        String cacheKey = CACHE_KEY_CLIENT_PREFIX + clientId;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<LiveStreamConfig> cached = (List<LiveStreamConfig>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached.stream()
                .map(LiveStreamConfigDto::fromEntity)
                .collect(Collectors.toList());
        }
        
        // 从数据库获取
        List<LiveStreamConfig> configs = configRepository.findByClientId(clientId);
        if (!configs.isEmpty()) {
            updateClientCache(clientId, configs);
        }
        
        return configs.stream()
            .map(LiveStreamConfigDto::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<LiveStreamConfigDto> getConfigsByClientId(String clientId, Pageable pageable) {
        Page<LiveStreamConfig> configs = configRepository.findByClientId(clientId, pageable);
        return configs.map(LiveStreamConfigDto::fromEntity);
    }
    
    @Override
    @Transactional
    public void deleteConfigsByClientId(String clientId) {
        log.info("删除客户端所有配置: clientId={}", clientId);
        configRepository.deleteByClientId(clientId);
        clearClientCache(clientId);
    }
    
    @Override
    @Transactional
    public void deleteConfigById(Long id) {
        log.info("删除配置: id={}", id);
        LiveStreamConfig config = configRepository.findById(id).orElse(null);
        if (config != null) {
            configRepository.deleteById(id);
            clearCache(id);
            clearClientCache(config.getClientId());
        }
    }
    
    @Override
    @Transactional
    public LiveStreamConfigDto setConfigActive(Long id, boolean active) {
        log.info("设置配置状态: id={}, active={}", id, active);
        
        LiveStreamConfig config = configRepository.findById(id).orElse(null);
        if (config != null) {
            config.setIsActive(active);
            config = configRepository.save(config);
            updateCache(config);
            clearClientCache(config.getClientId());
            return LiveStreamConfigDto.fromEntity(config);
        }
        
        return null;
    }
    
    @Override
    public ClientStats getClientStats(String clientId) {
        ClientStats stats = new ClientStats();
        stats.setClientId(clientId);
        stats.setTotalConfigs(configRepository.countByClientId(clientId));
        stats.setActiveConfigs(configRepository.countByClientIdAndIsActiveTrue(clientId));
        stats.setLiveConfigs(configRepository.countByClientIdAndIsLiveTrue(clientId));
        return stats;
    }
    
    @Override
    @Async
    public void triggerConfigUpdate(Long configId) {
        log.info("手动触发配置更新: configId={}", configId);
        
        LiveStreamConfig config = configRepository.findById(configId).orElse(null);
        if (config != null && config.getIsActive()) {
            updateConfigStatus(config);
        }
    }
    
    @Override
    @Async
    public void triggerClientConfigsUpdate(String clientId) {
        log.info("手动触发客户端所有配置更新: clientId={}", clientId);
        
        List<LiveStreamConfig> configs = configRepository.findByClientIdAndIsActiveTrue(clientId);
        for (LiveStreamConfig config : configs) {
            updateConfigStatus(config);
        }
    }
    
    @Override
    public List<StatusLogDto> getConfigStatusLogs(Long configId) {
        return statusLogRepository.findByConfigIdOrderByChangeTimeDesc(configId)
            .stream()
            .map(this::convertToStatusLogDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<StatusLogDto> getClientStatusLogs(String clientId) {
        return statusLogRepository.findByClientIdOrderByChangeTimeDesc(clientId)
            .stream()
            .map(this::convertToStatusLogDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 更新配置状态（通过Python脚本获取最新信息）
     */
    @Async
    public void updateConfigStatus(LiveStreamConfig config) {
        if (!config.getIsActive() || !config.isInLiveTimeRange()) {
            log.debug("配置不需要更新: id={}, active={}, inTimeRange={}", 
                config.getId(), config.getIsActive(), config.isInLiveTimeRange());
            return;
        }
        
        if (!config.needsUpdate()) {
            log.debug("配置更新时间未到: id={}, lastCheckTime={}", 
                config.getId(), config.getLastCheckTime());
            return;
        }
        
        log.info("开始更新配置状态: id={}, platform={}, url={}", 
            config.getId(), config.getPlatform(), config.getLiveUrl());
        
        try {
            // 执行Python脚本获取最新状态
            CompletableFuture<StreamFetchResult> future = pythonScriptService.fetchStreamAsync(
                config.getPlatform(), config.getLiveUrl(), config.getQuality());
            
            StreamFetchResult result = future.get();
            
            // 更新配置
            boolean oldStatus = config.getIsLive();
            boolean newStatus = result.isSuccess() && result.getData() != null && 
                Boolean.TRUE.equals(result.getData().getIs_live());
            
            updateConfigFromResult(config, result);
            config.setLastCheckTime(LocalDateTime.now());
            config.setLastUpdateTime(LocalDateTime.now());
            
            configRepository.save(config);
            updateCache(config);
            
            // 记录状态变更
            if (oldStatus != newStatus) {
                recordStatusChange(config, oldStatus, newStatus, 
                    newStatus ? "开始直播" : "结束直播");
                log.info("配置状态变更: id={}, oldStatus={}, newStatus={}", 
                    config.getId(), oldStatus, newStatus);
            }
            
        } catch (Exception e) {
            log.error("更新配置状态失败: id={}", config.getId(), e);
            config.setErrorMessage(e.getMessage());
            config.setRetryCount(config.getRetryCount() + 1);
            config.setLastCheckTime(LocalDateTime.now());
            configRepository.save(config);
            updateCache(config);
        }
    }
    
    /**
     * 从DTO更新配置
     */
    private void updateConfigFromDto(LiveStreamConfig config, LiveStreamConfigDto dto) {
        config.setStreamName(dto.getStreamName());
        config.setStartTime(java.time.LocalTime.parse(dto.getStartTime(), 
            DateTimeFormatter.ofPattern("HH:mm:ss")));
        config.setEndTime(java.time.LocalTime.parse(dto.getEndTime(), 
            DateTimeFormatter.ofPattern("HH:mm:ss")));
        config.setPlatform(dto.getPlatform());
        config.setQuality(dto.getQuality());
        config.setIsActive(dto.getIsActive());
    }
    
    /**
     * 从Python脚本结果更新配置
     */
    private void updateConfigFromResult(LiveStreamConfig config, StreamFetchResult result) {
        if (result.isSuccess() && result.getData() != null) {
            StreamFetchResult.StreamData data = result.getData();
            config.setIsLive(Boolean.TRUE.equals(data.getIs_live()));
            config.setAnchorName(data.getAnchor_name());
            config.setTitle(data.getTitle());
            config.setM3u8Url(data.getM3u8_url());
            config.setFlvUrl(data.getFlv_url());
            config.setRecordUrl(data.getRecord_url());
            config.setCookies(data.getNew_cookies());
            config.setToken(data.getNew_token());
            config.setExtraData(data.getExtra());
            config.setErrorMessage(null);
            config.setRetryCount(0);
        } else {
            config.setIsLive(false);
            config.setErrorMessage(result.getMessage());
            config.setRetryCount(config.getRetryCount() + 1);
        }
    }
    
    /**
     * 记录状态变更
     */
    private void recordStatusChange(LiveStreamConfig config, boolean oldStatus, boolean newStatus, String reason) {
        LiveStreamStatusLog log = new LiveStreamStatusLog();
        log.setConfigId(config.getId());
        log.setClientId(config.getClientId());
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangeReason(reason);
        statusLogRepository.save(log);
    }
    
    /**
     * 转换为状态日志DTO
     */
    private StatusLogDto convertToStatusLogDto(LiveStreamStatusLog log) {
        StatusLogDto dto = new StatusLogDto();
        dto.setId(log.getId());
        dto.setConfigId(log.getConfigId());
        dto.setClientId(log.getClientId());
        dto.setOldStatus(log.getOldStatus());
        dto.setNewStatus(log.getNewStatus());
        dto.setChangeTime(log.getChangeTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dto.setChangeReason(log.getChangeReason());
        return dto;
    }
    
    /**
     * 更新缓存
     */
    private void updateCache(LiveStreamConfig config) {
        String cacheKey = CACHE_KEY_PREFIX + config.getId();
        redisTemplate.opsForValue().set(cacheKey, config, java.time.Duration.ofSeconds(CACHE_DURATION));
    }
    
    /**
     * 更新客户端缓存
     */
    private void updateClientCache(String clientId, List<LiveStreamConfig> configs) {
        String cacheKey = CACHE_KEY_CLIENT_PREFIX + clientId;
        redisTemplate.opsForValue().set(cacheKey, configs, java.time.Duration.ofSeconds(CACHE_DURATION));
    }
    
    /**
     * 清除缓存
     */
    private void clearCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    /**
     * 清除客户端缓存
     */
    private void clearClientCache(String clientId) {
        String cacheKey = CACHE_KEY_CLIENT_PREFIX + clientId;
        redisTemplate.delete(cacheKey);
    }
}

