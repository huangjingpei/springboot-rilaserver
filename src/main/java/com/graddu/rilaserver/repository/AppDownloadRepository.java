package net.enjoy.springboot.registrationlogin.repository;

import net.enjoy.springboot.registrationlogin.entity.AppDownload;
import net.enjoy.springboot.registrationlogin.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppDownloadRepository extends JpaRepository<AppDownload, Long> {

    // 根据应用ID查找所有下载链接
    List<AppDownload> findByAppIdAndIsActiveTrue(Long appId);

    // 根据应用ID和平台查找下载链接
    Optional<AppDownload> findByAppIdAndPlatformAndIsActiveTrue(Long appId, App.Platform platform);

    // 根据平台查找所有下载链接
    List<AppDownload> findByPlatformAndIsActiveTrue(App.Platform platform);

    // 增加下载次数
    @Modifying
    @Query("UPDATE AppDownload ad SET ad.downloadCount = ad.downloadCount + 1 WHERE ad.id = :id")
    void incrementDownloadCount(@Param("id") Long id);

    // 根据应用ID查找所有下载链接（包括非活跃的）
    List<AppDownload> findByAppId(Long appId);

    // 根据应用ID和平台查找下载链接（包括非活跃的）
    Optional<AppDownload> findByAppIdAndPlatform(Long appId, App.Platform platform);

    // 统计应用的下载次数
    @Query("SELECT SUM(ad.downloadCount) FROM AppDownload ad WHERE ad.app.id = :appId AND ad.isActive = true")
    Long getTotalDownloadCountByAppId(@Param("appId") Long appId);

    // 根据下载次数排序查找
    List<AppDownload> findByIsActiveTrueOrderByDownloadCountDesc();

    // 根据平台和下载次数排序查找
    List<AppDownload> findByPlatformAndIsActiveTrueOrderByDownloadCountDesc(App.Platform platform);
} 