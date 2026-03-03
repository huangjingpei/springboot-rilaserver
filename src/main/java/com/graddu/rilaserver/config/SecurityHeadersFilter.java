package com.graddu.rilaserver.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter implements Filter {

    @Autowired
    private SecurityConfig securityConfig;
    
    @Value("${security.enable-hsts:false}")
    private boolean enableHsts;
    
    @Value("${security.enable-xss-protection:true}")
    private boolean enableXssProtection;
    
    @Value("${security.enable-content-type-options:true}")
    private boolean enableContentTypeOptions;
    
    @Value("${security.enable-frame-options:true}")
    private boolean enableFrameOptions;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 内容安全策略
        httpResponse.setHeader("Content-Security-Policy", securityConfig.getContentSecurityPolicy());
        
        // XSS保护
        if (enableXssProtection) {
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        }
        
        // 内容类型选项
        if (enableContentTypeOptions) {
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        }
        
        // 框架选项 - 本地开发时允许同源嵌入
        if (enableFrameOptions) {
            // 本地开发环境允许同源嵌入，生产环境应该设置为DENY
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
        }
        
        // HSTS - 只在HTTPS环境下启用
        if (enableHsts && "https".equalsIgnoreCase(httpRequest.getScheme())) {
            httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        
        // 引用策略
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // 权限策略
        httpResponse.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        
        // 缓存控制 - 本地开发时允许缓存
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setHeader("Expires", "0");

        chain.doFilter(request, response);
    }
} 