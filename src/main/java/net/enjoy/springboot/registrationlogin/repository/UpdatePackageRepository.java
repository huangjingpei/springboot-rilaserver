package net.enjoy.springboot.registrationlogin.repository;

import net.enjoy.springboot.registrationlogin.entity.UpdatePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UpdatePackageRepository extends JpaRepository<UpdatePackage, Long> {
    
    /**
     * 根据平台查找最新的激活版本
     */
    Optional<UpdatePackage> findTopByPlatformAndIsActiveTrueOrderByReleaseDateDesc(String platform);
    
    /**
     * 根据版本号和平台查找升级包
     */
    Optional<UpdatePackage> findByVersionAndPlatform(String version, String platform);
    
    /**
     * 根据平台查找所有激活的升级包
     */
    List<UpdatePackage> findByPlatformAndIsActiveTrueOrderByReleaseDateDesc(String platform);
    
    /**
     * 根据版本号查找升级包
     */
    Optional<UpdatePackage> findByVersion(String version);
    
    /**
     * 分页查询所有升级包
     */
    Page<UpdatePackage> findAllByOrderByReleaseDateDesc(Pageable pageable);
    
    /**
     * 根据平台分页查询升级包
     */
    Page<UpdatePackage> findByPlatformOrderByReleaseDateDesc(String platform, Pageable pageable);
    
    /**
     * 检查版本号是否已存在
     */
    boolean existsByVersion(String version);
    
    /**
     * 根据版本号和平台检查是否存在
     */
    boolean existsByVersionAndPlatform(String version, String platform);
    
    /**
     * 查找所有激活的升级包
     */
    List<UpdatePackage> findByIsActiveTrueOrderByReleaseDateDesc();
    
    /**
     * 根据平台查找强制更新的版本
     */
    @Query("SELECT u FROM UpdatePackage u WHERE u.platform = :platform AND u.isActive = true AND u.isMandatory = true ORDER BY u.releaseDate DESC")
    List<UpdatePackage> findMandatoryUpdatesByPlatform(@Param("platform") String platform);
} 