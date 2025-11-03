package net.enjoy.springboot.registrationlogin.repository;

import net.enjoy.springboot.registrationlogin.entity.LiveStreamTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveStreamTaskRepository extends JpaRepository<LiveStreamTask, Long> {
    
    /**
     * 根据客户端ID查找所有任务
     */
    List<LiveStreamTask> findByClientIdOrderByTaskIndex(String clientId);
    
    /**
     * 根据客户端ID分页查找任务
     */
    Page<LiveStreamTask> findByClientId(String clientId, Pageable pageable);
    
    /**
     * 根据客户端ID和任务索引查找任务
     */
    Optional<LiveStreamTask> findByClientIdAndTaskIndex(String clientId, Integer taskIndex);
    
    /**
     * 检查客户端ID和任务索引是否存在
     */
    boolean existsByClientIdAndTaskIndex(String clientId, Integer taskIndex);
    
    /**
     * 查找需要检查的任务（激活状态且到达检查时间）
     */
    @Query("SELECT t FROM LiveStreamTask t WHERE t.isActive = true AND " +
           "(t.nextCheckTime IS NULL OR t.nextCheckTime <= :now) AND " +
           "t.clientId = :clientId")
    List<LiveStreamTask> findTasksNeedingCheck(@Param("clientId") String clientId, @Param("now") LocalDateTime now);
    
    /**
     * 查找所有需要检查的任务
     */
    @Query("SELECT t FROM LiveStreamTask t WHERE t.isActive = true AND " +
           "(t.nextCheckTime IS NULL OR t.nextCheckTime <= :now)")
    List<LiveStreamTask> findAllTasksNeedingCheck(@Param("now") LocalDateTime now);
    
    /**
     * 根据平台查找任务
     */
    List<LiveStreamTask> findByClientIdAndPlatform(String clientId, String platform);
    
    /**
     * 统计客户端的任务数量
     */
    long countByClientId(String clientId);
    
    /**
     * 统计客户端的激活任务数量
     */
    long countByClientIdAndIsActiveTrue(String clientId);
    
    /**
     * 删除客户端的所有任务
     */
    @Modifying
    @Query("DELETE FROM LiveStreamTask t WHERE t.clientId = :clientId")
    void deleteByClientId(@Param("clientId") String clientId);
    
    /**
     * 批量删除任务
     */
    @Modifying
    @Query("DELETE FROM LiveStreamTask t WHERE t.clientId = :clientId AND t.taskIndex IN :taskIndexes")
    void deleteByClientIdAndTaskIndexes(@Param("clientId") String clientId, @Param("taskIndexes") List<Integer> taskIndexes);
}
