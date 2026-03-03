package com.graddu.rilaserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "live_stream_task_logs")
@Data
public class LiveStreamTaskLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    @Column(name = "client_id", nullable = false)
    private String clientId;
    
    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType; // CREATE, UPDATE, DELETE, CHECK
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // SUCCESS, FAILED, RUNNING
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;
    
    public enum OperationType {
        CREATE, UPDATE, DELETE, CHECK
    }
    
    public enum Status {
        SUCCESS, FAILED, RUNNING
    }
}
