package net.enjoy.springboot.registrationlogin.repository;

import net.enjoy.springboot.registrationlogin.entity.LiveStreamStatusLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveStreamStatusLogRepository extends JpaRepository<LiveStreamStatusLog, Long> {
    
    /**
     * 根据配置ID查找状态变更日志
     */
    List<LiveStreamStatusLog> findByConfigIdOrderByChangeTimeDesc(Long configId);
    
    /**
     * 根据客户端ID查找状态变更日志
     */
    List<LiveStreamStatusLog> findByClientIdOrderByChangeTimeDesc(String clientId);
    
    /**
     * 根据客户端ID分页查找状态变更日志
     */
    Page<LiveStreamStatusLog> findByClientIdOrderByChangeTimeDesc(String clientId, Pageable pageable);
    
    /**
     * 根据配置ID和时间范围查找状态变更日志
     */
    @Query("SELECT l FROM LiveStreamStatusLog l WHERE l.configId = :configId AND " +
           "l.changeTime BETWEEN :startTime AND :endTime ORDER BY l.changeTime DESC")
    List<LiveStreamStatusLog> findByConfigIdAndTimeRange(
            @Param("configId") Long configId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据客户端ID和时间范围查找状态变更日志
     */
    @Query("SELECT l FROM LiveStreamStatusLog l WHERE l.clientId = :clientId AND " +
           "l.changeTime BETWEEN :startTime AND :endTime ORDER BY l.changeTime DESC")
    List<LiveStreamStatusLog> findByClientIdAndTimeRange(
            @Param("clientId") String clientId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找最近的状态变更日志
     */
    @Query("SELECT l FROM LiveStreamStatusLog l WHERE l.changeTime >= :since ORDER BY l.changeTime DESC")
    List<LiveStreamStatusLog> findRecentLogs(@Param("since") LocalDateTime since);
    
    /**
     * 统计客户端的状态变更次数
     */
    long countByClientId(String clientId);
    
    /**
     * 统计配置的状态变更次数
     */
    long countByConfigId(Long configId);
}

