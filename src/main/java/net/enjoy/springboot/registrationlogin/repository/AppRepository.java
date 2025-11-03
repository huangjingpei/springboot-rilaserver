package net.enjoy.springboot.registrationlogin.repository;

import net.enjoy.springboot.registrationlogin.entity.App;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {
    
    /**
     * 根据应用标识符查找应用
     */
    Optional<App> findByAppId(String appId);
    
    /**
     * 根据应用标识符查找激活的应用
     */
    Optional<App> findByAppIdAndIsActiveTrue(String appId);
    
    /**
     * 查找所有激活的应用
     */
    List<App> findByIsActiveTrue();
    
    /**
     * 分页查找所有激活的应用
     */
    Page<App> findByIsActiveTrue(Pageable pageable);
    
    /**
     * 查找所有公开的应用
     */
    List<App> findByIsPublicTrue();
    
    /**
     * 分页查找所有公开的应用
     */
    Page<App> findByIsPublicTrue(Pageable pageable);
    
    /**
     * 查找所有激活且公开的应用
     */
    List<App> findByIsActiveTrueAndIsPublicTrue();
    
    /**
     * 根据分类查找应用
     */
    List<App> findByCategoryAndIsActiveTrue(String category);
    
    /**
     * 根据标签查找应用（模糊匹配）
     */
    @Query("SELECT a FROM App a WHERE a.isActive = true AND a.tags LIKE %:tag%")
    List<App> findByTagContaining(@Param("tag") String tag);
    
    /**
     * 根据应用名称模糊查找
     */
    @Query("SELECT a FROM App a WHERE a.isActive = true AND (a.name LIKE %:keyword% OR a.description LIKE %:keyword%)")
    Page<App> searchApps(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 查找所有应用分类
     */
    @Query("SELECT DISTINCT a.category FROM App a WHERE a.category IS NOT NULL AND a.isActive = true")
    List<String> findAllCategories();
    
    /**
     * 查找所有应用标签
     */
    @Query("SELECT DISTINCT a.tags FROM App a WHERE a.isActive = true AND a.tags IS NOT NULL")
    List<String> findAllTags();
    
    /**
     * 检查应用标识符是否存在
     */
    boolean existsByAppId(String appId);
    
    /**
     * 根据开发者查找应用
     */
    List<App> findByDeveloperAndIsActiveTrue(String developer);
    
    /**
     * 查找需要强制更新的应用
     */
    List<App> findByIsMandatoryUpdateTrueAndIsActiveTrue();
    
    /**
     * 根据应用类型查找应用
     */
    Page<App> findByAppTypeAndIsActiveTrue(App.AppType appType, Pageable pageable);
    
    /**
     * 根据平台查找应用
     */
    Page<App> findByPlatformAndIsActiveTrue(App.Platform platform, Pageable pageable);
    
    /**
     * 查找免费应用
     */
    Page<App> findByIsFreeTrueAndIsActiveTrue(Pageable pageable);
    
    /**
     * 查找推荐应用
     */
    Page<App> findByIsFeaturedTrueAndIsActiveTrue(Pageable pageable);
    
    /**
     * 根据开发者查找应用（分页）
     */
    Page<App> findByDeveloperAndIsActiveTrue(String developer, Pageable pageable);
    
    /**
     * 获取所有应用类型
     */
    @Query("SELECT DISTINCT a.appType FROM App a WHERE a.isActive = true AND a.appType IS NOT NULL")
    List<App.AppType> findAllAppTypes();
    
    /**
     * 获取所有平台
     */
    @Query("SELECT DISTINCT a.platform FROM App a WHERE a.isActive = true AND a.platform IS NOT NULL")
    List<App.Platform> findAllPlatforms();
    
    /**
     * 获取应用数量
     */
    @Query("SELECT COUNT(a) FROM App a WHERE a.isActive = true")
    Long countActiveApps();
    
    /**
     * 获取免费应用数量
     */
    @Query("SELECT COUNT(a) FROM App a WHERE a.isActive = true AND a.isFree = true")
    Long countFreeApps();
    
    /**
     * 获取推荐应用数量
     */
    @Query("SELECT COUNT(a) FROM App a WHERE a.isActive = true AND a.isFeatured = true")
    Long countFeaturedApps();
    
    /**
     * 获取总下载次数
     */
    @Query("SELECT SUM(a.downloadCount) FROM App a WHERE a.isActive = true")
    Long getTotalDownloads();
    
    /**
     * 获取下载量前N的应用
     */
    @Query("SELECT a FROM App a WHERE a.isActive = true ORDER BY a.downloadCount DESC")
    List<App> findTopDownloads(Pageable pageable);
    
    /**
     * 按创建时间倒序查找所有应用
     */
    Page<App> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 按下载次数倒序查找所有应用
     */
    Page<App> findAllByOrderByDownloadCountDesc(Pageable pageable);
    
    /**
     * 按评分倒序查找所有应用
     */
    Page<App> findAllByOrderByRatingDesc(Pageable pageable);
} 