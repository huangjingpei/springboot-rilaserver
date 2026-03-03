package com.graddu.rilaserver.repository;

import com.graddu.rilaserver.entity.AppReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppReviewRepository extends JpaRepository<AppReview, Long> {

    // 根据应用ID查找评论
    Page<AppReview> findByAppIdOrderByCreatedAtDesc(Long appId, Pageable pageable);

    // 根据用户ID查找评论
    Page<AppReview> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 根据应用ID和用户ID查找评论
    AppReview findByAppIdAndUserId(Long appId, Long userId);

    // 根据评分范围查找评论
    Page<AppReview> findByAppIdAndRatingBetweenOrderByCreatedAtDesc(Long appId, 
                                                                   java.math.BigDecimal minRating, 
                                                                   java.math.BigDecimal maxRating, 
                                                                   Pageable pageable);

    // 查找有帮助的评论
    Page<AppReview> findByAppIdAndIsHelpfulTrueOrderByHelpfulCountDesc(Long appId, Pageable pageable);

    // 查找已验证购买的评论
    Page<AppReview> findByAppIdAndIsVerifiedPurchaseTrueOrderByCreatedAtDesc(Long appId, Pageable pageable);

    // 统计应用的评论数量
    @Query("SELECT COUNT(r) FROM AppReview r WHERE r.app.id = :appId")
    Long countByAppId(@Param("appId") Long appId);

    // 统计应用的平均评分
    @Query("SELECT AVG(r.rating) FROM AppReview r WHERE r.app.id = :appId")
    java.math.BigDecimal getAverageRatingByAppId(@Param("appId") Long appId);

    // 根据评分排序查找评论
    Page<AppReview> findByAppIdOrderByRatingDesc(Long appId, Pageable pageable);

    // 根据评分排序查找评论（升序）
    Page<AppReview> findByAppIdOrderByRatingAsc(Long appId, Pageable pageable);

    // 根据有帮助数量排序查找评论
    Page<AppReview> findByAppIdOrderByHelpfulCountDesc(Long appId, Pageable pageable);

    // 检查用户是否已经评论过该应用
    boolean existsByAppIdAndUserId(Long appId, Long userId);

    // 查找所有评论（用于管理）
    Page<AppReview> findAllByOrderByCreatedAtDesc(Pageable pageable);
} 