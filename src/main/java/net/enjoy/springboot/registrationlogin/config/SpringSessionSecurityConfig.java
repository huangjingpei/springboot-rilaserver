package net.enjoy.springboot.registrationlogin.config;

import net.enjoy.springboot.registrationlogin.security.CustomUserDetailsService;
import net.enjoy.springboot.registrationlogin.utils.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SpringSessionSecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 明确指定允许的源，包括qrc:协议和生产环境地址
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:8080", 
            "http://127.0.0.1:8080",
            "http://192.168.3.4:8080",  // 你的测试地址
            "http://125.122.157.97:8081",  // 生产环境地址
            "https://125.122.157.97:8081", // HTTPS版本
            "qrc:*",  // 允许所有qrc:协议的源
            "*"       // 允许所有源（开发环境使用）
        ));
        // 允许所有HTTP方法 (GET, POST, PUT, DELETE, etc.)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许所有请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允许浏览器发送凭据 (例如 cookies)
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用这个CORS配置
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                // 移除maximumSessions配置，使用自定义业务逻辑控制设备数
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api/v2/auth/**",
                    "/api/register", 
                    "/api/login", 
                    "/api/user/status/**", 
                    "/api/user/unlock/**",
                    "/api/admin/**",
                    "/api/apps/**",  // 应用商城API
                    "/api/stream-limits/**", // 推流限制API
                    "/api/v1/updates/**",  // 升级系统API - 允许公开访问
                    "/api/v1/proxy-config/health",  // 代理客户端配置健康检查 - 允许公开访问
                    "/proxy-config-test.html",  // 代理客户端配置测试页面
                    "/register/**",
                    "/register-api.html",
                    "/register-simple.html",
                    "/login-api.html",
                    "/api-test.html",
                    "/user-type-test.html",
                    "/app-store.html",  // 应用商城页面
                    "/app-store-test.html",  // 应用商城测试页面
                    "/stream-limit-test.html", // 推流限制测试页面
                    "/update-test.html", // 升级测试页面
                    "/login",
                    "/form-login",  // 添加登录页面路径
                    "/index",
                    "/users",
                    "/websocket-test.html",
                    "/stream-test.html",
                    "/stream-notification-test.html",
                    "/favicon.ico",
                    "/js/**",
                    "/css/**",
                    "/images/**",
                    "/ws/**",
                    "/sockjs-node/**",
                    "/static/**", 
                    "/templates/**",
                    "/index/hook/**",
                    "/zlm/**",
                    "/qwebchannel.js",
                    "/*.html"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/form-login")        // 改为 /form-login
                .loginProcessingUrl("/form-login") // 改为 /form-login
                .defaultSuccessUrl("/index")
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }
}