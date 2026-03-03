package com.graddu.rilaserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.graddu.rilaserver.dto.StreamFetchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PythonScriptService {
    
    @Value("${app.python.script.path:scripts/fetch_douyin_stream.py}")
    private String scriptPath;
    
    @Value("${app.python.executable:python}")
    private String pythonExecutable;
    
    @Value("${app.python.timeout:30}")
    private int timeoutSeconds;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 执行Python脚本获取直播流信息
     * 
     * @param platform 平台名称 (douyin, kuaishou, bilibili)
     * @param liveUrl 直播间地址
     * @param quality 清晰度 (HD, OD)
     * @return 异步执行结果
     */
    public CompletableFuture<StreamFetchResult> fetchStreamAsync(String platform, String liveUrl, String quality) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchStream(platform, liveUrl, quality);
            } catch (Exception e) {
                log.error("异步执行Python脚本失败: platform={}, url={}, quality={}", platform, liveUrl, quality, e);
                return createErrorResult(e.getMessage());
            }
        });
    }
    
    /**
     * 同步执行Python脚本获取直播流信息
     */
    public StreamFetchResult fetchStream(String platform, String liveUrl, String quality) {
        log.info("开始执行Python脚本: platform={}, url={}, quality={}", platform, liveUrl, quality);
        
        // 检查脚本文件是否存在
        Path scriptFilePath = Paths.get(scriptPath);
        if (!Files.exists(scriptFilePath)) {
            String errorMsg = "Python脚本文件不存在: " + scriptPath;
            log.error(errorMsg);
            return createErrorResult(errorMsg);
        }
        
        // 构建命令
        String[] command = {
            pythonExecutable,
            scriptFilePath.toString(),
            platform,
            liveUrl,
            quality
        };
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        
        try {
            Process process = processBuilder.start();
            
            // 设置超时
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                String errorMsg = "Python脚本执行超时: " + timeoutSeconds + "秒";
                log.error(errorMsg);
                return createErrorResult(errorMsg);
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String errorMsg = "Python脚本执行失败，退出码: " + exitCode;
                log.error(errorMsg);
                return createErrorResult(errorMsg);
            }
            
            // 读取输出
            String output = readProcessOutput(process);
            log.debug("Python脚本输出: {}", output);
            
            // 解析JSON结果
            try {
                StreamFetchResult result = objectMapper.readValue(output, StreamFetchResult.class);
                log.info("Python脚本执行成功: platform={}, url={}, isLive={}", 
                    platform, liveUrl, result.getData() != null ? result.getData().getIs_live() : null);
                return result;
            } catch (Exception e) {
                String errorMsg = "解析Python脚本输出失败: " + e.getMessage();
                log.error(errorMsg + ", 原始输出: {}", output, e);
                return createErrorResult(errorMsg);
            }
            
        } catch (IOException | InterruptedException e) {
            String errorMsg = "执行Python脚本时发生异常: " + e.getMessage();
            log.error(errorMsg, e);
            return createErrorResult(errorMsg);
        }
    }
    
    /**
     * 读取进程输出
     */
    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }
    
    /**
     * 创建错误结果
     */
    private StreamFetchResult createErrorResult(String errorMessage) {
        StreamFetchResult result = new StreamFetchResult();
        result.setCode(-1);
        result.setMessage(errorMessage);
        return result;
    }
    
    /**
     * 检查Python环境
     */
    public boolean checkPythonEnvironment() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, "--version");
            Process process = processBuilder.start();
            boolean completed = process.waitFor(10, TimeUnit.SECONDS);
            
            if (completed && process.exitValue() == 0) {
                String version = readProcessOutput(process);
                log.info("Python环境检查成功: {}", version);
                return true;
            } else {
                log.error("Python环境检查失败: 无法执行python命令");
                return false;
            }
        } catch (Exception e) {
            log.error("Python环境检查异常", e);
            return false;
        }
    }
    
    /**
     * 检查脚本文件
     */
    public boolean checkScriptFile() {
        Path scriptFilePath = Paths.get(scriptPath);
        boolean exists = Files.exists(scriptFilePath);
        if (exists) {
            log.info("Python脚本文件存在: {}", scriptPath);
        } else {
            log.error("Python脚本文件不存在: {}", scriptPath);
        }
        return exists;
    }
}

