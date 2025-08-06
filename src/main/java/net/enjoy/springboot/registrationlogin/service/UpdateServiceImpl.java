package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.dto.UpdateCheckResponse;
import net.enjoy.springboot.registrationlogin.dto.UpdatePackageDto;
import net.enjoy.springboot.registrationlogin.entity.UpdatePackage;
import net.enjoy.springboot.registrationlogin.repository.UpdatePackageRepository;
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
    
    @Value("${app.update.download-url-prefix:/api/v1/updates/file}")
    private String downloadUrlPrefix;
    
    @Value("${app.update.hash-algorithm:SHA-256}")
    private String defaultHashAlgorithm;
    
    @Override
    public UpdateCheckResponse checkForUpdate(String currentVersion, String platform) {
        logger.info("检查更新: 当前版本={}, 平台={}", currentVersion, platform);
        
        // 验证参数
        if (!StringUtils.hasText(currentVersion) || !StringUtils.hasText(platform)) {
            logger.warn("参数无效: currentVersion={}, platform={}", currentVersion, platform);
            return new UpdateCheckResponse(false);
        }
        
        // 验证版本号格式
        if (!isValidVersion(currentVersion)) {
            logger.warn("当前版本号格式无效: {}", currentVersion);
            return new UpdateCheckResponse(false);
        }
        
        try {
            // 查找最新的激活版本
            Optional<UpdatePackage> latestPackageOpt = updatePackageRepository
                .findTopByPlatformAndIsActiveTrueOrderByReleaseDateDesc(platform);
            
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
                logger.info("未找到平台 {} 的升级包", platform);
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
        logger.info("上传升级包: 版本={}, 平台={}", updatePackageDto.getVersion(), updatePackageDto.getPlatform());
        
        // 验证版本号格式
        if (!isValidVersion(updatePackageDto.getVersion())) {
            throw new IllegalArgumentException("版本号格式无效: " + updatePackageDto.getVersion());
        }
        
        // 检查版本是否已存在
        if (updatePackageRepository.existsByVersionAndPlatform(
            updatePackageDto.getVersion(), updatePackageDto.getPlatform())) {
            throw new IllegalArgumentException("该版本已存在: " + updatePackageDto.getVersion() + " for " + updatePackageDto.getPlatform());
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
    public Page<UpdatePackage> getUpdatePackagesByPlatform(String platform, Pageable pageable) {
        return updatePackageRepository.findByPlatformOrderByReleaseDateDesc(platform, pageable);
    }
    
    @Override
    public UpdatePackage getUpdatePackageById(Long id) {
        return updatePackageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("升级包不存在: " + id));
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