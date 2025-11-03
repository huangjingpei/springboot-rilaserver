package net.enjoy.springboot.registrationlogin.repository;

import net.enjoy.springboot.registrationlogin.entity.LiveStreamConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveStreamConfigRepository extends JpaRepository<LiveStreamConfig, Long> {
    
    /**
     * 根据客户端ID查找所有配置
     */
    List<LiveStreamConfig> findByClientId(String clientId);
    
    /**
     * 根据客户端ID分页查找配置
     */
    Page<LiveStreamConfig> findByClientId(String clientId, Pageable pageable);
    
    /**
     * 根据客户端ID和启用状态查找配置
     */
    List<LiveStreamConfig> findByClientIdAndIsActiveTrue(String clientId);
    
    /**
     * 查找需要更新的配置（距离上次检查超过1分钟）
     */
    @Query("SELECT l FROM LiveStreamConfig l WHERE l.isActive = true AND " +
           "(l.lastCheckTime IS NULL OR l.lastCheckTime < :cutoffTime)")
    List<LiveStreamConfig> findConfigsNeedingUpdate(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 查找在直播时间范围内的配置
     */
    @Query("SELECT l FROM LiveStreamConfig l WHERE l.isActive = true AND " +
           "((l.startTime <= l.endTime AND :currentTime BETWEEN l.startTime AND l.endTime) OR " +
           "(l.startTime > l.endTime AND (:currentTime >= l.startTime OR :currentTime <= l.endTime)))")
    List<LiveStreamConfig> findConfigsInLiveTimeRange(@Param("currentTime") java.time.LocalTime currentTime);
    
    /**
     * 根据平台查找配置
     */
    List<LiveStreamConfig> findByPlatformAndIsActiveTrue(String platform);
    
    /**
     * 查找正在直播的配置
     */
    List<LiveStreamConfig> findByIsLiveTrue();
    
    /**
     * 根据客户端ID和直播间地址查找配置
     */
    LiveStreamConfig findByClientIdAndLiveUrl(String clientId, String liveUrl);
    
    /**
     * 检查客户端ID和直播间地址是否存在
     */
    boolean existsByClientIdAndLiveUrl(String clientId, String liveUrl);
    
    /**
     * 删除客户端的所有配置
     */
    @Modifying
    @Query("DELETE FROM LiveStreamConfig l WHERE l.clientId = :clientId")
    void deleteByClientId(@Param("clientId") String clientId);
    
    /**
     * 统计客户端的配置数量
     */
    long countByClientId(String clientId);
    
    /**
     * 统计客户端的活跃配置数量
     */
    long countByClientIdAndIsActiveTrue(String clientId);
    
    /**
     * 统计客户端的直播中配置数量
     */
    long countByClientIdAndIsLiveTrue(String clientId);
    
    /**
     * 查找所有活跃的配置
     */
    List<LiveStreamConfig> findByIsActiveTrue();
}
