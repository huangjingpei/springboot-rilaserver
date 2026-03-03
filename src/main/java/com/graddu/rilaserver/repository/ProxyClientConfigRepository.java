package com.graddu.rilaserver.repository;

import com.graddu.rilaserver.entity.ProxyClientConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProxyClientConfigRepository extends JpaRepository<ProxyClientConfig, Long> {
    
    /**
     * 根据用户ID查找配置
     * @param userId 用户ID
     * @return 配置信息
     */
    Optional<ProxyClientConfig> findByUserId(String userId);
    
    /**
     * 检查用户ID是否存在配置
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByUserId(String userId);
    
    /**
     * 根据用户ID删除配置
     * @param userId 用户ID
     */
    @Modifying
    @Query("DELETE FROM ProxyClientConfig p WHERE p.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}
