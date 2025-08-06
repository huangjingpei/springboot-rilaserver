package net.enjoy.springboot.registrationlogin.controller;

import net.enjoy.springboot.registrationlogin.dto.UpdateCheckResponse;
import net.enjoy.springboot.registrationlogin.dto.UpdatePackageDto;
import net.enjoy.springboot.registrationlogin.entity.UpdatePackage;
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

/**
 * 升级控制器
 * 提供软件升级相关的REST API
 */
@RestController
@RequestMapping("/api/v1/updates")
@Validated
public class UpdateController {
    
    private static final Logger logger = LoggerFactory.getLogger(UpdateController.class);
    
    @Autowired
    private UpdateService updateService;
    
    /**
     * 检查最新版本
     * GET /api/v1/updates/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<UpdateCheckResponse> checkLatestUpdate(
            @RequestParam @NotBlank(message = "当前版本不能为空") String currentVersion,
            @RequestParam @NotBlank(message = "平台信息不能为空") String platform) {
        
        logger.info("检查更新请求: version={}, platform={}", currentVersion, platform);
        
        UpdateCheckResponse response = updateService.checkForUpdate(currentVersion, platform);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取下载链接
     * GET /api/v1/updates/download/{version}
     */
    @GetMapping("/download/{version}")
    public ResponseEntity<Resource> downloadUpdate(
            @PathVariable @Pattern(regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+.*$", message = "版本号格式无效") String version,
            @RequestParam @NotBlank(message = "平台信息不能为空") String platform) {
        
        logger.info("下载请求: version={}, platform={}", version, platform);
        
        try {
            UpdatePackage updatePackage = updateService.getUpdatePackageByVersionAndPlatform(version, platform);
            
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
            @RequestParam String platform) {
        
        logger.info("直接下载文件: version={}, platform={}", version, platform);
        
        try {
            UpdatePackage updatePackage = updateService.getUpdatePackageByVersionAndPlatform(version, platform);
            
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
     * 根据平台获取升级包
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
            @RequestParam("version") String version,
            @RequestParam("platform") String platform,
            @RequestParam(value = "releaseNotes", required = false) String releaseNotes,
            @RequestParam(value = "isMandatory", defaultValue = "false") boolean isMandatory,
            @RequestParam(value = "description", required = false) String description) {
        
        logger.info("上传升级包: version={}, platform={}, fileName={}", version, platform, file.getOriginalFilename());
        
        UpdatePackageDto dto = new UpdatePackageDto();
        dto.setFile(file);
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
     * GET /api/v1/updates/mandatory/{platform}
     */
    @GetMapping("/mandatory/{platform}")
    public ResponseEntity<List<UpdatePackage>> getMandatoryUpdates(@PathVariable String platform) {
        List<UpdatePackage> mandatoryUpdates = updateService.getMandatoryUpdates(platform);
        return ResponseEntity.ok(mandatoryUpdates);
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