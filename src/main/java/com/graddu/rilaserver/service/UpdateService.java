package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.UpdateCheckResponse;
import com.graddu.rilaserver.dto.UpdatePackageDto;
import com.graddu.rilaserver.entity.UpdatePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 升级服务接口
 */
public interface UpdateService {
    
    /**
     * 检查是否有可用更新
     * @param appId 应用标识符
     * @param currentVersion 当前版本
     * @param platform 平台信息
     * @return 更新检查响应
     */
    UpdateCheckResponse checkForUpdate(String appId, String currentVersion, String platform);
    
    /**
     * 获取下载URL
     * @param version 版本号
     * @param platform 平台信息
     * @return 下载URL
     */
    String getDownloadUrl(String version, String platform);
    
    /**
     * 上传新的升级包
     * @param updatePackageDto 升级包信息
     * @return 保存的升级包
     */
    UpdatePackage uploadUpdatePackage(UpdatePackageDto updatePackageDto);
    
    /**
     * 获取所有升级包（分页）
     * @param pageable 分页参数
     * @return 升级包列表
     */
    Page<UpdatePackage> getAllUpdatePackages(Pageable pageable);
    
    /**
     * 根据应用标识符和平台获取升级包（分页）
     * @param appId 应用标识符
     * @param platform 平台信息
     * @param pageable 分页参数
     * @return 升级包列表
     */
    Page<UpdatePackage> getUpdatePackagesByAppIdAndPlatform(String appId, String platform, Pageable pageable);
    
    /**
     * 根据平台获取升级包（分页，兼容旧版本）
     * @param platform 平台信息
     * @param pageable 分页参数
     * @return 升级包列表
     */
    Page<UpdatePackage> getUpdatePackagesByPlatform(String platform, Pageable pageable);
    
    /**
     * 根据ID获取升级包
     * @param id 升级包ID
     * @return 升级包
     */
    UpdatePackage getUpdatePackageById(Long id);
    
    /**
     * 根据版本号、应用标识符和平台获取升级包
     * @param version 版本号
     * @param appId 应用标识符
     * @param platform 平台信息
     * @return 升级包
     */
    UpdatePackage getUpdatePackageByVersionAndAppIdAndPlatform(String version, String appId, String platform);
    
    /**
     * 根据版本号和平台获取升级包（兼容旧版本）
     * @param version 版本号
     * @param platform 平台信息
     * @return 升级包
     */
    UpdatePackage getUpdatePackageByVersionAndPlatform(String version, String platform);
    
    /**
     * 更新升级包信息
     * @param id 升级包ID
     * @param updatePackage 升级包信息
     * @return 更新后的升级包
     */
    UpdatePackage updateUpdatePackage(Long id, UpdatePackage updatePackage);
    
    /**
     * 删除升级包
     * @param id 升级包ID
     * @return 是否删除成功
     */
    boolean deleteUpdatePackage(Long id);
    
    /**
     * 激活/停用升级包
     * @param id 升级包ID
     * @param isActive 是否激活
     * @return 更新后的升级包
     */
    UpdatePackage setUpdatePackageActive(Long id, boolean isActive);
    
    /**
     * 获取所有支持的平台
     * @return 平台列表
     */
    List<String> getAllPlatforms();
    
    /**
     * 根据应用标识符和平台获取强制更新的版本
     * @param appId 应用标识符
     * @param platform 平台信息
     * @return 强制更新版本列表
     */
    List<UpdatePackage> getMandatoryUpdates(String appId, String platform);
    
    /**
     * 根据平台获取强制更新的版本（兼容旧版本）
     * @param platform 平台信息
     * @return 强制更新版本列表
     */
    List<UpdatePackage> getMandatoryUpdates(String platform);
    
    /**
     * 验证版本号格式
     * @param version 版本号
     * @return 是否有效
     */
    boolean isValidVersion(String version);
    
    /**
     * 比较版本号
     * @param version1 版本1
     * @param version2 版本2
     * @return 比较结果：-1(v1<v2), 0(v1=v2), 1(v1>v2)
     */
    int compareVersions(String version1, String version2);
} 