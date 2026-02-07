package net.enjoy.springboot.registrationlogin.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    @Value("${security.local-rate-limit-enabled:true}")
    private boolean localRateLimitEnabled;
    
    @Value("${security.local-rate-limit-requests-per-minute:60}")
    private int localRequestsPerMinute;
    
    @Value("${security.local-rate-limit-requests-per-hour:1000}")
    private int localRequestsPerHour;
    
    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return buckets;
    }
    
    /**
     * 为指定IP创建或获取速率限制桶
     */
    public Bucket resolveBucket(String ipAddress) {
        return buckets.computeIfAbsent(ipAddress, this::newBucket);
    }
    
    /**
     * 创建新的速率限制桶
     * 本地开发环境：更宽松的限制
     * 生产环境：更严格的限制
     */
    private Bucket newBucket(String ipAddress) {
        // 检查是否为本地开发环境
        if (isLocalDevelopment(ipAddress)) {
            // 本地开发环境，更宽松的限制
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(localRequestsPerMinute, Refill.intervally(localRequestsPerMinute, Duration.ofMinutes(1))))
                    .addLimit(Bandwidth.classic(localRequestsPerHour, Refill.intervally(localRequestsPerHour, Duration.ofHours(1))))
                    .build();
        } else {
            // 生产环境，更严格的限制
            if (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || ipAddress.equals("127.0.0.1")) {
                // 内网IP，中等限制
                return Bucket.builder()
                        .addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1))))
                        .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofHours(1))))
                        .build();
            } else {
                // 外网IP，严格限制
                return Bucket.builder()
                        .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                        .addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofHours(1))))
                        .build();
            }
        }
    }
    
    /**
     * 判断是否为本地开发环境
     */
    private boolean isLocalDevelopment(String ipAddress) {
        return localRateLimitEnabled && (
            ipAddress.equals("127.0.0.1") || 
            ipAddress.equals("localhost") || 
            ipAddress.equals("::1") ||
            ipAddress.startsWith("192.168.") ||
            ipAddress.startsWith("10.")
        );
    }
    
    /**
     * 清理过期的桶
     */
    public void cleanupBuckets() {
        // 这里可以添加清理逻辑，比如定期清理长时间未使用的桶
    }
} 