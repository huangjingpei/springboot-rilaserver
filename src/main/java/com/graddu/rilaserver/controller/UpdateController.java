package net.enjoy.springboot.registrationlogin.controller;

import net.enjoy.springboot.registrationlogin.dto.AppDto;
import net.enjoy.springboot.registrationlogin.dto.UpdateCheckResponse;
import net.enjoy.springboot.registrationlogin.dto.UpdatePackageDto;
import net.enjoy.springboot.registrationlogin.entity.App;
import net.enjoy.springboot.registrationlogin.entity.UpdatePackage;
import net.enjoy.springboot.registrationlogin.service.AppService;
import net.enjoy.springboot.registrationlogin.service.UpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 多应用升级控制器
 * 提供支持多个应用的通用升级系统REST API
 */
@RestController
@RequestMapping("/api/v1/updates")
@Validated
public class UpdateController {
    
    private static final Logger logger = LoggerFactory.getLogger(UpdateController.class);
    
    @Autowired
    private UpdateService updateService;
    
    @Autowired
    private AppService appService;
    
    /**
     * 检查应用最新版本
     * GET /api/v1/updates/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<UpdateCheckResponse> checkLatestUpdate(
            @RequestParam @NotBlank(message = "应用标识符不能为空") String appId,
            @RequestParam @NotBlank(message = "当前版本不能为空") String currentVersion,
            @RequestParam @NotBlank(message = "平台信息不能为空") String platform) {
        
        logger.info("检查应用更新请求: appId={}, version={}, platform={}", appId, currentVersion, platform);
        
        // 验证应用是否存在且激活
        Optional<App> appOpt = appService.getAppByAppId(appId);
        if (appOpt.isEmpty()) {
            logger.warn("应用不存在: appId={}", appId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new UpdateCheckResponse(false, "应用不存在: " + appId));
        }
        
        App app = appOpt.get();
        if (!app.getIsActive()) {
            logger.warn("应用已停用: appId={}", appId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new UpdateCheckResponse(false, "应用已停用: " + appId));
        }
        
        UpdateCheckResponse response = updateService.checkForUpdate(appId, currentVersion, platform);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取下载链接
     * GET /api/v1/updates/download/{version}
     */
    @GetMapping("/download/{version}")
    public ResponseEntity<Resource> downloadUpdate(
            @PathVariable @Pattern(regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+.*$", message = "版本号格式无效") String version,
            @RequestParam @NotBlank(message = "应用标识符不能为空") String appId,
            @RequestParam @NotBlank(message = "平台信息不能为空") String platform) {
        
        logger.info("下载请求: appId={}, version={}, platform={}", appId, version, platform);
        
        try {
            UpdatePackage updatePackage = updateService.getUpdatePackageByVersionAndAppIdAndPlatform(version, appId, platform);
            
            logger.info("找到升级包: ID={}, 文件URL={}", updatePackage.getId(), updatePackage.getFileUrl());
            
            if (!updatePackage.getIsActive()) {
                logger.warn("升级包已停用: {}", updatePackage.getId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
            // 从文件URL中提取本地路径
            String filePath = extractLocalPath(updatePackage.getFileUrl());
            logger.info("提取的文件路径: {}", filePath);
            
            Path path = Paths.get(filePath);
            logger.info("完整文件路径: {}", path.toAbsolutePath());
            
            Resource resource = new UrlResource(path.toUri());
            logger.info("资源是否存在: {}, 是否可读: {}", resource.exists(), resource.isReadable());
            
            if (resource.exists() && resource.isReadable()) {
                logger.info("文件下载成功: {}", updatePackage.getFileName());
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + updatePackage.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, updatePackage.getFileType())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(updatePackage.getFileSize()))
                    .body(resource);
            } else {
                logger.error("文件不存在或不可读: {}", path.toAbsolutePath());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
        } catch (Exception e) {
            logger.error("文件下载失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * 直接下载文件（用于本地文件存储）
     * GET /api/v1/updates/file/{version}
     */
    @GetMapping("/file/{version}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String version,
            @RequestParam @NotBlank(message = "应用标识符不能为空") String appId,
            @RequestParam String platform) {
        
        logger.info("直接下载文件: appId={}, version={}, platform={}", appId, version, platform);
        
        try {
            UpdatePackage updatePackage = updateService.getUpdatePackageByVersionAndAppIdAndPlatform(version, appId, platform);
            
            if (!updatePackage.getIsActive()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
            }
            
            // 从文件URL中提取本地路径
            String filePath = extractLocalPath(updatePackage.getFileUrl());
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + updatePackage.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, updatePackage.getFileType())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(updatePackage.getFileSize()))
                    .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
        } catch (Exception e) {
            logger.error("文件下载失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * 获取所有升级包（分页）
     * GET /api/v1/updates
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UpdatePackage>> getAllUpdatePackages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UpdatePackage> packages = updateService.getAllUpdatePackages(pageable);
        
        return ResponseEntity.ok(packages);
    }
    
    /**
     * 根据应用标识符和平台获取升级包
     * GET /api/v1/updates/app/{appId}/platform/{platform}
     */
    @GetMapping("/app/{appId}/platform/{platform}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UpdatePackage>> getUpdatePackagesByAppIdAndPlatform(
            @PathVariable String appId,
            @PathVariable String platform,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending());
        Page<UpdatePackage> packages = updateService.getUpdatePackagesByAppIdAndPlatform(appId, platform, pageable);
        
        return ResponseEntity.ok(packages);
    }
    
    /**
     * 根据平台获取升级包（兼容旧版本）
     * GET /api/v1/updates/platform/{platform}
     */
    @GetMapping("/platform/{platform}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UpdatePackage>> getUpdatePackagesByPlatform(
            @PathVariable String platform,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending());
        Page<UpdatePackage> packages = updateService.getUpdatePackagesByPlatform(platform, pageable);
        
        return ResponseEntity.ok(packages);
    }
    
    /**
     * 根据ID获取升级包
     * GET /api/v1/updates/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UpdatePackage> getUpdatePackageById(@PathVariable Long id) {
        UpdatePackage updatePackage = updateService.getUpdatePackageById(id);
        return ResponseEntity.ok(updatePackage);
    }
    
    /**
     * 上传新的升级包
     * POST /api/v1/updates
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UpdatePackage> uploadUpdatePackage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("appId") String appId,
            @RequestParam("version") String version,
            @RequestParam("platform") String platform,
            @RequestParam(value = "releaseNotes", required = false) String releaseNotes,
            @RequestParam(value = "isMandatory", defaultValue = "false") boolean isMandatory,
            @RequestParam(value = "description", required = false) String description) {
        
        logger.info("上传升级包: appId={}, version={}, platform={}, fileName={}", appId, version, platform, file.getOriginalFilename());
        
        UpdatePackageDto dto = new UpdatePackageDto();
        dto.setFile(file);
        dto.setAppId(appId);
        dto.setVersion(version);
        dto.setPlatform(platform);
        dto.setReleaseNotes(releaseNotes);
        dto.setIsMandatory(isMandatory);
        dto.setDescription(description);
        
        UpdatePackage savedPackage = updateService.uploadUpdatePackage(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPackage);
    }
    
    /**
     * 更新升级包信息
     * PUT /api/v1/updates/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UpdatePackage> updateUpdatePackage(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePackage updatePackage) {
        
        UpdatePackage updatedPackage = updateService.updateUpdatePackage(id, updatePackage);
        return ResponseEntity.ok(updatedPackage);
    }
    
    /**
     * 删除升级包
     * DELETE /api/v1/updates/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUpdatePackage(@PathVariable Long id) {
        boolean deleted = updateService.deleteUpdatePackage(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("message", deleted ? "升级包删除成功" : "升级包删除失败");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 激活/停用升级包
     * PATCH /api/v1/updates/{id}/active
     */
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UpdatePackage> setUpdatePackageActive(
            @PathVariable Long id,
            @RequestParam boolean isActive) {
        
        UpdatePackage updatePackage = updateService.setUpdatePackageActive(id, isActive);
        return ResponseEntity.ok(updatePackage);
    }
    
    /**
     * 获取所有支持的平台
     * GET /api/v1/updates/platforms
     */
    @GetMapping("/platforms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAllPlatforms() {
        List<String> platforms = updateService.getAllPlatforms();
        return ResponseEntity.ok(platforms);
    }
    
    /**
     * 获取强制更新的版本
     * GET /api/v1/updates/mandatory/{appId}/{platform}
     */
    @GetMapping("/mandatory/{appId}/{platform}")
    public ResponseEntity<List<UpdatePackage>> getMandatoryUpdates(
            @PathVariable String appId,
            @PathVariable String platform) {
        List<UpdatePackage> mandatoryUpdates = updateService.getMandatoryUpdates(appId, platform);
        return ResponseEntity.ok(mandatoryUpdates);
    }
    
    /**
     * 获取强制更新的版本（兼容旧版本）
     * GET /api/v1/updates/mandatory/{platform}
     */
    @GetMapping("/mandatory/{platform}")
    public ResponseEntity<List<UpdatePackage>> getMandatoryUpdatesByPlatform(@PathVariable String platform) {
        List<UpdatePackage> mandatoryUpdates = updateService.getMandatoryUpdates(platform);
        return ResponseEntity.ok(mandatoryUpdates);
    }
    
    // ==================== 应用管理API ====================
    
    /**
     * 获取所有应用
     * GET /api/v1/updates/apps
     */
    @GetMapping("/apps")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AppDto>> getAllApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<AppDto> apps = appService.getAllApps(page, size);
        
        return ResponseEntity.ok(apps);
    }
    
    /**
     * 获取激活的应用
     * GET /api/v1/updates/apps/active
     */
    @GetMapping("/apps/active")
    public ResponseEntity<Page<AppDto>> getActiveApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<AppDto> apps = appService.getActiveApps(page, size);
        
        return ResponseEntity.ok(apps);
    }
    
    /**
     * 根据ID获取应用
     * GET /api/v1/updates/apps/{id}
     */
    @GetMapping("/apps/{id}")
    public ResponseEntity<App> getAppById(@PathVariable Long id) {
        Optional<App> appOpt = appService.getAppById(id);
        if (appOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(appOpt.get());
    }
    
    /**
     * 根据应用标识符获取应用
     * GET /api/v1/updates/apps/app/{appId}
     */
    @GetMapping("/apps/app/{appId}")
    public ResponseEntity<App> getAppByAppId(@PathVariable String appId) {
        Optional<App> appOpt = appService.getAppByAppId(appId);
        if (appOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(appOpt.get());
    }
    
    /**
     * 创建新应用
     * POST /api/v1/updates/apps
     */
    @PostMapping("/apps")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<App> createApp(@Valid @RequestBody App app) {
        App createdApp = appService.createApp(app);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdApp);
    }
    
    /**
     * 更新应用
     * PUT /api/v1/updates/apps/{id}
     */
    @PutMapping("/apps/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<App> updateApp(@PathVariable Long id, @Valid @RequestBody App app) {
        App updatedApp = appService.updateApp(id, app);
        return ResponseEntity.ok(updatedApp);
    }
    
    /**
     * 删除应用
     * DELETE /api/v1/updates/apps/{id}
     */
    @DeleteMapping("/apps/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteApp(@PathVariable Long id) {
        boolean deleted = appService.deleteApp(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("message", deleted ? "应用删除成功" : "应用删除失败");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 设置应用激活状态
     * PATCH /api/v1/updates/apps/{id}/active
     */
    @PatchMapping("/apps/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<App> setAppActive(@PathVariable Long id, @RequestParam boolean isActive) {
        App app = appService.setAppActive(id, isActive);
        return ResponseEntity.ok(app);
    }
    
    /**
     * 设置应用公开状态
     * PATCH /api/v1/updates/apps/{id}/public
     */
    @PatchMapping("/apps/{id}/public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<App> setAppPublic(@PathVariable Long id, @RequestParam boolean isPublic) {
        App app = appService.setAppPublic(id, isPublic);
        return ResponseEntity.ok(app);
    }
    
    /**
     * 设置应用强制更新状态
     * PATCH /api/v1/updates/apps/{id}/mandatory-update
     */
    @PatchMapping("/apps/{id}/mandatory-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<App> setAppMandatoryUpdate(@PathVariable Long id, @RequestParam boolean isMandatoryUpdate) {
        App app = appService.setAppMandatoryUpdate(id, isMandatoryUpdate);
        return ResponseEntity.ok(app);
    }
    
    /**
     * 更新应用版本
     * PATCH /api/v1/updates/apps/{id}/version
     */
    @PatchMapping("/apps/{id}/version")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<App> updateAppVersion(@PathVariable Long id, @RequestParam String version) {
        App app = appService.updateAppVersion(id, version);
        return ResponseEntity.ok(app);
    }
    
    /**
     * 更新应用推荐版本
     * PATCH /api/v1/updates/apps/{id}/recommended-version
     */
    @PatchMapping("/apps/{id}/recommended-version")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<App> updateRecommendedVersion(@PathVariable Long id, @RequestParam String version) {
        App app = appService.updateRecommendedVersion(id, version);
        return ResponseEntity.ok(app);
    }
    
    /**
     * 更新应用最低版本
     * PATCH /api/v1/updates/apps/{id}/min-version
     */
    @PatchMapping("/apps/{id}/min-version")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<App> updateMinVersion(@PathVariable Long id, @RequestParam String version) {
        App app = appService.updateMinVersion(id, version);
        return ResponseEntity.ok(app);
    }
    
    /**
     * 根据分类获取应用
     * GET /api/v1/updates/apps/category/{category}
     */
    @GetMapping("/apps/category/{category}")
    public ResponseEntity<List<App>> getAppsByCategory(@PathVariable String category) {
        List<App> apps = appService.getAppsByCategory(category);
        return ResponseEntity.ok(apps);
    }
    
    /**
     * 根据标签搜索应用
     * GET /api/v1/updates/apps/tag/{tag}
     */
    @GetMapping("/apps/tag/{tag}")
    public ResponseEntity<List<App>> getAppsByTag(@PathVariable String tag) {
        List<App> apps = appService.searchAppsByTag(tag);
        return ResponseEntity.ok(apps);
    }
    
    /**
     * 搜索应用
     * GET /api/v1/updates/apps/search
     */
    @GetMapping("/apps/search")
    public ResponseEntity<Page<App>> searchApps(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<App> apps = appService.searchApps(keyword, pageable);
        
        return ResponseEntity.ok(apps);
    }
    
    /**
     * 获取所有应用分类
     * GET /api/v1/updates/apps/categories
     */
    @GetMapping("/apps/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = appService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * 获取所有应用标签
     * GET /api/v1/updates/apps/tags
     */
    @GetMapping("/apps/tags")
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = appService.getAllTags();
        return ResponseEntity.ok(tags);
    }
    
    /**
     * 根据开发者获取应用
     * GET /api/v1/updates/apps/developer/{developer}
     */
    @GetMapping("/apps/developer/{developer}")
    public ResponseEntity<List<App>> getAppsByDeveloper(@PathVariable String developer) {
        List<App> apps = appService.getAppsByDeveloper(developer);
        return ResponseEntity.ok(apps);
    }
    
    /**
     * 获取需要强制更新的应用
     * GET /api/v1/updates/apps/mandatory-update
     */
    @GetMapping("/apps/mandatory-update")
    public ResponseEntity<List<App>> getMandatoryUpdateApps() {
        List<App> apps = appService.getMandatoryUpdateApps();
        return ResponseEntity.ok(apps);
    }
    
    /**
     * 验证版本号格式
     * GET /api/v1/updates/validate-version
     */
    @GetMapping("/validate-version")
    public ResponseEntity<Map<String, Object>> validateVersion(@RequestParam String version) {
        boolean isValid = updateService.isValidVersion(version);
        
        Map<String, Object> response = new HashMap<>();
        response.put("version", version);
        response.put("isValid", isValid);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 比较版本号
     * GET /api/v1/updates/compare-versions
     */
    @GetMapping("/compare-versions")
    public ResponseEntity<Map<String, Object>> compareVersions(
            @RequestParam String version1,
            @RequestParam String version2) {
        
        try {
            int result = updateService.compareVersions(version1, version2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("version1", version1);
            response.put("version2", version2);
            response.put("result", result);
            response.put("description", getComparisonDescription(result));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 从文件URL中提取本地路径
     */
    private String extractLocalPath(String fileUrl) {
        logger.info("原始文件URL: {}", fileUrl);
        
        // 如果已经是相对路径，直接返回
        if (!fileUrl.startsWith("http")) {
            return fileUrl;
        }
        
        // 处理不同的URL格式
        if (fileUrl.contains("/files/")) {
            String path = fileUrl.substring(fileUrl.indexOf("/files/") + 7);
            // 将 updates 替换为 uploads/updates
            if (path.startsWith("updates/")) {
                path = "uploads/" + path;
            }
            logger.info("提取的路径: {}", path);
            return path;
        }
        
        // 如果是完整的HTTP URL，尝试提取路径部分
        try {
            java.net.URL url = new java.net.URL(fileUrl);
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            // 将 updates 替换为 uploads/updates
            if (path.startsWith("updates/")) {
                path = "uploads/" + path;
            }
            logger.info("从URL提取的路径: {}", path);
            return path;
        } catch (Exception e) {
            logger.warn("无法解析文件URL: {}", fileUrl);
            return fileUrl;
        }
    }
    
    /**
     * 获取版本比较的描述
     */
    private String getComparisonDescription(int result) {
        switch (result) {
            case -1: return "version1 < version2";
            case 0: return "version1 = version2";
            case 1: return "version1 > version2";
            default: return "unknown";
        }
    }
} 