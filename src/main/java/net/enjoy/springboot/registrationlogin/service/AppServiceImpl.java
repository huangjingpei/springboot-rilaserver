package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.dto.AppDto;
import net.enjoy.springboot.registrationlogin.dto.AppDownloadDto;
import net.enjoy.springboot.registrationlogin.dto.AppReviewDto;
import net.enjoy.springboot.registrationlogin.dto.AppSearchRequest;
import net.enjoy.springboot.registrationlogin.entity.App;
import net.enjoy.springboot.registrationlogin.entity.AppDownload;
import net.enjoy.springboot.registrationlogin.entity.AppReview;
import net.enjoy.springboot.registrationlogin.repository.AppDownloadRepository;
import net.enjoy.springboot.registrationlogin.repository.AppRepository;
import net.enjoy.springboot.registrationlogin.repository.AppReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppServiceImpl implements AppService {

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppDownloadRepository appDownloadRepository;

    @Autowired
    private AppReviewRepository appReviewRepository;

    @Override
    public AppDto getAppById(Long id) {
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isPresent()) {
            App app = appOpt.get();
            AppDto appDto = new AppDto(app);
            
            // 获取下载链接
            List<AppDownload> downloads = appDownloadRepository.findByAppIdAndIsActiveTrue(id);
            List<AppDownloadDto> downloadDtos = downloads.stream()
                    .map(AppDownloadDto::new)
                    .collect(Collectors.toList());
            appDto.setDownloads(downloadDtos);
            
            // 获取评论（只获取前10条）
            Page<AppReview> reviews = appReviewRepository.findByAppIdOrderByCreatedAtDesc(id, 
                    PageRequest.of(0, 10));
            List<AppReviewDto> reviewDtos = reviews.getContent().stream()
                    .map(AppReviewDto::new)
                    .collect(Collectors.toList());
            appDto.setReviews(reviewDtos);
            
            return appDto;
        }
        throw new RuntimeException("应用不存在");
    }

    @Override
    public Page<AppDto> searchApps(AppSearchRequest request) {
        // 构建排序
        Sort sort = buildSort(request.getSortBy(), request.getSortOrder());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // 根据筛选条件选择不同的查询方法
        Page<App> apps;
        
        if (request.getType() != null) {
            // 按类型筛选
            apps = appRepository.findByTypeAndIsActiveTrue(request.getType(), pageable);
        } else if (request.getPlatform() != null) {
            // 按平台筛选 - 暂时返回空结果
            apps = Page.empty(pageable);
        } else if (request.getIsFree() != null && request.getIsFree()) {
            // 免费应用
            apps = appRepository.findByPriceAndIsActiveTrue(BigDecimal.ZERO, pageable);
        } else if (request.getIsFeatured() != null && request.getIsFeatured()) {
            // 推荐应用
            apps = appRepository.findByIsFeaturedTrueAndIsActiveTrue(pageable);
        } else if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            // 关键词搜索
            apps = appRepository.findByKeyword(request.getKeyword().trim(), pageable);
        } else {
            // 默认查询所有活跃应用
            apps = appRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        }
        
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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<App> apps = appRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        return apps.map(AppDto::new);
    }

    @Override
    public Page<AppDto> getPopularApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findByIsActiveTrueOrderByDownloadCountDesc(pageable);
        return apps.map(AppDto::new);
    }

    @Override
    public Page<AppDto> getTopRatedApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findByIsActiveTrueOrderByRatingDesc(pageable);
        return apps.map(AppDto::new);
    }

    @Override
    public Page<AppDto> getAppsByType(App.AppType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findByTypeAndIsActiveTrue(type, pageable);
        return apps.map(AppDto::new);
    }

    @Override
    public Page<AppDto> getAppsByPlatform(App.Platform platform, int page, int size) {
        // 暂时返回空结果，避免复杂的查询
        Pageable pageable = PageRequest.of(page, size);
        return Page.empty(pageable);
    }

    @Override
    public Page<AppDto> getFreeApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<App> apps = appRepository.findByPriceAndIsActiveTrue(BigDecimal.ZERO, pageable);
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
        return appRepository.findAllActiveTypes();
    }

    @Override
    public List<App.Platform> getAllPlatforms() {
        // 暂时返回空列表，避免复杂的查询
        return List.of();
    }

    @Override
    public Long getAppCount() {
        return appRepository.countActiveApps();
    }

    @Override
    public Long getFreeAppCount() {
        return appRepository.countByPriceAndIsActiveTrue(BigDecimal.ZERO);
    }

    @Override
    public Long getFeaturedAppCount() {
        return appRepository.countByIsFeaturedTrueAndIsActiveTrue();
    }

    @Override
    public Long getTotalDownloads() {
        return appRepository.sumDownloadCountByIsActiveTrue();
    }

    @Override
    public List<AppDto> getTopDownloads(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "downloadCount"));
        Page<App> apps = appRepository.findByIsActiveTrueOrderByDownloadCountDesc(pageable);
        return apps.getContent().stream().map(AppDto::new).collect(Collectors.toList());
    }

    @Override
    public Page<AppDto> getAllApps(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<App> apps = appRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        return apps.map(AppDto::new);
    }

    @Override
    public AppDto createApp(AppDto appDto) {
        App app = new App();
        updateAppFromDto(app, appDto);
        App savedApp = appRepository.save(app);
        return new AppDto(savedApp);
    }

    @Override
    public AppDto updateApp(Long id, AppDto appDto) {
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isPresent()) {
            App app = appOpt.get();
            updateAppFromDto(app, appDto);
            App savedApp = appRepository.save(app);
            return new AppDto(savedApp);
        }
        throw new RuntimeException("应用不存在");
    }

    @Override
    public void deleteApp(Long id) {
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isPresent()) {
            App app = appOpt.get();
            app.setIsActive(false);
            appRepository.save(app);
        }
    }

    @Override
    public void setAppFeatured(Long id, boolean featured) {
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isPresent()) {
            App app = appOpt.get();
            app.setIsFeatured(featured);
            appRepository.save(app);
        }
    }

    @Override
    public void setAppActive(Long id, boolean active) {
        Optional<App> appOpt = appRepository.findById(id);
        if (appOpt.isPresent()) {
            App app = appOpt.get();
            app.setIsActive(active);
            appRepository.save(app);
        }
    }

    @Override
    public void incrementDownloadCount(Long appId, App.Platform platform) {
        Optional<AppDownload> downloadOpt = appDownloadRepository.findByAppIdAndPlatformAndIsActiveTrue(appId, platform);
        if (downloadOpt.isPresent()) {
            appDownloadRepository.incrementDownloadCount(downloadOpt.get().getId());
        }
        
        // 同时更新应用的总下载次数
        Optional<App> appOpt = appRepository.findById(appId);
        if (appOpt.isPresent()) {
            App app = appOpt.get();
            app.setDownloadCount(app.getDownloadCount() + 1);
            appRepository.save(app);
        }
    }

    // 私有辅助方法
    private Sort buildSort(String sortBy, String sortOrder) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        switch (sortBy.toLowerCase()) {
            case "rating":
                return Sort.by(direction, "rating");
            case "price":
                return Sort.by(direction, "price");
            case "downloadcount":
                return Sort.by(direction, "downloadCount");
            case "createdat":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    private void updateAppFromDto(App app, AppDto appDto) {
        app.setName(appDto.getName());
        app.setDescription(appDto.getDescription());
        app.setShortDescription(appDto.getShortDescription());
        app.setAppIcon(appDto.getAppIcon());
        app.setScreenshots(appDto.getScreenshots());
        app.setPrice(appDto.getPrice());
        app.setOriginalPrice(appDto.getOriginalPrice());
        app.setType(appDto.getType());
        app.setPlatforms(appDto.getPlatforms());
        app.setFileSize(appDto.getFileSize());
        app.setVersion(appDto.getVersion());
        app.setDeveloper(appDto.getDeveloper());
        app.setReleaseDate(appDto.getReleaseDate());
        app.setIsFeatured(appDto.getIsFeatured());
        app.setIsActive(appDto.getIsActive());
    }
} 