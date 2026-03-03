package com.graddu.rilaserver.repository;

import java.time.LocalDateTime;
import java.util.List;
import com.graddu.rilaserver.entity.SmartCdnClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SmartCdnClientRepository extends JpaRepository<SmartCdnClient, Long> {

    List<SmartCdnClient> findByLanId(String lanId);

    SmartCdnClient findFirstByClientIdOrderByUpdatedAtDesc(String clientId);

    long deleteByClientId(String clientId);

    @Modifying
    @Transactional
    @Query("delete from SmartCdnClient c where c.lastSeenAt < :time")
    void deleteByLastSeenAtBefore(@Param("time") LocalDateTime time);
}
