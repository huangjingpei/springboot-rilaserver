package net.enjoy.springboot.registrationlogin.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "live_stream_status_logs")
@Data
public class LiveStreamStatusLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_id", nullable = false)
    private Long configId;
    
    @Column(name = "client_id", nullable = false)
    private String clientId;
    
    @Column(name = "old_status")
    private Boolean oldStatus;
    
    @Column(name = "new_status")
    private Boolean newStatus;
    
    @CreationTimestamp
    @Column(name = "change_time")
    private java.time.LocalDateTime changeTime;
    
    @Column(name = "change_reason")
    private String changeReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", insertable = false, updatable = false)
    private LiveStreamConfig liveStreamConfig;
}

