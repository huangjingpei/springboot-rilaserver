package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.AppDto;
import com.graddu.rilaserver.dto.AppSearchRequest;
import com.graddu.rilaserver.entity.App;
import com.graddu.rilaserver.repository.AppRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 应用服务实现类
 */
@Service
@Transactional
public class AppServiceImpl implements AppService {
    
    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);
    
    @Autowired
    private AppRepository appRepository;
    
    @Override
    public App createApp(App app) {
        logger.info("创建应用: appId={}, name={}", app.getAppId(), app.getName());
        
        // 验证应用标识符唯一性
        if (appRepository.existsByAppId(app.getAppId())) {
            throw new IllegalArgumentException("应用标识符已存在: " + app.getAppId());
        }
        
        // 设置默认值
        if (app.getIsActive() == null) {
            app.setIsActive(true);
        }
        if (app.getIsPublic() == null) {
            app.setIsPublic(true);
        }
        if (app.getIsMandatoryUpdate() == null) {
            app.setIsMandatoryUpdate(false);
        }
        
        App savedApp = appRepository.save(app);
        logger.info("应用创建成功: id={}, appId={}", savedApp.getId(), savedApp.getAppId());
        
        return savedApp;
    }
    
    @Override
    public Optional<App> getAppById(Long id) {
        return appRepository.findById(id);
    }
    
    @Override
    public Optional<App> getAppByAppId(String appId) {
        return appRepository.findByAppId(appId);
    }
    
    @Override
    public List<App> getAllActiveApps() {
        return appRepository.findByIsActiveTrue();
    }
    
    @Override
    public List<App> getAllPublicApps() {
        return appRepository.findByIsPublicTrue();
    }
    
    @Override
    public Page<AppDto> getAllApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findAll(pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public Page<AppDto> getActiveApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<App> apps = appRepository.findByIsActiveTrue(pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public List<App> getAppsByCategory(String category) {
        if (!StringUtils.hasText(category)) {
            throw new IllegalArgumentException("分类不能为空");
        }
        return appRepository.findByCategoryAndIsActiveTrue(category);
    }
    
    @Override
    public List<App> searchAppsByTag(String tag) {
        if (!StringUtils.hasText(tag)) {
            throw new IllegalArgumentException("标签不能为空");
        }
        return appRepository.findByTagContaining(tag);
    }
    
    @Override
    public Page<App> searchApps(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        return appRepository.searchApps(keyword, pageable);
    }
    
    @Override
    public App updateApp(Long id, App app) {
        logger.info("更新应用: id={}, appId={}", id, app.getAppId());
        
        Optional<App> existingAppOpt = appRepository.findById(id);
        if (existingAppOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App existingApp = existingAppOpt.get();
        
        // 检查应用标识符唯一性（如果修改了）
        if (!existingApp.getAppId().equals(app.getAppId()) && 
            appRepository.existsByAppId(app.getAppId())) {
            throw new IllegalArgumentException("应用标识符已存在: " + app.getAppId());
        }
        
        // 更新字段
        existingApp.setName(app.getName());
        existingApp.setDescription(app.getDescription());
        existingApp.setCurrentVersion(app.getCurrentVersion());
        existingApp.setMinVersion(app.getMinVersion());
        existingApp.setRecommendedVersion(app.getRecommendedVersion());
        existingApp.setIconUrl(app.getIconUrl());
        existingApp.setWebsiteUrl(app.getWebsiteUrl());
        existingApp.setDeveloper(app.getDeveloper());
        existingApp.setDeveloperEmail(app.getDeveloperEmail());
        existingApp.setLicense(app.getLicense());
        existingApp.setFeatures(app.getFeatures());
        existingApp.setChangelog(app.getChangelog());
        existingApp.setCategory(app.getCategory());
        existingApp.setTags(app.getTags());
        existingApp.setUpdatedAt(Instant.now());
        
        App updatedApp = appRepository.save(existingApp);
        logger.info("应用更新成功: id={}, appId={}", updatedApp.getId(), updatedApp.getAppId());
        
        return updatedApp;
    }
    
    @Override
    public boolean deleteApp(Long id) {
        logger.info("删除应用: id={}", id);
        
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            logger.warn("应用不存在: id={}", id);
            return false;
        }
        
        appRepository.deleteById(id);
        logger.info("应用删除成功: id={}", id);
        
        return true;
    }
    
    @Override
    public App setAppActive(Long id, boolean isActive) {
        logger.info("设置应用激活状态: id={}, isActive={}", id, isActive);
        
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App app = appOpt.get();
        app.setIsActive(isActive);
        app.setUpdatedAt(Instant.now());
        
        App updatedApp = appRepository.save(app);
        logger.info("应用激活状态设置成功: id={}, isActive={}", updatedApp.getId(), updatedApp.getIsActive());
        
        return updatedApp;
    }
    
    @Override
    public App setAppPublic(Long id, boolean isPublic) {
        logger.info("设置应用公开状态: id={}, isPublic={}", id, isPublic);
        
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App app = appOpt.get();
        app.setIsPublic(isPublic);
        app.setUpdatedAt(Instant.now());
        
        App updatedApp = appRepository.save(app);
        logger.info("应用公开状态设置成功: id={}, isPublic={}", updatedApp.getId(), updatedApp.getIsPublic());
        
        return updatedApp;
    }
    
    @Override
    public App setAppMandatoryUpdate(Long id, boolean isMandatoryUpdate) {
        logger.info("设置应用强制更新状态: id={}, isMandatoryUpdate={}", id, isMandatoryUpdate);
        
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App app = appOpt.get();
        app.setIsMandatoryUpdate(isMandatoryUpdate);
        app.setUpdatedAt(Instant.now());
        
        App updatedApp = appRepository.save(app);
        logger.info("应用强制更新状态设置成功: id={}, isMandatoryUpdate={}", updatedApp.getId(), updatedApp.getIsMandatoryUpdate());
        
        return updatedApp;
    }
    
    @Override
    public List<String> getAllCategories() {
        return appRepository.findAllCategories();
    }
    
    @Override
    public List<String> getAllTags() {
        return appRepository.findAllTags();
    }
    
    @Override
    public boolean existsByAppId(String appId) {
        return appRepository.existsByAppId(appId);
    }
    
    @Override
    public List<App> getAppsByDeveloper(String developer) {
        if (!StringUtils.hasText(developer)) {
            throw new IllegalArgumentException("开发者名称不能为空");
        }
        return appRepository.findByDeveloperAndIsActiveTrue(developer);
    }
    
    @Override
    public List<App> getMandatoryUpdateApps() {
        return appRepository.findByIsMandatoryUpdateTrueAndIsActiveTrue();
    }
    
    @Override
    public App updateAppVersion(Long id, String version) {
        logger.info("更新应用版本: id={}, version={}", id, version);
        
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App app = appOpt.get();
        app.setCurrentVersion(version);
        app.setLastUpdateDate(Instant.now());
        app.setUpdatedAt(Instant.now());
        
        App updatedApp = appRepository.save(app);
        logger.info("应用版本更新成功: id={}, version={}", updatedApp.getId(), updatedApp.getCurrentVersion());
        
        return updatedApp;
    }
    
    @Override
    public App updateRecommendedVersion(Long id, String version) {
        logger.info("更新应用推荐版本: id={}, version={}", id, version);
        
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App app = appOpt.get();
        app.setRecommendedVersion(version);
        app.setUpdatedAt(Instant.now());
        
        App updatedApp = appRepository.save(app);
        logger.info("应用推荐版本更新成功: id={}, version={}", updatedApp.getId(), updatedApp.getRecommendedVersion());
        
        return updatedApp;
    }
    
    @Override
    public App updateMinVersion(Long id, String version) {
        logger.info("更新应用最低版本: id={}, version={}", id, version);
        
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App app = appOpt.get();
        app.setMinVersion(version);
        app.setUpdatedAt(Instant.now());
        
        App updatedApp = appRepository.save(app);
        logger.info("应用最低版本更新成功: id={}, version={}", updatedApp.getId(), updatedApp.getMinVersion());
        
        return updatedApp;
    }
    
    @Override
    public Page<AppDto> getAppsByType(App.AppType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findByAppTypeAndIsActiveTrue(type, pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public Page<AppDto> getAppsByPlatform(App.Platform platform, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findByPlatformAndIsActiveTrue(platform, pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public Page<AppDto> getFreeApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findByIsFreeTrueAndIsActiveTrue(pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public Page<AppDto> getAppsByDeveloper(String developer, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findByDeveloperAndIsActiveTrue(developer, pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public List<App.AppType> getAllAppTypes() {
        return appRepository.findAllAppTypes();
    }
    
    @Override
    public List<App.Platform> getAllPlatforms() {
        return appRepository.findAllPlatforms();
    }
    
    @Override
    public Long getAppCount() {
        return appRepository.countActiveApps();
    }
    
    @Override
    public Long getFreeAppCount() {
        return appRepository.countFreeApps();
    }
    
    @Override
    public Long getFeaturedAppCount() {
        return appRepository.countFeaturedApps();
    }
    
    @Override
    public Long getTotalDownloads() {
        return appRepository.getTotalDownloads();
    }
    
    @Override
    public List<AppDto> getTopDownloads(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<App> apps = appRepository.findTopDownloads(pageable);
        return apps.stream().map(AppDto::new).toList();
    }
    
    @Override
    public AppDto getAppDtoById(Long id) {
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        return new AppDto(appOpt.get());
    }
    
    @Override
    public Page<AppDto> searchApps(AppSearchRequest request) {
        // 简化实现，实际应该根据请求参数进行复杂查询
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<App> apps = appRepository.findAll(pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public Page<AppDto> getFeaturedApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findByIsFeaturedTrueAndIsActiveTrue(pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public Page<AppDto> getLatestApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findAllByOrderByCreatedAtDesc(pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public Page<AppDto> getPopularApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findAllByOrderByDownloadCountDesc(pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public Page<AppDto> getTopRatedApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findAllByOrderByRatingDesc(pageable);
        return apps.map(AppDto::new);
    }
    
    @Override
    public AppDto createApp(AppDto appDto) {
        App app = new App();
        app.setAppId(appDto.getAppId());
        app.setName(appDto.getName());
        app.setDescription(appDto.getDescription());
        app.setCurrentVersion(appDto.getCurrentVersion());
        app.setMinVersion(appDto.getMinVersion());
        app.setRecommendedVersion(appDto.getRecommendedVersion());
        app.setIconUrl(appDto.getIconUrl());
        app.setWebsiteUrl(appDto.getWebsiteUrl());
        app.setDeveloper(appDto.getDeveloper());
        app.setDeveloperEmail(appDto.getDeveloperEmail());
        app.setLicense(appDto.getLicense());
        app.setFeatures(appDto.getFeatures());
        app.setChangelog(appDto.getChangelog());
        app.setIsActive(appDto.getIsActive());
        app.setIsPublic(appDto.getIsPublic());
        app.setIsMandatoryUpdate(appDto.getIsMandatoryUpdate());
        app.setCategory(appDto.getCategory());
        app.setTags(appDto.getTags());
        app.setAppType(appDto.getAppType());
        app.setPlatform(appDto.getPlatform());
        app.setIsFree(appDto.getIsFree());
        app.setIsFeatured(appDto.getIsFeatured());
        app.setDownloadCount(appDto.getDownloadCount());
        app.setRating(appDto.getRating());
        app.setRatingCount(appDto.getRatingCount());
        
        App savedApp = appRepository.save(app);
        return new AppDto(savedApp);
    }
    
    @Override
    public AppDto updateApp(Long id, AppDto appDto) {
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App app = appOpt.get();
        app.setAppId(appDto.getAppId());
        app.setName(appDto.getName());
        app.setDescription(appDto.getDescription());
        app.setCurrentVersion(appDto.getCurrentVersion());
        app.setMinVersion(appDto.getMinVersion());
        app.setRecommendedVersion(appDto.getRecommendedVersion());
        app.setIconUrl(appDto.getIconUrl());
        app.setWebsiteUrl(appDto.getWebsiteUrl());
        app.setDeveloper(appDto.getDeveloper());
        app.setDeveloperEmail(appDto.getDeveloperEmail());
        app.setLicense(appDto.getLicense());
        app.setFeatures(appDto.getFeatures());
        app.setChangelog(appDto.getChangelog());
        app.setIsActive(appDto.getIsActive());
        app.setIsPublic(appDto.getIsPublic());
        app.setIsMandatoryUpdate(appDto.getIsMandatoryUpdate());
        app.setCategory(appDto.getCategory());
        app.setTags(appDto.getTags());
        app.setAppType(appDto.getAppType());
        app.setPlatform(appDto.getPlatform());
        app.setIsFree(appDto.getIsFree());
        app.setIsFeatured(appDto.getIsFeatured());
        app.setDownloadCount(appDto.getDownloadCount());
        app.setRating(appDto.getRating());
        app.setRatingCount(appDto.getRatingCount());
        app.setUpdatedAt(Instant.now());
        
        App savedApp = appRepository.save(app);
        return new AppDto(savedApp);
    }
    
    @Override
    public void setAppFeatured(Long id, boolean featured) {
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App app = appOpt.get();
        app.setIsFeatured(featured);
        app.setUpdatedAt(Instant.now());
        appRepository.save(app);
    }
    
    @Override
    public void setAppActiveStatus(Long id, boolean active) {
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App app = appOpt.get();
        app.setIsActive(active);
        app.setUpdatedAt(Instant.now());
        appRepository.save(app);
    }
    
    @Override
    public void incrementDownloadCount(Long id, App.Platform platform) {
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isEmpty()) {
            throw new IllegalArgumentException("应用不存在: " + id);
        }
        
        App app = appOpt.get();
        app.setDownloadCount(app.getDownloadCount() + 1);
        app.setUpdatedAt(Instant.now());
        appRepository.save(app);
    }
} 