package com.graddu.rilaserver.repository;

import com.graddu.rilaserver.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.userId = :userId AND la.success = false AND la.attemptTime > :since")
    int countFailedAttemptsSince(@Param("userId") String userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT la FROM LoginAttempt la WHERE la.userId = :userId ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findByUserIdOrderByAttemptTimeDesc(@Param("userId") String userId);
    
    @Query("SELECT la FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.success = false AND la.attemptTime > :since")
    List<LoginAttempt> findFailedAttemptsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
} 