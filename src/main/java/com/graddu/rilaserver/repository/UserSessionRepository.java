package net.enjoy.springboot.registrationlogin.repository;

import net.enjoy.springboot.registrationlogin.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    int countByUserIdAndStatus(String userId, String status);
    List<UserSession> findByUserIdAndStatus(String userId, String status);
    UserSession findByUserIdAndDeviceId(String userId, String deviceId);
    List<UserSession> findByUserId(String userId);
}