package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.UpdateCheckResponse;
import com.graddu.rilaserver.dto.UpdatePackageDto;
import com.graddu.rilaserver.entity.App;
import com.graddu.rilaserver.entity.UpdatePackage;
import com.graddu.rilaserver.repository.UpdatePackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 升级服务实现类
 */
@Service
@Transactional
public class UpdateServiceImpl implements UpdateService {
    
    private static final Logger logger = LoggerFactory.getLogger(UpdateServiceImpl.class);
    
    // 语义化版本号正则表达式
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?$"
    );
    
    @Autowired
    private UpdatePackageRepository updatePackageRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private AppService appService;
    
    @Value("${app.update.download-url-prefix:/api/v1/updates/file}")
    private String downloadUrlPrefix;
    
    @Value("${app.update.hash-algorithm:SHA-256}")
    private String defaultHashAlgorithm;
    
    @Override
    public UpdateCheckResponse checkForUpdate(String appId, String currentVersion, String platform) {
        logger.info("检查更新: appId={}, 当前版本={}, 平台={}", appId, currentVersion, platform);
        
        // 验证参数
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(currentVersion) || !StringUtils.hasText(platform)) {
            logger.warn("参数无效: appId={}, currentVersion={}, platform={}", appId, currentVersion, platform);
            return new UpdateCheckResponse(false);
        }
        
        // 验证版本号格式
        if (!isValidVersion(currentVersion)) {
            logger.warn("当前版本号格式无效: {}", currentVersion);
            return new UpdateCheckResponse(false);
        }
        
        try {
            // 查找最新的激活版本（根据appId和platform）
            Optional<UpdatePackage> latestPackageOpt = updatePackageRepository
                .findTopByAppIdAndPlatformAndIsActiveTrueOrderByReleaseDateDesc(appId, platform);
            
            if (latestPackageOpt.isPresent()) {
                UpdatePackage latestPackage = latestPackageOpt.get();
                String latestVersion = latestPackage.getVersion();
                
                // 比较版本号
                int comparison = compareVersions(currentVersion, latestVersion);
                
                if (comparison < 0) {
                    // 有新版本可用
                    logger.info("发现新版本: {} -> {}", currentVersion, latestVersion);
                    return createUpdateResponse(latestPackage);
                } else {
                    logger.info("已是最新版本: {}", currentVersion);
                }
            } else {
                logger.info("未找到应用 {} 平台 {} 的升级包", appId, platform);
            }
            
        } catch (Exception e) {
            logger.error("检查更新失败", e);
        }
        
        return new UpdateCheckResponse(false);
    }
    
    @Override
    public String getDownloadUrl(String version, String platform) {
        logger.info("获取下载URL: 版本={}, 平台={}", version, platform);
        
        UpdatePackage updatePackage = updatePackageRepository
            .findByVersionAndPlatform(version, platform)
            .orElseThrow(() -> new RuntimeException("升级包不存在: " + version + " for " + platform));
        
        if (!updatePackage.getIsActive()) {
            throw new RuntimeException("升级包已停用: " + version);
        }
        
        // 构建下载URL
        return downloadUrlPrefix + "/" + version + "?platform=" + platform;
    }
    
    @Override
    public UpdatePackage uploadUpdatePackage(UpdatePackageDto updatePackageDto) {
        logger.info("上传升级包: appId={}, 版本={}, 平台={}", updatePackageDto.getAppId(), updatePackageDto.getVersion(), updatePackageDto.getPlatform());
        
        // 验证版本号格式
        if (!isValidVersion(updatePackageDto.getVersion())) {
            throw new IllegalArgumentException("版本号格式无效: " + updatePackageDto.getVersion());
        }
        
        // 自动创建或更新应用记录
        ensureAppExists(updatePackageDto.getAppId(), updatePackageDto.getVersion(), updatePackageDto.getPlatform());
        
        // 检查版本是否已存在（根据应用标识符、版本和平台）
        if (updatePackageRepository.existsByVersionAndAppIdAndPlatform(
            updatePackageDto.getVersion(), updatePackageDto.getAppId(), updatePackageDto.getPlatform())) {
            logger.warn("版本已存在，尝试更新现有记录: {} for {} on {}", 
                updatePackageDto.getVersion(), updatePackageDto.getAppId(), updatePackageDto.getPlatform());
            
            // 查找现有记录并更新
            Optional<UpdatePackage> existingPackage = updatePackageRepository
                .findByVersionAndAppIdAndPlatform(updatePackageDto.getVersion(), updatePackageDto.getAppId(), updatePackageDto.getPlatform());
            
            if (existingPackage.isPresent()) {
                UpdatePackage packageToUpdate = existingPackage.get();
                
                try {
                    // 更新文件相关信息
                    String fileUrl = fileStorageService.uploadFile(
                        updatePackageDto.getFile(),
                        updatePackageDto.getFile().getOriginalFilename(),
                        "updates/" + updatePackageDto.getPlatform()
                    );
                    
                    String fileHash = calculateFileHash(updatePackageDto.getFile());
                    
                    packageToUpdate.setFileUrl(fileUrl);
                    packageToUpdate.setFileHash(fileHash);
                    packageToUpdate.setFileName(updatePackageDto.getFile().getOriginalFilename());
                    packageToUpdate.setFileSize(updatePackageDto.getFile().getSize());
                    packageToUpdate.setFileType(updatePackageDto.getFile().getContentType());
                    packageToUpdate.setReleaseNotes(updatePackageDto.getReleaseNotes());
                    packageToUpdate.setDescription(updatePackageDto.getDescription());
                    packageToUpdate.setIsMandatory(updatePackageDto.getIsMandatory());
                    
                    UpdatePackage savedPackage = updatePackageRepository.save(packageToUpdate);
                    logger.info("升级包更新成功: ID={}", savedPackage.getId());
                    return savedPackage;
                    
                } catch (IOException e) {
                    logger.error("文件处理失败", e);
                    throw new RuntimeException("文件处理失败: " + e.getMessage());
                }
            }
        }
        
        try {
            // 上传文件
            String fileUrl = fileStorageService.uploadFile(
                updatePackageDto.getFile(),
                updatePackageDto.getFile().getOriginalFilename(),
                "updates/" + updatePackageDto.getPlatform()
            );
            
            // 计算文件哈希值
            String fileHash = calculateFileHash(updatePackageDto.getFile());
            
            // 创建升级包实体
            UpdatePackage updatePackage = new UpdatePackage();
            updatePackage.setAppId(updatePackageDto.getAppId()); // 设置应用标识符
            updatePackage.setVersion(updatePackageDto.getVersion());
            updatePackage.setPlatform(updatePackageDto.getPlatform());
            updatePackage.setFileUrl(fileUrl);
            updatePackage.setFileHash(fileHash);
            updatePackage.setHashAlgorithm(defaultHashAlgorithm);
            updatePackage.setReleaseNotes(updatePackageDto.getReleaseNotes());
            updatePackage.setIsMandatory(updatePackageDto.getIsMandatory());
            updatePackage.setDescription(updatePackageDto.getDescription());
            updatePackage.setFileName(updatePackageDto.getFile().getOriginalFilename());
            updatePackage.setFileSize(updatePackageDto.getFile().getSize());
            updatePackage.setFileType(updatePackageDto.getFile().getContentType());
            
            // 保存到数据库
            UpdatePackage savedPackage = updatePackageRepository.save(updatePackage);
            logger.info("升级包上传成功: ID={}", savedPackage.getId());
            
            return savedPackage;
            
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public Page<UpdatePackage> getAllUpdatePackages(Pageable pageable) {
        return updatePackageRepository.findAllByOrderByReleaseDateDesc(pageable);
    }
    
    @Override
    public Page<UpdatePackage> getUpdatePackagesByAppIdAndPlatform(String appId, String platform, Pageable pageable) {
        return updatePackageRepository.findByAppIdAndPlatformOrderByReleaseDateDesc(appId, platform, pageable);
    }
    
    @Override
    public Page<UpdatePackage> getUpdatePackagesByPlatform(String platform, Pageable pageable) {
        return updatePackageRepository.findByPlatformOrderByReleaseDateDesc(platform, pageable);
    }
    
    @Override
    public UpdatePackage getUpdatePackageById(Long id) {
        return updatePackageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("升级包不存在: " + id));
    }
    
    @Override
    public UpdatePackage getUpdatePackageByVersionAndAppIdAndPlatform(String version, String appId, String platform) {
        return updatePackageRepository.findByVersionAndAppIdAndPlatform(version, appId, platform)
            .orElseThrow(() -> new RuntimeException("升级包不存在: version=" + version + ", appId=" + appId + ", platform=" + platform));
    }
    
    @Override
    public UpdatePackage getUpdatePackageByVersionAndPlatform(String version, String platform) {
        return updatePackageRepository.findByVersionAndPlatform(version, platform)
            .orElseThrow(() -> new RuntimeException("升级包不存在: " + version + " for " + platform));
    }
    
    @Override
    public UpdatePackage updateUpdatePackage(Long id, UpdatePackage updatePackage) {
        UpdatePackage existingPackage = getUpdatePackageById(id);
        
        // 更新字段
        if (StringUtils.hasText(updatePackage.getReleaseNotes())) {
            existingPackage.setReleaseNotes(updatePackage.getReleaseNotes());
        }
        if (updatePackage.getIsMandatory() != null) {
            existingPackage.setIsMandatory(updatePackage.getIsMandatory());
        }
        if (StringUtils.hasText(updatePackage.getDescription())) {
            existingPackage.setDescription(updatePackage.getDescription());
        }
        if (updatePackage.getIsActive() != null) {
            existingPackage.setIsActive(updatePackage.getIsActive());
        }
        
        return updatePackageRepository.save(existingPackage);
    }
    
    @Override
    public boolean deleteUpdatePackage(Long id) {
        UpdatePackage updatePackage = getUpdatePackageById(id);
        
        try {
            // 删除文件
            fileStorageService.deleteFile(updatePackage.getFileUrl());
            
            // 删除数据库记录
            updatePackageRepository.delete(updatePackage);
            
            logger.info("升级包删除成功: ID={}", id);
            return true;
            
        } catch (Exception e) {
            logger.error("删除升级包失败: ID={}", id, e);
            return false;
        }
    }
    
    @Override
    public UpdatePackage setUpdatePackageActive(Long id, boolean isActive) {
        UpdatePackage updatePackage = getUpdatePackageById(id);
        updatePackage.setIsActive(isActive);
        return updatePackageRepository.save(updatePackage);
    }
    
    @Override
    public List<String> getAllPlatforms() {
        return updatePackageRepository.findAll().stream()
            .map(UpdatePackage::getPlatform)
            .distinct()
            .sorted()
            .toList();
    }
    
    @Override
    public List<UpdatePackage> getMandatoryUpdates(String appId, String platform) {
        return updatePackageRepository.findMandatoryUpdatesByAppIdAndPlatform(appId, platform);
    }
    
    @Override
    public List<UpdatePackage> getMandatoryUpdates(String platform) {
        return updatePackageRepository.findMandatoryUpdatesByPlatform(platform);
    }
    
    @Override
    public boolean isValidVersion(String version) {
        if (!StringUtils.hasText(version)) {
            return false;
        }
        return VERSION_PATTERN.matcher(version).matches();
    }
    
    @Override
    public int compareVersions(String version1, String version2) {
        if (!isValidVersion(version1) || !isValidVersion(version2)) {
            throw new IllegalArgumentException("版本号格式无效");
        }
        
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        
        int maxLength = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            
            if (num1 < num2) return -1;
            if (num1 > num2) return 1;
        }
        
        return 0;
    }
    
    /**
     * 创建更新响应
     */
    private UpdateCheckResponse createUpdateResponse(UpdatePackage updatePackage) {
        String downloadUrl = downloadUrlPrefix + "/" + updatePackage.getVersion() + "?platform=" + updatePackage.getPlatform();
        
        return new UpdateCheckResponse(
            true,
            updatePackage.getVersion(),
            updatePackage.getReleaseNotes(),
            updatePackage.getIsMandatory(),
            updatePackage.getReleaseDate(),
            downloadUrl,
            updatePackage.getFileHash(),
            updatePackage.getHashAlgorithm(),
            updatePackage.getFileSize(),
            updatePackage.getFileName(),
            updatePackage.getDescription()
        );
    }
    
    /**
     * 确保应用记录存在，如果不存在则自动创建
     */
    private void ensureAppExists(String appId, String version, String platform) {
        try {
            // 检查应用是否存在
            Optional<App> existingApp = appService.getAppByAppId(appId);
            
            if (existingApp.isEmpty()) {
                // 应用不存在，自动创建
                logger.info("应用不存在，自动创建: appId={}", appId);
                
                App newApp = new App();
                newApp.setAppId(appId);
                newApp.setName(appId); // 使用appId作为名称
                newApp.setDescription("自动创建的应用");
                newApp.setCurrentVersion(version);
                newApp.setMinVersion("1.0.0");
                newApp.setRecommendedVersion(version);
                newApp.setDeveloper("Auto Created");
                newApp.setDeveloperEmail("auto@system.com");
                newApp.setLicense("Commercial");
                newApp.setFeatures("自动创建");
                newApp.setChangelog("自动创建的应用");
                newApp.setIsActive(true);
                newApp.setIsPublic(true);
                newApp.setIsMandatoryUpdate(false);
                newApp.setCategory("工具");
                newApp.setTags("自动创建");
                newApp.setAppType(App.AppType.TOOL);
                newApp.setPlatform(App.Platform.valueOf(platform.toUpperCase()));
                newApp.setIsFree(false);
                newApp.setIsFeatured(false);
                newApp.setDownloadCount(0L);
                newApp.setRating(0.0);
                newApp.setRatingCount(0L);
                
                appService.createApp(newApp);
                logger.info("应用创建成功: appId={}", appId);
                
            } else {
                // 应用存在，更新版本信息
                App app = existingApp.get();
                if (compareVersions(version, app.getCurrentVersion()) > 0) {
                    logger.info("更新应用版本: appId={}, 从 {} 更新到 {}", appId, app.getCurrentVersion(), version);
                    app.setCurrentVersion(version);
                    app.setRecommendedVersion(version);
                    appService.updateApp(app.getId(), app);
                }
            }
            
        } catch (Exception e) {
            logger.error("自动创建应用失败: appId={}", appId, e);
            // 不抛出异常，让上传流程继续
        }
    }
    
    /**
     * 计算文件哈希值
     */
    private String calculateFileHash(MultipartFile file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(defaultHashAlgorithm);
            byte[] hash = digest.digest(file.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("不支持的哈希算法: " + defaultHashAlgorithm);
        }
    }
} 