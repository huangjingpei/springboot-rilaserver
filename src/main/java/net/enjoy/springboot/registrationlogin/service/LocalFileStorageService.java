package net.enjoy.springboot.registrationlogin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 */
@Service
public class LocalFileStorageService implements FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageService.class);
    
    @Value("${app.file-storage.local.base-path:uploads}")
    private String basePath;
    
    @Value("${app.file-storage.local.base-url:http://localhost:8080/files}")
    private String baseUrl;
    
    @Value("${app.file-storage.local.max-file-size:524288000}")
    private long maxFileSize;
    
    @Value("${app.file-storage.local.allowed-extensions:zip,exe,msi,dmg,pkg,deb,rpm,tar.gz}")
    private String allowedExtensions;
    
    @Override
    public String uploadFile(MultipartFile file, String fileName, String directory) throws IOException {
        logger.info("上传文件: fileName={}, directory={}, size={}", fileName, directory, file.getSize());
        
        // 验证文件大小
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制: " + file.getSize() + " > " + maxFileSize);
        }
        
        // 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !isAllowedExtension(originalFilename)) {
            throw new IllegalArgumentException("不支持的文件类型: " + originalFilename);
        }
        
        // 生成唯一文件名
        String uniqueFileName = generateUniqueFileName(fileName);
        
        // 创建目录
        Path uploadDir = Paths.get(basePath, directory);
        Files.createDirectories(uploadDir);
        
        // 保存文件
        Path filePath = uploadDir.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回文件URL
        String fileUrl = getFileUrl(directory + "/" + uniqueFileName);
        logger.info("文件上传成功: {}", fileUrl);
        
        return fileUrl;
    }
    
    @Override
    public String uploadFile(InputStream inputStream, String fileName, String directory, String contentType) throws IOException {
        logger.info("上传文件流: fileName={}, directory={}, contentType={}", fileName, directory, contentType);
        
        // 验证文件扩展名
        if (!isAllowedExtension(fileName)) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileName);
        }
        
        // 生成唯一文件名
        String uniqueFileName = generateUniqueFileName(fileName);
        
        // 创建目录
        Path uploadDir = Paths.get(basePath, directory);
        Files.createDirectories(uploadDir);
        
        // 保存文件
        Path filePath = uploadDir.resolve(uniqueFileName);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回文件URL
        String fileUrl = getFileUrl(directory + "/" + uniqueFileName);
        logger.info("文件流上传成功: {}", fileUrl);
        
        return fileUrl;
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        logger.info("删除文件: {}", fileUrl);
        
        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            Path fullPath = Paths.get(basePath, filePath);
            
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                logger.info("文件删除成功: {}", fileUrl);
                return true;
            } else {
                logger.warn("文件不存在: {}", fileUrl);
                return false;
            }
        } catch (Exception e) {
            logger.error("删除文件失败: {}", fileUrl, e);
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String fileUrl) {
        String filePath = extractFilePathFromUrl(fileUrl);
        Path fullPath = Paths.get(basePath, filePath);
        return Files.exists(fullPath);
    }
    
    @Override
    public String getFileUrl(String filePath) {
        return baseUrl + "/" + filePath;
    }
    
    @Override
    public long getFileSize(String fileUrl) {
        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            Path fullPath = Paths.get(basePath, filePath);
            
            if (Files.exists(fullPath)) {
                return Files.size(fullPath);
            }
        } catch (Exception e) {
            logger.error("获取文件大小失败: {}", fileUrl, e);
        }
        return 0;
    }
    
    @Override
    public String getFileType(String fileUrl) {
        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            Path fullPath = Paths.get(basePath, filePath);
            
            if (Files.exists(fullPath)) {
                return Files.probeContentType(fullPath);
            }
        } catch (Exception e) {
            logger.error("获取文件类型失败: {}", fileUrl, e);
        }
        return "application/octet-stream";
    }
    
    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (StringUtils.hasText(originalFileName)) {
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalFileName.substring(lastDotIndex);
            }
        }
        
        return UUID.randomUUID().toString() + extension;
    }
    
    /**
     * 检查文件扩展名是否允许
     */
    private boolean isAllowedExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }
        
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        
        String[] allowed = allowedExtensions.split(",");
        for (String allowedExt : allowed) {
            if (allowedExt.trim().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 从URL中提取文件路径
     */
    private String extractFilePathFromUrl(String fileUrl) {
        if (fileUrl.startsWith(baseUrl)) {
            return fileUrl.substring(baseUrl.length() + 1);
        }
        return fileUrl;
    }
} 