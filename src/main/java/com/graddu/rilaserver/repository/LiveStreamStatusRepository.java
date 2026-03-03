package com.graddu.rilaserver.repository;

import com.graddu.rilaserver.entity.LiveStreamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiveStreamStatusRepository extends JpaRepository<LiveStreamStatus, Long> {
    
    /**
     * 根据任务ID查找最新的状态
     */
    @Query("SELECT s FROM LiveStreamStatus s WHERE s.taskId = :taskId ORDER BY s.createdAt DESC")
    List<LiveStreamStatus> findByTaskIdOrderByCreatedAtDesc(@Param("taskId") Long taskId);
    
    /**
     * 根据任务ID查找最新状态
     */
    @Query("SELECT s FROM LiveStreamStatus s WHERE s.taskId = :taskId ORDER BY s.createdAt DESC")
    Optional<LiveStreamStatus> findLatestByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 根据客户端ID查找所有状态
     */
    @Query("SELECT s FROM LiveStreamStatus s WHERE s.clientId = :clientId ORDER BY s.createdAt DESC")
    List<LiveStreamStatus> findByClientIdOrderByCreatedAtDesc(@Param("clientId") String clientId);
    
    /**
     * 根据客户端ID查找正在直播的状态
     */
    @Query("SELECT s FROM LiveStreamStatus s WHERE s.clientId = :clientId AND s.isLive = true ORDER BY s.createdAt DESC")
    List<LiveStreamStatus> findLiveStreamsByClientId(@Param("clientId") String clientId);
    
    /**
     * 统计客户端正在直播的数量
     */
    long countByClientIdAndIsLiveTrue(String clientId);
    
    /**
     * 根据任务ID和状态码查找
     */
    List<LiveStreamStatus> findByTaskIdAndStatusCode(Long taskId, Integer statusCode);
    
    /**
     * 查找失败的状态
     */
    @Query("SELECT s FROM LiveStreamStatus s WHERE s.statusCode != 0 ORDER BY s.createdAt DESC")
    List<LiveStreamStatus> findFailedStatuses();
}
