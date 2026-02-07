package net.enjoy.springboot.registrationlogin.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.enjoy.springboot.registrationlogin.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 白名单路径（无需 token）
        String path = request.getRequestURI();
        System.out.println("JWT过滤器处理请求: " + path + " [" + request.getMethod() + "]");
        
        if (path.startsWith("/api/v2/auth/login") ||
            path.startsWith("/api/v2/auth/register") ||
            path.startsWith("/api/login") ||
            path.startsWith("/api/register") ||
            path.startsWith("/api/admin/") ||
            path.startsWith("/api/apps/") ||  // 应用商城API
            path.startsWith("/api/smartcdn/client/register") || // SmartCDN客户端注册 - 允许未登录访问
            path.startsWith("/api/v1/updates/") ||  // 升级系统API - 允许公开访问
            path.equals("/api/v1/proxy-config/health") ||  // 代理客户端配置健康检查 - 允许公开访问
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/swagger-ui.html") ||
            path.startsWith("/register") ||
            path.equals("/register-api.html") ||
            path.equals("/register-simple.html") ||
            path.equals("/login-api.html") ||
            path.equals("/api-test.html") ||
            path.equals("/user-type-test.html") ||
            path.equals("/app-store.html") ||  // 应用商城页面
            path.equals("/app-store-test.html") ||  // 应用商城测试页面
            path.equals("/proxy-config-test.html") ||  // 代理客户端配置测试页面
            path.equals("/test.html") ||
            path.equals("/stream-test.html") ||
            path.equals("/stream-notification-test.html") ||
            path.equals("/index") ||
            path.equals("/login") ||
            path.equals("/form-login") ||  // 添加登录页面路径
            path.equals("/users") ||
            // 新增静态资源和WebSocket端点
            path.equals("/websocket-test.html") ||
            path.equals("/favicon.ico") ||
            path.startsWith("/js/") ||
            path.startsWith("/css/") ||
            path.startsWith("/images/") ||
            path.startsWith("/ws") ||
            path.startsWith("/sockjs-node/") ||
            // 临时允许zlm接口用于测试
            path.startsWith("/zlm/") ||
            // 允许hook接口
            path.startsWith("/index/hook/") ||
            // 允许所有HTML文件
            path.endsWith(".html")
        ) {
            System.out.println("JWT过滤器: 路径 " + path + " 在白名单中，直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("JWT过滤器: 路径 " + path + " 不在白名单中，进行JWT验证");

        String header = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 先从header解析token
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            // 校验token
            if (jwtUtil.validateToken(token)) {
                username = jwtUtil.getUsernameFromToken(token);
            } else {
                // token无效
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized");
                return;
            }
        } else {
            // 没有token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return;
        }

        // 设置Spring Security上下文
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/qwebchannel.js") || 
               path.startsWith("/static/") ||
               path.endsWith(".html") ||
               path.startsWith("/api/login") ||
               path.startsWith("/api/register") ||
               path.startsWith("/api/apps/") ||  // 应用商城API
               path.startsWith("/api/v1/updates/") ||  // 升级系统API - 允许公开访问
               path.equals("/form-login") ||  // 添加登录页面路径
               path.startsWith("/ws");  // 添加WebSocket路径排除
    }
}