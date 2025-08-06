package net.enjoy.springboot.registrationlogin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.enjoy.springboot.registrationlogin.entity.Package;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId; // 手机号/授权码

    @Column(unique = true)
    private String phone; // 手机号码

    private String name; // 用户姓名

    private String password; // 可为空（授权码类）

    private String type; // register/auth/enterprise

    private Integer maxDevices; // 最大设备数
    
    private Integer maxStreams; // 最大推流数

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE; // 用户状态

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0; // 登录失败次数

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil; // 账户锁定时间

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt; // 密码修改时间

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt; // 最后登录时间

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 创建时间

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_roles",
            joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "ROLE_ID", referencedColumnName = "ID")})
    private List<Role> roles = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "package_id")
    private Package userPackage;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserPackage> userPackages = new ArrayList<>();
}