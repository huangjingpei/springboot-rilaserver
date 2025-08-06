package net.enjoy.springboot.registrationlogin.repository;

import net.enjoy.springboot.registrationlogin.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    
    @Query("SELECT ud FROM UserDevice ud WHERE ud.userId = :userId AND ud.isActive = true")
    List<UserDevice> findActiveDevicesByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(ud) FROM UserDevice ud WHERE ud.userId = :userId AND ud.isActive = true")
    int countActiveDevicesByUserId(@Param("userId") String userId);
    
    @Query("SELECT ud FROM UserDevice ud WHERE ud.userId = :userId AND ud.deviceId = :deviceId")
    Optional<UserDevice> findByUserIdAndDeviceId(@Param("userId") String userId, @Param("deviceId") String deviceId);
    
    @Query("SELECT ud FROM UserDevice ud WHERE ud.deviceId = :deviceId AND ud.isActive = true")
    List<UserDevice> findActiveDevicesByDeviceId(@Param("deviceId") String deviceId);
} 