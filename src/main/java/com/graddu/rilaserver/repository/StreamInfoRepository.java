package com.graddu.rilaserver.repository;

import com.graddu.rilaserver.entity.StreamInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreamInfoRepository extends JpaRepository<StreamInfo, Long> {
    
    /**
     * 根据流ID查找流信息
     * @param streamId ZLMediaKit的流ID
     * @return StreamInfo
     */
    Optional<StreamInfo> findByStreamId(String streamId);
    
    /**
     * 根据用户ID查找流信息
     */
    List<StreamInfo> findByUserId(String userId);
    
    /**
     * 根据状态查找流信息
     */
    List<StreamInfo> findByStatus(StreamInfo.StreamStatus status);
    
    /**
     * 根据用户ID和状态查找流信息
     */
    List<StreamInfo> findByUserIdAndStatus(String userId, StreamInfo.StreamStatus status);
    
    /**
     * 查找活跃的流（仅推流中）
     * 注意：流状态应该只反映推流者状态，PLAYING不应该作为流状态
     */
    @Query("SELECT s FROM StreamInfo s WHERE s.status = 'PUSHING'")
    List<StreamInfo> findActiveStreams();
    
    /**
     * 查找用户活跃的流（仅推流中）
     * 注意：流状态应该只反映推流者状态，PLAYING不应该作为流状态
     */
    @Query("SELECT s FROM StreamInfo s WHERE s.userId = :userId AND s.status = 'PUSHING'")
    List<StreamInfo> findActiveStreamsByUserId(@Param("userId") String userId);
    
    /**
     * 查找指定时间范围内的流
     */
    @Query("SELECT s FROM StreamInfo s WHERE s.createdAt BETWEEN :startTime AND :endTime")
    List<StreamInfo> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计用户的总流数
     */
    @Query("SELECT COUNT(s) FROM StreamInfo s WHERE s.userId = :userId")
    long countByUserId(@Param("userId") String userId);
    
    /**
     * 统计活跃流数量（仅推流中）
     * 注意：流状态应该只反映推流者状态，PLAYING不应该作为流状态
     */
    @Query("SELECT COUNT(s) FROM StreamInfo s WHERE s.status = 'PUSHING'")
    long countActiveStreams();
} 