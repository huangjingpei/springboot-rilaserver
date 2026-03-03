package com.graddu.rilaserver.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件存储服务接口
 * 支持多种存储方式：本地文件系统、Nginx静态服务器、对象存储等
 */
public interface FileStorageService {
    
    /**
     * 上传文件
     * @param file 要上传的文件
     * @param fileName 文件名
     * @param directory 存储目录
     * @return 文件的访问URL
     * @throws IOException 上传异常
     */
    String uploadFile(MultipartFile file, String fileName, String directory) throws IOException;
    
    /**
     * 上传文件流
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param directory 存储目录
     * @param contentType 文件类型
     * @return 文件的访问URL
     * @throws IOException 上传异常
     */
    String uploadFile(InputStream inputStream, String fileName, String directory, String contentType) throws IOException;
    
    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * 检查文件是否存在
     * @param fileUrl 文件URL
     * @return 文件是否存在
     */
    boolean fileExists(String fileUrl);
    
    /**
     * 获取文件的完整访问URL
     * @param filePath 文件路径
     * @return 完整的访问URL
     */
    String getFileUrl(String filePath);
    
    /**
     * 获取文件大小
     * @param fileUrl 文件URL
     * @return 文件大小（字节）
     */
    long getFileSize(String fileUrl);
    
    /**
     * 获取文件类型
     * @param fileUrl 文件URL
     * @return 文件MIME类型
     */
    String getFileType(String fileUrl);
} 