package net.enjoy.springboot.registrationlogin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityConfig {
    
    // 登录失败限制
    private int maxLoginAttempts = 5;
    private int lockoutDurationMinutes = 30;
    
    // 密码策略
    private int minPasswordLength = 8;
    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireNumbers = true;
    private boolean requireSpecialChars = false;
    private int passwordExpiryDays = 90;
    
    // IP限制
    private int maxFailedAttemptsPerIp = 10;
    private int ipLockoutDurationMinutes = 60;
    
    // 会话管理
    private int sessionTimeoutMinutes = 30;
    private boolean allowMultipleSessions = true;
    
    // 内容安全策略
    private String contentSecurityPolicy = "default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; font-src 'self' https://cdnjs.cloudflare.com; img-src 'self' data:; connect-src 'self'; frame-ancestors 'none';";
    
    // 安全头配置
    private boolean enableHsts = true;
    private boolean enableXssProtection = true;
    private boolean enableContentTypeOptions = true;
    private boolean enableFrameOptions = true;

    // Getters and Setters
    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public void setMaxLoginAttempts(int maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }

    public void setLockoutDurationMinutes(int lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    public int getMinPasswordLength() {
        return minPasswordLength;
    }

    public void setMinPasswordLength(int minPasswordLength) {
        this.minPasswordLength = minPasswordLength;
    }

    public boolean isRequireUppercase() {
        return requireUppercase;
    }

    public void setRequireUppercase(boolean requireUppercase) {
        this.requireUppercase = requireUppercase;
    }

    public boolean isRequireLowercase() {
        return requireLowercase;
    }

    public void setRequireLowercase(boolean requireLowercase) {
        this.requireLowercase = requireLowercase;
    }

    public boolean isRequireNumbers() {
        return requireNumbers;
    }

    public void setRequireNumbers(boolean requireNumbers) {
        this.requireNumbers = requireNumbers;
    }

    public boolean isRequireSpecialChars() {
        return requireSpecialChars;
    }

    public void setRequireSpecialChars(boolean requireSpecialChars) {
        this.requireSpecialChars = requireSpecialChars;
    }

    public int getPasswordExpiryDays() {
        return passwordExpiryDays;
    }

    public void setPasswordExpiryDays(int passwordExpiryDays) {
        this.passwordExpiryDays = passwordExpiryDays;
    }

    public int getMaxFailedAttemptsPerIp() {
        return maxFailedAttemptsPerIp;
    }

    public void setMaxFailedAttemptsPerIp(int maxFailedAttemptsPerIp) {
        this.maxFailedAttemptsPerIp = maxFailedAttemptsPerIp;
    }

    public int getIpLockoutDurationMinutes() {
        return ipLockoutDurationMinutes;
    }

    public void setIpLockoutDurationMinutes(int ipLockoutDurationMinutes) {
        this.ipLockoutDurationMinutes = ipLockoutDurationMinutes;
    }

    public int getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public boolean isAllowMultipleSessions() {
        return allowMultipleSessions;
    }

    public void setAllowMultipleSessions(boolean allowMultipleSessions) {
        this.allowMultipleSessions = allowMultipleSessions;
    }

    public String getContentSecurityPolicy() {
        return contentSecurityPolicy;
    }

    public void setContentSecurityPolicy(String contentSecurityPolicy) {
        this.contentSecurityPolicy = contentSecurityPolicy;
    }

    public boolean isEnableHsts() {
        return enableHsts;
    }

    public void setEnableHsts(boolean enableHsts) {
        this.enableHsts = enableHsts;
    }

    public boolean isEnableXssProtection() {
        return enableXssProtection;
    }

    public void setEnableXssProtection(boolean enableXssProtection) {
        this.enableXssProtection = enableXssProtection;
    }

    public boolean isEnableContentTypeOptions() {
        return enableContentTypeOptions;
    }

    public void setEnableContentTypeOptions(boolean enableContentTypeOptions) {
        this.enableContentTypeOptions = enableContentTypeOptions;
    }

    public boolean isEnableFrameOptions() {
        return enableFrameOptions;
    }

    public void setEnableFrameOptions(boolean enableFrameOptions) {
        this.enableFrameOptions = enableFrameOptions;
    }
}