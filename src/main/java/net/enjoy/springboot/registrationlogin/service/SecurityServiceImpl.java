package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.config.SecurityConfig;
import net.enjoy.springboot.registrationlogin.entity.AuditLog;
import net.enjoy.springboot.registrationlogin.entity.LoginAttempt;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.entity.UserDevice;
import net.enjoy.springboot.registrationlogin.entity.UserStatus;
import net.enjoy.springboot.registrationlogin.repository.AuditLogRepository;
import net.enjoy.springboot.registrationlogin.repository.LoginAttemptRepository;
import net.enjoy.springboot.registrationlogin.repository.UserDeviceRepository;
import net.enjoy.springboot.registrationlogin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private SecurityConfig securityConfig;
    
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserDeviceRepository userDeviceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void recordLoginAttempt(String userId, String ipAddress, String userAgent, boolean success, String failureReason) {
        LoginAttempt attempt = new LoginAttempt(userId, ipAddress, userAgent, success, failureReason);
        loginAttemptRepository.save(attempt);
        
        if (!success) {
            User user = userRepository.findByUserId(userId);
            if (user != null) {
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                
                if (user.getFailedLoginAttempts() >= securityConfig.getMaxLoginAttempts()) {
                    user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(securityConfig.getLockoutDurationMinutes()));
                    user.setStatus(UserStatus.SUSPENDED);
                }
                
                userRepository.save(user);
            }
        }
    }

    @Override
    public boolean isAccountLocked(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) return false;
        
        if (user.getAccountLockedUntil() != null && LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
            return true;
        }
        
        // 如果锁定时间已过，重置失败次数
        if (user.getAccountLockedUntil() != null && LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
            resetFailedAttempts(userId);
        }
        
        return false;
    }

    @Override
    public boolean isIpBlocked(String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(securityConfig.getIpLockoutDurationMinutes());
        List<LoginAttempt> failedAttempts = loginAttemptRepository.findFailedAttemptsByIpSince(ipAddress, since);
        return failedAttempts.size() >= securityConfig.getMaxFailedAttemptsPerIp();
    }

    @Override
    public void resetFailedAttempts(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user != null) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            if (user.getStatus() == UserStatus.SUSPENDED) {
                user.setStatus(UserStatus.ACTIVE);
            }
            userRepository.save(user);
        }
    }

    @Override
    public boolean validatePassword(String password) {
        if (password == null || password.length() < securityConfig.getMinPasswordLength()) {
            return false;
        }
        
        if (securityConfig.isRequireUppercase() && !password.matches(".*[A-Z].*")) {
            return false;
        }
        
        if (securityConfig.isRequireLowercase() && !password.matches(".*[a-z].*")) {
            return false;
        }
        
        if (securityConfig.isRequireNumbers() && !password.matches(".*\\d.*")) {
            return false;
        }
        
        if (securityConfig.isRequireSpecialChars() && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean isPasswordExpired(User user) {
        if (user.getPasswordChangedAt() == null) {
            return false; // 新用户，密码未过期
        }
        
        LocalDateTime expiryDate = user.getPasswordChangedAt().plusDays(securityConfig.getPasswordExpiryDays());
        return LocalDateTime.now().isAfter(expiryDate);
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public List<UserDevice> getUserDevices(String userId) {
        return userDeviceRepository.findActiveDevicesByUserId(userId);
    }

    @Override
    public UserDevice registerDevice(String userId, String deviceId, String deviceName, String deviceType, String ipAddress, String userAgent) {
        // 检查是否已存在该设备
        UserDevice existingDevice = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId).orElse(null);
        
        if (existingDevice != null) {
            // 更新现有设备信息
            existingDevice.setLastLoginTime(LocalDateTime.now());
            existingDevice.setIpAddress(ipAddress);
            existingDevice.setUserAgent(userAgent);
            existingDevice.setActive(true);
            return userDeviceRepository.save(existingDevice);
        } else {
            // 创建新设备记录
            UserDevice newDevice = new UserDevice(userId, deviceId, deviceName, deviceType, ipAddress, userAgent);
            return userDeviceRepository.save(newDevice);
        }
    }

    @Override
    public void deactivateDevice(String userId, String deviceId) {
        UserDevice device = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId).orElse(null);
        if (device != null) {
            device.setActive(false);
            userDeviceRepository.save(device);
        }
    }

    @Override
    public boolean canLoginWithDevice(String userId, String deviceId) {
        // 检查设备是否已被该用户注册
        UserDevice device = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId).orElse(null);
        return device != null && device.isActive();
    }

    @Override
    public void logAction(String userId, String action, String resource, String ipAddress, String userAgent, String details, String status) {
        AuditLog log = new AuditLog(userId, action, resource, ipAddress, userAgent, details, status);
        auditLogRepository.save(log);
    }

    @Override
    public List<AuditLog> getUserAuditLogs(String userId, int page, int size) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, PageRequest.of(page, size)).getContent();
    }

    @Override
    public List<AuditLog> getAuditLogsByAction(String action, int page, int size) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, PageRequest.of(page, size)).getContent();
    }

    @Override
    public boolean performSecurityChecks(String userId, String ipAddress) {
        // 检查账户是否被锁定
        if (isAccountLocked(userId)) {
            return false;
        }
        
        // 检查IP是否被阻止
        if (isIpBlocked(ipAddress)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查用户基本状态（在登录流程早期调用）
     */
    public boolean checkUserStatus(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            return false;
        }
        
        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 综合安全检查（包含用户状态）
     */
    public boolean performCompleteSecurityCheck(String userId, String ipAddress) {
        // 1. 检查用户状态
        if (!checkUserStatus(userId)) {
            return false;
        }
        
        // 2. 执行安全检查
        return performSecurityChecks(userId, ipAddress);
    }
} 