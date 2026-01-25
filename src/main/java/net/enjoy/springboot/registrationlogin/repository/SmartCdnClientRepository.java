package net.enjoy.springboot.registrationlogin.repository;

import java.util.List;
import net.enjoy.springboot.registrationlogin.entity.SmartCdnClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmartCdnClientRepository extends JpaRepository<SmartCdnClient, Long> {

    List<SmartCdnClient> findByLanId(String lanId);

    SmartCdnClient findFirstByClientIdOrderByUpdatedAtDesc(String clientId);

    long deleteByClientId(String clientId);
}
