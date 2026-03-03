package com.graddu.rilaserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.graddu.rilaserver.dto.ProxyClientConfigDto;
import com.graddu.rilaserver.service.ProxyClientConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/proxy-config")
@RequiredArgsConstructor
@Slf4j
public class ProxyClientConfigController {

    private final ProxyClientConfigService proxyClientConfigService;

    /**
     * 保存或更新代理客户端配置
     * POST /api/v1/proxy-config
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ProxyClientConfigDto> saveOrUpdateConfig(
            @Valid @RequestBody ProxyClientConfigDto configDto) {
        
        log.info("收到保存/更新代理客户端配置请求: userId={}", configDto.getUserId());
        
        try {
            ProxyClientConfigDto savedConfig = proxyClientConfigService.saveOrUpdateConfig(configDto);
            log.info("配置保存/更新成功: userId={}", configDto.getUserId());
            return ResponseEntity.ok(savedConfig);
        } catch (Exception e) {
            log.error("配置保存/更新失败: userId={}", configDto.getUserId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据用户ID获取代理客户端配置
     * GET /api/v1/proxy-config/{userId}
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ProxyClientConfigDto> getConfigByUserId(@PathVariable String userId) {
        
        log.info("收到获取代理客户端配置请求: userId={}", userId);
        
        try {
            ProxyClientConfigDto config = proxyClientConfigService.getConfigByUserId(userId);
            if (config != null) {
                log.info("配置获取成功: userId={}", userId);
                return ResponseEntity.ok(config);
            } else {
                log.warn("配置不存在: userId={}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("配置获取失败: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据用户ID删除代理客户端配置
     * DELETE /api/v1/proxy-config/{userId}
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConfigByUserId(@PathVariable String userId) {
        
        log.info("收到删除代理客户端配置请求: userId={}", userId);
        
        try {
            proxyClientConfigService.deleteConfigByUserId(userId);
            log.info("配置删除成功: userId={}", userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("配置删除失败: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 健康检查接口
     * GET /api/v1/proxy-config/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Proxy Client Config Service is running");
    }
}
