package com.graddu.rilaserver.controller;

import com.graddu.rilaserver.entity.AuditLog;
import com.graddu.rilaserver.entity.UserDevice;
import com.graddu.rilaserver.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    private SecurityService securityService;

    /**
     * 获取用户设备列表
     */
    @GetMapping("/devices/{userId}")
    public ResponseEntity<List<UserDevice>> getUserDevices(@PathVariable String userId) {
        List<UserDevice> devices = securityService.getUserDevices(userId);
        return ResponseEntity.ok(devices);
    }

    /**
     * 停用设备
     */
    @DeleteMapping("/devices/{userId}/{deviceId}")
    public ResponseEntity<String> deactivateDevice(@PathVariable String userId, @PathVariable String deviceId) {
        securityService.deactivateDevice(userId, deviceId);
        return ResponseEntity.ok("设备已停用");
    }

    /**
     * 获取用户审计日志
     */
    @GetMapping("/audit/{userId}")
    public ResponseEntity<List<AuditLog>> getUserAuditLogs(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<AuditLog> logs = securityService.getUserAuditLogs(userId, page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据操作类型获取审计日志
     */
    @GetMapping("/audit/action/{action}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<AuditLog> logs = securityService.getAuditLogsByAction(action, page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * 检查账户是否被锁定
     */
    @GetMapping("/check-lock/{userId}")
    public ResponseEntity<Boolean> isAccountLocked(@PathVariable String userId) {
        boolean isLocked = securityService.isAccountLocked(userId);
        return ResponseEntity.ok(isLocked);
    }

    /**
     * 检查IP是否被阻止
     */
    @GetMapping("/check-ip/{ipAddress}")
    public ResponseEntity<Boolean> isIpBlocked(@PathVariable String ipAddress) {
        boolean isBlocked = securityService.isIpBlocked(ipAddress);
        return ResponseEntity.ok(isBlocked);
    }

    /**
     * 重置用户登录失败次数
     */
    @PostMapping("/reset-attempts/{userId}")
    public ResponseEntity<String> resetFailedAttempts(@PathVariable String userId) {
        securityService.resetFailedAttempts(userId);
        return ResponseEntity.ok("登录失败次数已重置");
    }
} 