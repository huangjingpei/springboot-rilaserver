package com.graddu.rilaserver.service;

import com.graddu.rilaserver.entity.User;

/**
 * 推流限制服务接口
 */
public interface StreamLimitService {
    
    /**
     * 检查用户是否可以开始新的推流
     * @param userId 用户ID
     * @return 是否可以推流
     */
    boolean canUserStartStream(String userId);
    
    /**
     * 检查用户是否可以开始新的推流（详细信息）
     * @param userId 用户ID
     * @return 推流限制检查结果
     */
    StreamLimitCheckResult checkStreamLimit(String userId);
    
    /**
     * 获取用户当前推流数量
     * @param userId 用户ID
     * @return 当前推流数量
     */
    int getCurrentStreamCount(String userId);
    
    /**
     * 获取用户的最大推流数限制
     * @param userId 用户ID
     * @return 最大推流数
     */
    int getMaxStreamCount(String userId);
    
    /**
     * 推流限制检查结果
     */
    class StreamLimitCheckResult {
        private boolean allowed;
        private String message;
        private String code;
        private int currentStreams;
        private int maxStreams;
        
        public StreamLimitCheckResult(boolean allowed, String message, String code, int currentStreams, int maxStreams) {
            this.allowed = allowed;
            this.message = message;
            this.code = code;
            this.currentStreams = currentStreams;
            this.maxStreams = maxStreams;
        }
        
        // Getters
        public boolean isAllowed() { return allowed; }
        public String getMessage() { return message; }
        public String getCode() { return code; }
        public int getCurrentStreams() { return currentStreams; }
        public int getMaxStreams() { return maxStreams; }
        
        // 静态方法
        public static StreamLimitCheckResult allowed(int currentStreams, int maxStreams) {
            return new StreamLimitCheckResult(true, "允许推流", "ALLOWED", currentStreams, maxStreams);
        }
        
        public static StreamLimitCheckResult limitExceeded(int currentStreams, int maxStreams) {
            return new StreamLimitCheckResult(false, 
                String.format("已达最大推流数限制：%d/%d。请先停止其他推流", currentStreams, maxStreams), 
                "STREAM_LIMIT_EXCEEDED", currentStreams, maxStreams);
        }
        
        public static StreamLimitCheckResult userNotFound() {
            return new StreamLimitCheckResult(false, "用户不存在", "USER_NOT_FOUND", 0, 0);
        }
    }
} 