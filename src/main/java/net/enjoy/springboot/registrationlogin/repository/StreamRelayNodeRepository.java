package net.enjoy.springboot.registrationlogin.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import net.enjoy.springboot.registrationlogin.entity.StreamRelayNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;

public interface StreamRelayNodeRepository extends JpaRepository<StreamRelayNode, Long> {

    List<StreamRelayNode> findByStreamId(String streamId);

    Optional<StreamRelayNode> findByStreamIdAndDepthAndPlatform(String streamId, Integer depth, StreamRelayNode.Platform platform);

    Optional<StreamRelayNode> findByStreamIdAndPullUrl(String streamId, String pullUrl);

    @Query("select n from StreamRelayNode n where n.streamId = :streamId and n.lanId = :lanId and n.platform = net.enjoy.springboot.registrationlogin.entity.StreamRelayNode$Platform.MEDIAMTX and n.status = net.enjoy.springboot.registrationlogin.entity.StreamRelayNode$Status.ACTIVE order by n.depth asc, n.currentSubscribers asc")
    List<StreamRelayNode> findActiveMediamtxNodesByStreamAndLanOrdered(@Param("streamId") String streamId, @Param("lanId") String lanId);

    List<StreamRelayNode> findByStreamIdAndLanIdAndPlatformAndDepth(String streamId, String lanId, StreamRelayNode.Platform platform, Integer depth);

    long deleteByStreamIdAndPlatform(String streamId, StreamRelayNode.Platform platform);

    long deleteByStreamIdAndLanIdAndPlatform(String streamId, String lanId, StreamRelayNode.Platform platform);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from StreamRelayNode n where n.id = :id")
    Optional<StreamRelayNode> lockById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("delete from StreamRelayNode n where n.updatedAt < :time and n.status = :status")
    void deleteByUpdatedAtBeforeAndStatus(@Param("time") LocalDateTime time, @Param("status") StreamRelayNode.Status status);
}

