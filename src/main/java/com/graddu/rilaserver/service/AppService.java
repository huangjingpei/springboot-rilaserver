package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.AppDto;
import com.graddu.rilaserver.dto.AppSearchRequest;
import com.graddu.rilaserver.entity.App;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 应用服务接口
 */
public interface AppService {
    
    /**
     * 创建新应用
     */
    App createApp(App app);
    
    /**
     * 根据ID获取应用
     */
    Optional<App> getAppById(Long id);
    
    /**
     * 根据应用标识符获取应用
     */
    Optional<App> getAppByAppId(String appId);
    
    /**
     * 获取所有激活的应用
     */
    List<App> getAllActiveApps();
    
    /**
     * 获取所有公开的应用
     */
    List<App> getAllPublicApps();
    
    /**
     * 分页获取所有应用
     */
    Page<AppDto> getAllApps(int page, int size);
    
    /**
     * 分页获取激活的应用
     */
    Page<AppDto> getActiveApps(int page, int size);
    
    /**
     * 根据分类获取应用
     */
    List<App> getAppsByCategory(String category);
    
    /**
     * 根据标签搜索应用
     */
    List<App> searchAppsByTag(String tag);
    
    /**
     * 根据关键词搜索应用
     */
    Page<App> searchApps(String keyword, Pageable pageable);
    
    /**
     * 更新应用信息
     */
    App updateApp(Long id, App app);
    
    /**
     * 删除应用
     */
    boolean deleteApp(Long id);
    
    /**
     * 激活/停用应用
     */
    App setAppActive(Long id, boolean isActive);
    
    /**
     * 设置应用公开状态
     */
    App setAppPublic(Long id, boolean isPublic);
    
    /**
     * 设置应用强制更新状态
     */
    App setAppMandatoryUpdate(Long id, boolean isMandatoryUpdate);
    
    /**
     * 获取所有应用分类
     */
    List<String> getAllCategories();
    
    /**
     * 获取所有应用标签
     */
    List<String> getAllTags();
    
    /**
     * 检查应用标识符是否存在
     */
    boolean existsByAppId(String appId);
    
    /**
     * 根据开发者获取应用
     */
    List<App> getAppsByDeveloper(String developer);
    
    /**
     * 获取需要强制更新的应用
     */
    List<App> getMandatoryUpdateApps();
    
    /**
     * 更新应用当前版本
     */
    App updateAppVersion(Long id, String version);
    
    /**
     * 更新应用推荐版本
     */
    App updateRecommendedVersion(Long id, String version);
    
    /**
     * 更新应用最低版本
     */
    App updateMinVersion(Long id, String version);
    
    /**
     * 根据应用类型获取应用
     */
    Page<AppDto> getAppsByType(App.AppType type, int page, int size);
    
    /**
     * 根据平台获取应用
     */
    Page<AppDto> getAppsByPlatform(App.Platform platform, int page, int size);
    
    /**
     * 获取免费应用
     */
    Page<AppDto> getFreeApps(int page, int size);
    
    /**
     * 根据开发者获取应用（分页）
     */
    Page<AppDto> getAppsByDeveloper(String developer, int page, int size);
    
    /**
     * 获取所有应用类型
     */
    List<App.AppType> getAllAppTypes();
    
    /**
     * 获取所有平台
     */
    List<App.Platform> getAllPlatforms();
    
    /**
     * 获取应用数量
     */
    Long getAppCount();
    
    /**
     * 获取免费应用数量
     */
    Long getFreeAppCount();
    
    /**
     * 获取推荐应用数量
     */
    Long getFeaturedAppCount();
    
    /**
     * 获取总下载次数
     */
    Long getTotalDownloads();
    
    /**
     * 获取下载量前N的应用
     */
    List<AppDto> getTopDownloads(int limit);
    
    /**
     * 根据ID获取应用DTO
     */
    AppDto getAppDtoById(Long id);
    
    /**
     * 搜索应用
     */
    Page<AppDto> searchApps(AppSearchRequest request);
    
    /**
     * 获取推荐应用
     */
    Page<AppDto> getFeaturedApps(int page, int size);
    
    /**
     * 获取最新应用
     */
    Page<AppDto> getLatestApps(int page, int size);
    
    /**
     * 获取热门应用
     */
    Page<AppDto> getPopularApps(int page, int size);
    
    /**
     * 获取高评分应用
     */
    Page<AppDto> getTopRatedApps(int page, int size);
    
    /**
     * 创建应用
     */
    AppDto createApp(AppDto appDto);
    
    /**
     * 更新应用
     */
    AppDto updateApp(Long id, AppDto appDto);
    
    /**
     * 设置应用推荐状态
     */
    void setAppFeatured(Long id, boolean featured);
    
    /**
     * 设置应用激活状态（返回void）
     */
    void setAppActiveStatus(Long id, boolean active);
    
    /**
     * 增加下载次数
     */
    void incrementDownloadCount(Long id, App.Platform platform);
} 