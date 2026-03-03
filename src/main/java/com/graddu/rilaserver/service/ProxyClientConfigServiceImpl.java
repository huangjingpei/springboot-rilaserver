package com.graddu.rilaserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.graddu.rilaserver.dto.ProxyClientConfigDto;
import com.graddu.rilaserver.entity.ProxyClientConfig;
import com.graddu.rilaserver.repository.ProxyClientConfigRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProxyClientConfigServiceImpl implements ProxyClientConfigService {

    private final ProxyClientConfigRepository proxyClientConfigRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "proxy_config:";
    private static final Duration CACHE_DURATION = Duration.ofHours(24);

    @Override
    @Transactional
    public ProxyClientConfigDto saveOrUpdateConfig(ProxyClientConfigDto configDto) {
        log.info("保存或更新代理客户端配置: userId={}", configDto.getUserId());
        
        // 检查是否已存在配置
        Optional<ProxyClientConfig> existingConfig = proxyClientConfigRepository.findByUserId(configDto.getUserId());
        
        ProxyClientConfig entity;
        if (existingConfig.isPresent()) {
            // 更新现有配置
            entity = existingConfig.get();
            log.info("更新现有配置: id={}", entity.getId());
        } else {
            // 创建新配置
            entity = new ProxyClientConfig();
            log.info("创建新配置");
        }
        
        // 转换DTO到实体
        ProxyClientConfig newEntity = configDto.toEntity();
        entity.setUserId(newEntity.getUserId());
        entity.setLiveUrl(newEntity.getLiveUrl());
        entity.setPlatformId(newEntity.getPlatformId());
        entity.setPlatformName(newEntity.getPlatformName());
        entity.setAnchorName(newEntity.getAnchorName());
        entity.setIsLive(newEntity.getIsLive());
        entity.setTitle(newEntity.getTitle());
        entity.setQuality(newEntity.getQuality());
        entity.setM3u8Url(newEntity.getM3u8Url());
        entity.setFlvUrl(newEntity.getFlvUrl());
        entity.setRecordUrl(newEntity.getRecordUrl());
        entity.setCookies(newEntity.getCookies());
        entity.setToken(newEntity.getToken());
        entity.setExtraData(newEntity.getExtraData());
        
        // 保存到数据库
        ProxyClientConfig savedEntity = proxyClientConfigRepository.save(entity);
        log.info("配置保存成功: id={}", savedEntity.getId());
        
        // 更新Redis缓存
        updateCache(savedEntity);
        
        return ProxyClientConfigDto.fromEntity(savedEntity);
    }

    @Override
    public ProxyClientConfigDto getConfigByUserId(String userId) {
        log.info("获取代理客户端配置: userId={}", userId);
        
        // 首先尝试从Redis缓存获取
        String cacheKey = CACHE_KEY_PREFIX + userId;
        ProxyClientConfigDto cachedConfig = (ProxyClientConfigDto) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedConfig != null) {
            log.info("从缓存获取配置成功: userId={}", userId);
            return cachedConfig;
        }
        
        // 从数据库获取
        Optional<ProxyClientConfig> entity = proxyClientConfigRepository.findByUserId(userId);
        
        if (entity.isPresent()) {
            ProxyClientConfigDto configDto = ProxyClientConfigDto.fromEntity(entity.get());
            // 缓存结果
            redisTemplate.opsForValue().set(cacheKey, configDto, CACHE_DURATION);
            log.info("从数据库获取配置成功并缓存: userId={}", userId);
            return configDto;
        } else {
            log.warn("未找到配置: userId={}", userId);
            return null;
        }
    }

    @Override
    @Transactional
    public void deleteConfig(Long id) {
        log.info("删除代理客户端配置: id={}", id);
        
        Optional<ProxyClientConfig> entity = proxyClientConfigRepository.findById(id);
        if (entity.isPresent()) {
            String userId = entity.get().getUserId();
            proxyClientConfigRepository.deleteById(id);
            // 清除Redis缓存
            clearCache(userId);
            log.info("配置删除成功: id={}, userId={}", id, userId);
        } else {
            log.warn("未找到要删除的配置: id={}", id);
        }
    }

    @Override
    @Transactional
    public void deleteConfigByUserId(String userId) {
        log.info("根据用户ID删除代理客户端配置: userId={}", userId);
        
        Optional<ProxyClientConfig> entity = proxyClientConfigRepository.findByUserId(userId);
        if (entity.isPresent()) {
            proxyClientConfigRepository.deleteByUserId(userId);
            // 清除Redis缓存
            clearCache(userId);
            log.info("配置删除成功: userId={}", userId);
        } else {
            log.warn("未找到要删除的配置: userId={}", userId);
        }
    }

    private void updateCache(ProxyClientConfig entity) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + entity.getUserId();
            ProxyClientConfigDto configDto = ProxyClientConfigDto.fromEntity(entity);
            redisTemplate.opsForValue().set(cacheKey, configDto, CACHE_DURATION);
            log.info("缓存更新成功: userId={}", entity.getUserId());
        } catch (Exception e) {
            log.error("缓存更新失败: userId={}", entity.getUserId(), e);
        }
    }

    private void clearCache(String userId) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.info("缓存清除成功: userId={}", userId);
        } catch (Exception e) {
            log.error("缓存清除失败: userId={}", userId, e);
        }
    }
}
