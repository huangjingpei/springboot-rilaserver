package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpringSessionService {

    @Autowired
    private SessionRegistry sessionRegistry;

    /**
     * 获取用户当前活跃会话数
     */
    public int getActiveSessionCount(String userId) {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal.toString().equals(userId))
                .mapToInt(principal -> sessionRegistry.getAllSessions(principal, false).size())
                .sum();
    }

    /**
     * 获取用户所有活跃会话信息
     */
    public List<org.springframework.security.core.session.SessionInformation> getUserActiveSessions(String userId) {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal.toString().equals(userId))
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否可以登录（基于会话数限制）
     */
    public boolean canUserLogin(User user) {
        int currentSessions = getActiveSessionCount(user.getUserId());
        return currentSessions < user.getMaxDevices();
    }

    /**
     * 强制踢出用户的所有会话
     */
    public void forceLogoutUser(String userId) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal.toString().equals(userId))
                .forEach(principal -> {
                    sessionRegistry.getAllSessions(principal, false).forEach(sessionInformation -> {
                        sessionInformation.expireNow();
                    });
                });
    }

    /**
     * 踢出指定会话
     */
    public void forceLogoutSession(String sessionId) {
        sessionRegistry.getAllPrincipals().stream()
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .filter(sessionInfo -> sessionInfo.getSessionId().equals(sessionId))
                .findFirst()
                .ifPresent(sessionInfo -> sessionInfo.expireNow());
    }

    /**
     * 获取会话信息
     */
    public org.springframework.security.core.session.SessionInformation getSession(String sessionId) {
        return sessionRegistry.getAllPrincipals().stream()
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .filter(sessionInfo -> sessionInfo.getSessionId().equals(sessionId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取用户会话统计信息
     */
    public UserSessionStats getUserSessionStats(String userId) {
        List<org.springframework.security.core.session.SessionInformation> sessions = getUserActiveSessions(userId);
        List<SessionInfo> sessionInfos = sessions.stream()
                .map(session -> new SessionInfo(
                    session.getSessionId(),
                    session.getLastRequest().toInstant(),
                    session.getPrincipal().toString()
                ))
                .collect(Collectors.toList());
        
        return new UserSessionStats(userId, sessions.size(), sessionInfos);
    }

    /**
     * 会话统计信息
     */
    public static class UserSessionStats {
        private String userId;
        private int activeSessionCount;
        private List<SessionInfo> sessions;

        public UserSessionStats(String userId, int activeSessionCount, List<SessionInfo> sessions) {
            this.userId = userId;
            this.activeSessionCount = activeSessionCount;
            this.sessions = sessions;
        }

        // Getters
        public String getUserId() { return userId; }
        public int getActiveSessionCount() { return activeSessionCount; }
        public List<SessionInfo> getSessions() { return sessions; }
    }

    /**
     * 会话信息
     */
    public static class SessionInfo {
        private String sessionId;
        private java.time.Instant lastRequest;
        private String principal;

        public SessionInfo(String sessionId, java.time.Instant lastRequest, String principal) {
            this.sessionId = sessionId;
            this.lastRequest = lastRequest;
            this.principal = principal;
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public java.time.Instant getLastRequest() { return lastRequest; }
        public String getPrincipal() { return principal; }
    }
} 