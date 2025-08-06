package net.enjoy.springboot.registrationlogin.repository;

import net.enjoy.springboot.registrationlogin.entity.App;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {

    // 根据关键词搜索应用名称和描述
    @Query("SELECT a FROM App a WHERE a.isActive = true AND " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<App> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 根据应用类型查找
    Page<App> findByTypeAndIsActiveTrue(App.AppType type, Pageable pageable);

    // 根据平台查找 - 暂时注释掉复杂的查询
    // @Query("SELECT DISTINCT a FROM App a JOIN a.platforms p WHERE p IN :platforms AND a.isActive = true")
    // Page<App> findByPlatforms(@Param("platforms") List<App.Platform> platforms, Pageable pageable);

    // 根据价格范围查找
    Page<App> findByPriceBetweenAndIsActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // 根据评分范围查找
    Page<App> findByRatingBetweenAndIsActiveTrue(BigDecimal minRating, BigDecimal maxRating, Pageable pageable);

    // 查找免费应用
    Page<App> findByPriceAndIsActiveTrue(BigDecimal price, Pageable pageable);

    // 查找推荐应用
    Page<App> findByIsFeaturedTrueAndIsActiveTrue(Pageable pageable);

    // 复杂搜索查询 - 最简化版本
    @Query("SELECT a FROM App a WHERE a.isActive = true")
    Page<App> searchApps(@Param("keyword") String keyword,
                         @Param("type") App.AppType type,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("minRating") BigDecimal minRating,
                         @Param("maxRating") BigDecimal maxRating,
                         @Param("isFree") Boolean isFree,
                         @Param("isFeatured") Boolean isFeatured,
                         Pageable pageable);

    // 根据下载量排序查找
    Page<App> findByIsActiveTrueOrderByDownloadCountDesc(Pageable pageable);

    // 根据评分排序查找
    Page<App> findByIsActiveTrueOrderByRatingDesc(Pageable pageable);

    // 根据创建时间排序查找
    Page<App> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    // 根据价格排序查找
    Page<App> findByIsActiveTrueOrderByPriceAsc(Pageable pageable);

    // 查找所有活跃的应用类型
    @Query("SELECT DISTINCT a.type FROM App a WHERE a.isActive = true")
    List<App.AppType> findAllActiveTypes();

    // 查找所有活跃的平台
    @Query("SELECT DISTINCT p FROM App a JOIN a.platforms p WHERE a.isActive = true")
    List<App.Platform> findAllActivePlatforms();

    // 统计应用数量
    @Query("SELECT COUNT(a) FROM App a WHERE a.isActive = true")
    Long countActiveApps();

    // 统计免费应用数量
    Long countByPriceAndIsActiveTrue(BigDecimal price);

    // 统计推荐应用数量
    Long countByIsFeaturedTrueAndIsActiveTrue();

    // 统计总下载次数
    @Query("SELECT COALESCE(SUM(a.downloadCount), 0) FROM App a WHERE a.isActive = true")
    Long sumDownloadCountByIsActiveTrue();

    // 根据开发者查找应用
    Page<App> findByDeveloperAndIsActiveTrue(String developer, Pageable pageable);
} 