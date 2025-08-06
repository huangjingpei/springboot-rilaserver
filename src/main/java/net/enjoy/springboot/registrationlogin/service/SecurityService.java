package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.entity.AuditLog;
import net.enjoy.springboot.registrationlogin.entity.LoginAttempt;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.entity.UserDevice;

import java.util.List;

public interface SecurityService {
    
    // 登录尝试管理
    void recordLoginAttempt(String userId, String ipAddress, String userAgent, boolean success, String failureReason);
    boolean isAccountLocked(String userId);
    boolean isIpBlocked(String ipAddress);
    void resetFailedAttempts(String userId);
    
    // 密码策略
    boolean validatePassword(String password);
    boolean isPasswordExpired(User user);
    void updatePassword(User user, String newPassword);
    
    // 设备管理
    List<UserDevice> getUserDevices(String userId);
    UserDevice registerDevice(String userId, String deviceId, String deviceName, String deviceType, String ipAddress, String userAgent);
    void deactivateDevice(String userId, String deviceId);
    boolean canLoginWithDevice(String userId, String deviceId);
    
    // 审计日志
    void logAction(String userId, String action, String resource, String ipAddress, String userAgent, String details, String status);
    List<AuditLog> getUserAuditLogs(String userId, int page, int size);
    List<AuditLog> getAuditLogsByAction(String action, int page, int size);
    
    // 安全检查
    boolean performSecurityChecks(String userId, String ipAddress);
    
    // 用户状态检查
    boolean checkUserStatus(String userId);
    
    // 综合安全检查
    boolean performCompleteSecurityCheck(String userId, String ipAddress);
} 