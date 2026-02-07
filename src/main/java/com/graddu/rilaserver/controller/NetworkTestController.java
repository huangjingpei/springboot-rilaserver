package net.enjoy.springboot.registrationlogin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络测试控制器 - 用于诊断网络连接问题
 */
@RestController
@RequestMapping("/test")
public class NetworkTestController {
    
    @GetMapping("/ping")
    public Map<String, Object> ping(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 记录访问日志
            System.out.println("=== 网络访问日志 ===");
            System.out.println("访问时间: " + new java.util.Date());
            System.out.println("客户端IP: " + getClientIpAddress(request));
            System.out.println("请求URL: " + request.getRequestURL());
            System.out.println("User-Agent: " + request.getHeader("User-Agent"));
            System.out.println("==================");
            
            // 获取服务器信息
            result.put("server_ip", InetAddress.getLocalHost().getHostAddress());
            result.put("server_name", InetAddress.getLocalHost().getHostName());
            
            // 获取客户端信息
            result.put("client_ip", getClientIpAddress(request));
            result.put("user_agent", request.getHeader("User-Agent"));
            result.put("request_url", request.getRequestURL().toString());
            
            // 获取所有网络接口
            result.put("all_interfaces", getAllNetworkInterfaces());
            
            result.put("status", "success");
            result.put("message", "网络连接正常");
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "获取网络信息失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("/simple")
    public String simple(HttpServletRequest request) {
        try {
            String clientIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            String requestUrl = request.getRequestURL().toString();
            
            // 详细日志
            System.out.println("=== Simple API 访问 ===");
            System.out.println("时间: " + new java.util.Date());
            System.out.println("客户端IP: " + clientIp);
            System.out.println("请求URL: " + requestUrl);
            System.out.println("User-Agent: " + userAgent);
            System.out.println("请求方法: " + request.getMethod());
            System.out.println("协议: " + request.getProtocol());
            System.out.println("====================");
            
            return "OK - 网络连接正常 - 客户端IP: " + clientIp + " - 时间: " + new java.util.Date();
        } catch (Exception e) {
            System.err.println("Simple API 处理异常: " + e.getMessage());
            e.printStackTrace();
            return "ERROR - 处理请求时发生异常: " + e.getMessage();
        }
    }
    
    @GetMapping("/hello")
    public String hello() {
        System.out.println("Hello接口被访问了！时间: " + new java.util.Date());
        return "Hello World!";
    }
    
    @GetMapping("")
    public String root() {
        System.out.println("根路径被访问了！时间: " + new java.util.Date());
        return "Network Test Controller - Root Path";
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
    
    private Map<String, String> getAllNetworkInterfaces() {
        Map<String, String> interfaces = new HashMap<>();
        try {
            java.util.Enumeration<java.net.NetworkInterface> networkInterfaces = 
                java.net.NetworkInterface.getNetworkInterfaces();
            
            while (networkInterfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                    java.util.Enumeration<java.net.InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        java.net.InetAddress address = addresses.nextElement();
                        if (address instanceof java.net.Inet4Address) {
                            interfaces.put(networkInterface.getName(), address.getHostAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            interfaces.put("error", e.getMessage());
        }
        return interfaces;
    }
} 