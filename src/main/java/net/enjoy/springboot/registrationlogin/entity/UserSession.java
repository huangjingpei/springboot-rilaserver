package net.enjoy.springboot.registrationlogin.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_session")
@Data
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String deviceId;

    private LocalDateTime loginTime;

    private LocalDateTime logoutTime;

    private String status; // online/offline
}