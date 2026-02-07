package net.enjoy.springboot.registrationlogin.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class DynamicMaxSessionAuthenticationStrategy implements SessionAuthenticationStrategy {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response)
            throws SessionAuthenticationException {
        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new SessionAuthenticationException("用户不存在");
        }
        int maxDevices = user.getMaxDevices();
        List<?> sessions = sessionRegistry.getAllSessions(authentication.getPrincipal(), false);
        if (sessions.size() >= maxDevices) {
            throw new SessionAuthenticationException("已达最大在线设备数限制：" + maxDevices + "台。请先在其他设备上登出");
        }
    }
}
