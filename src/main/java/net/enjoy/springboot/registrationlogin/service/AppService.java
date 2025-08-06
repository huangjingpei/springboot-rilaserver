package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.dto.AppDto;
import net.enjoy.springboot.registrationlogin.dto.AppSearchRequest;
import net.enjoy.springboot.registrationlogin.entity.App;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AppService {

    // 获取应用详情
    AppDto getAppById(Long id);

    // 搜索应用
    Page<AppDto> searchApps(AppSearchRequest request);

    // 获取推荐应用
    Page<AppDto> getFeaturedApps(int page, int size);

    // 获取最新应用
    Page<AppDto> getLatestApps(int page, int size);

    // 获取热门应用（按下载量）
    Page<AppDto> getPopularApps(int page, int size);

    // 获取高评分应用
    Page<AppDto> getTopRatedApps(int page, int size);

    // 根据类型获取应用
    Page<AppDto> getAppsByType(App.AppType type, int page, int size);

    // 根据平台获取应用
    Page<AppDto> getAppsByPlatform(App.Platform platform, int page, int size);

    // 获取免费应用
    Page<AppDto> getFreeApps(int page, int size);

    // 根据开发者获取应用
    Page<AppDto> getAppsByDeveloper(String developer, int page, int size);

    // 获取所有应用类型
    List<App.AppType> getAllAppTypes();

    // 获取所有平台
    List<App.Platform> getAllPlatforms();

    // 统计应用数量
    Long getAppCount();

    // 统计免费应用数量
    Long getFreeAppCount();

    // 统计推荐应用数量
    Long getFeaturedAppCount();

    // 统计总下载次数
    Long getTotalDownloads();

    // 获取下载量前N的应用
    List<AppDto> getTopDownloads(int limit);

    // 获取所有应用
    Page<AppDto> getAllApps(int page, int size);

    // 创建应用
    AppDto createApp(AppDto appDto);

    // 更新应用
    AppDto updateApp(Long id, AppDto appDto);

    // 删除应用
    void deleteApp(Long id);

    // 设置应用为推荐
    void setAppFeatured(Long id, boolean featured);

    // 设置应用状态
    void setAppActive(Long id, boolean active);

    // 增加下载次数
    void incrementDownloadCount(Long appId, App.Platform platform);
} 