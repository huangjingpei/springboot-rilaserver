package com.graddu.rilaserver.repository;

import com.graddu.rilaserver.entity.LiveStreamTaskLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveStreamTaskLogRepository extends JpaRepository<LiveStreamTaskLog, Long> {
    
    /**
     * 根据任务ID查找日志
     */
    List<LiveStreamTaskLog> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    /**
     * 根据客户端ID查找日志
     */
    List<LiveStreamTaskLog> findByClientIdOrderByCreatedAtDesc(String clientId);
    
    /**
     * 根据客户端ID分页查找日志
     */
    Page<LiveStreamTaskLog> findByClientId(String clientId, Pageable pageable);
    
    /**
     * 根据操作类型查找日志
     */
    List<LiveStreamTaskLog> findByOperationTypeOrderByCreatedAtDesc(String operationType);
    
    /**
     * 根据状态查找日志
     */
    List<LiveStreamTaskLog> findByStatusOrderByCreatedAtDesc(String status);
    
    /**
     * 根据客户端ID和操作类型查找日志
     */
    List<LiveStreamTaskLog> findByClientIdAndOperationTypeOrderByCreatedAtDesc(String clientId, String operationType);
    
    /**
     * 根据时间范围查找日志
     */
    @Query("SELECT l FROM LiveStreamTaskLog l WHERE l.createdAt BETWEEN :startTime AND :endTime ORDER BY l.createdAt DESC")
    List<LiveStreamTaskLog> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据客户端ID和时间范围查找日志
     */
    @Query("SELECT l FROM LiveStreamTaskLog l WHERE l.clientId = :clientId AND l.createdAt BETWEEN :startTime AND :endTime ORDER BY l.createdAt DESC")
    List<LiveStreamTaskLog> findByClientIdAndTimeRange(@Param("clientId") String clientId, 
                                                      @Param("startTime") LocalDateTime startTime, 
                                                      @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计客户端的操作次数
     */
    long countByClientId(String clientId);
    
    /**
     * 统计客户端的成功操作次数
     */
    long countByClientIdAndStatus(String clientId, String status);
}
