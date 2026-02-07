package net.enjoy.springboot.registrationlogin.service;

import java.time.LocalDateTime;

import net.enjoy.springboot.registrationlogin.config.SmartCdnConfig;
import net.enjoy.springboot.registrationlogin.constant.SmartCdnRedisKey;
import net.enjoy.springboot.registrationlogin.entity.StreamRelayNode;
import net.enjoy.springboot.registrationlogin.repository.SmartCdnClientRepository;
import net.enjoy.springboot.registrationlogin.repository.StreamRelayNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SmartCdnCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SmartCdnCleanupScheduler.class);

    @Autowired
    private SmartCdnConfig smartCdnConfig;

    @Autowired
    private SmartCdnClientRepository smartCdnClientRepository;

    @Autowired
    private StreamRelayNodeRepository streamRelayNodeRepository;

    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    @Transactional
    public void cleanup() {
        try {
            if (!smartCdnConfig.isEnabled()) {
                return;
            }

            // Only run cleanup if using MySQL storage (Redis handles expiry automatically)
            if (!"mysql".equalsIgnoreCase(smartCdnConfig.getStorageType())) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            // Clean up inactive clients
            // Use a slight buffer to avoid race conditions with just-expired items
            LocalDateTime clientThreshold = now.minusSeconds(SmartCdnRedisKey.CLIENT_EXPIRE_SECONDS);
            smartCdnClientRepository.deleteByLastSeenAtBefore(clientThreshold);

            // Clean up inactive relay nodes
            LocalDateTime nodeThreshold = now.minusSeconds(SmartCdnRedisKey.NODE_EXPIRE_SECONDS);
            streamRelayNodeRepository.deleteByUpdatedAtBeforeAndStatus(nodeThreshold, StreamRelayNode.Status.ACTIVE);
            
            // Note: In a production environment with strict consistency requirements, 
            // we might also want to recalculate parent subscriber counts here, 
            // as deleting children doesn't automatically decrement parent's currentSubscribers.
            // However, to maintain parity with the Redis implementation (which relies on key expiration),
            // we primarily focus on removing stale records.

        } catch (Exception e) {
            logger.error("Error during SmartCDN cleanup", e);
        }
    }
}
