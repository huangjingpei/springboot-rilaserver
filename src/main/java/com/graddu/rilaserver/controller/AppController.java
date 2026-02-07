package net.enjoy.springboot.registrationlogin.controller;

import net.enjoy.springboot.registrationlogin.dto.AppDto;
import net.enjoy.springboot.registrationlogin.dto.AppSearchRequest;
import net.enjoy.springboot.registrationlogin.entity.App;
import net.enjoy.springboot.registrationlogin.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apps")
@CrossOrigin(origins = "*")
public class AppController {

    @Autowired
    private AppService appService;

    // 获取应用详情
    @GetMapping("/{id}")
    public ResponseEntity<AppDto> getAppById(@PathVariable Long id) {
        try {
            AppDto app = appService.getAppDtoById(id);
            return ResponseEntity.ok(app);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 搜索应用
    @PostMapping("/search")
    public ResponseEntity<Page<AppDto>> searchApps(@RequestBody AppSearchRequest request) {
        Page<AppDto> apps = appService.searchApps(request);
        return ResponseEntity.ok(apps);
    }

    // 获取推荐应用
    @GetMapping("/featured")
    public ResponseEntity<Page<AppDto>> getFeaturedApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AppDto> apps = appService.getFeaturedApps(page, size);
        return ResponseEntity.ok(apps);
    }

    // 获取最新应用
    @GetMapping("/latest")
    public ResponseEntity<Page<AppDto>> getLatestApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AppDto> apps = appService.getLatestApps(page, size);
        return ResponseEntity.ok(apps);
    }

    // 获取热门应用
    @GetMapping("/popular")
    public ResponseEntity<Page<AppDto>> getPopularApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AppDto> apps = appService.getPopularApps(page, size);
        return ResponseEntity.ok(apps);
    }

    // 获取高评分应用
    @GetMapping("/top-rated")
    public ResponseEntity<Page<AppDto>> getTopRatedApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AppDto> apps = appService.getTopRatedApps(page, size);
        return ResponseEntity.ok(apps);
    }

    // 根据类型获取应用
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<AppDto>> getAppsByType(
            @PathVariable App.AppType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AppDto> apps = appService.getAppsByType(type, page, size);
        return ResponseEntity.ok(apps);
    }

    // 根据平台获取应用
    @GetMapping("/platform/{platform}")
    public ResponseEntity<Page<AppDto>> getAppsByPlatform(
            @PathVariable App.Platform platform,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AppDto> apps = appService.getAppsByPlatform(platform, page, size);
        return ResponseEntity.ok(apps);
    }

    // 获取免费应用
    @GetMapping("/free")
    public ResponseEntity<Page<AppDto>> getFreeApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AppDto> apps = appService.getFreeApps(page, size);
        return ResponseEntity.ok(apps);
    }

    // 根据开发者获取应用
    @GetMapping("/developer/{developer}")
    public ResponseEntity<Page<AppDto>> getAppsByDeveloper(
            @PathVariable String developer,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AppDto> apps = appService.getAppsByDeveloper(developer, page, size);
        return ResponseEntity.ok(apps);
    }

    // 获取所有应用类型
    @GetMapping("/types")
    public ResponseEntity<List<App.AppType>> getAllAppTypes() {
        List<App.AppType> types = appService.getAllAppTypes();
        return ResponseEntity.ok(types);
    }

    // 获取所有平台
    @GetMapping("/platforms")
    public ResponseEntity<List<App.Platform>> getAllPlatforms() {
        List<App.Platform> platforms = appService.getAllPlatforms();
        return ResponseEntity.ok(platforms);
    }

    // 获取应用数量
    @GetMapping("/count")
    public ResponseEntity<Long> getAppCount() {
        Long count = appService.getAppCount();
        return ResponseEntity.ok(count);
    }

    // 获取免费应用数量
    @GetMapping("/count/free")
    public ResponseEntity<Long> getFreeAppCount() {
        Long count = appService.getFreeAppCount();
        return ResponseEntity.ok(count);
    }

    // 获取推荐应用数量
    @GetMapping("/count/featured")
    public ResponseEntity<Long> getFeaturedAppCount() {
        Long count = appService.getFeaturedAppCount();
        return ResponseEntity.ok(count);
    }

    // 获取总下载次数
    @GetMapping("/count/downloads")
    public ResponseEntity<Long> getTotalDownloads() {
        Long count = appService.getTotalDownloads();
        return ResponseEntity.ok(count);
    }

    // 获取下载量前10的应用（用于图表显示）
    @GetMapping("/top-downloads")
    public ResponseEntity<List<AppDto>> getTopDownloads() {
        List<AppDto> apps = appService.getTopDownloads(10);
        return ResponseEntity.ok(apps);
    }

    // 获取所有应用（用于总应用数点击）
    @GetMapping("/all")
    public ResponseEntity<Page<AppDto>> getAllApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AppDto> apps = appService.getAllApps(page, size);
        return ResponseEntity.ok(apps);
    }

    // 创建应用（管理员功能）
    @PostMapping
    public ResponseEntity<AppDto> createApp(@RequestBody AppDto appDto) {
        try {
            AppDto createdApp = appService.createApp(appDto);
            return ResponseEntity.ok(createdApp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 更新应用（管理员功能）
    @PutMapping("/{id}")
    public ResponseEntity<AppDto> updateApp(@PathVariable Long id, @RequestBody AppDto appDto) {
        try {
            AppDto updatedApp = appService.updateApp(id, appDto);
            return ResponseEntity.ok(updatedApp);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 删除应用（管理员功能）
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApp(@PathVariable Long id) {
        try {
            appService.deleteApp(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 设置应用为推荐（管理员功能）
    @PutMapping("/{id}/featured")
    public ResponseEntity<Void> setAppFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        try {
            appService.setAppFeatured(id, featured);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 设置应用状态（管理员功能）
    @PutMapping("/{id}/active")
    public ResponseEntity<Void> setAppActive(@PathVariable Long id, @RequestParam boolean active) {
        try {
            appService.setAppActiveStatus(id, active);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 增加下载次数
    @PostMapping("/{id}/download")
    public ResponseEntity<Void> incrementDownloadCount(
            @PathVariable Long id, 
            @RequestParam App.Platform platform) {
        try {
            appService.incrementDownloadCount(id, platform);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 简单搜索（GET请求）
    @GetMapping("/search-simple")
    public ResponseEntity<Page<AppDto>> searchAppsGet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) App.AppType type,
            @RequestParam(required = false) App.Platform platform,
            @RequestParam(required = false) Boolean isFree,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        AppSearchRequest request = new AppSearchRequest();
        request.setKeyword(keyword);
        if (type != null) {
            request.setTypes(List.of(type));
        }
        if (platform != null) {
            request.setPlatforms(List.of(platform));
        }
        request.setIsFree(isFree);
        request.setIsFeatured(isFeatured);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortOrder(sortOrder);
        
        Page<AppDto> apps = appService.searchApps(request);
        return ResponseEntity.ok(apps);
    }
}