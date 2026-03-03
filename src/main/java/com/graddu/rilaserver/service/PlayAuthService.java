package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.PlayAuthResult;

/**
 * 播放鉴权服务接口
 * 用于验证用户是否有权限播放指定的流
 */
public interface PlayAuthService {
    
    /**
     * 验证播放权限
     * @param streamId 流ID
     * @param userId 用户ID（可选）
     * @param token 播放令牌（可选）
     * @param ip 客户端IP地址
     * @param protocol 播放协议（rtmp/hls/flv等）
     * @return 鉴权结果
     */
    PlayAuthResult verifyPlayAuth(String streamId, String userId, String token, String ip, String protocol);
    
    /**
     * 生成播放令牌
     * @param streamId 流ID
     * @param userId 用户ID
     * @param expireSeconds 过期时间（秒）
     * @return 播放令牌
     */
    String generatePlayToken(String streamId, String userId, long expireSeconds);
    
    /**
     * 验证播放令牌
     * @param token 播放令牌
     * @param streamId 流ID
     * @return 是否有效
     */
    boolean validatePlayToken(String token, String streamId);
    
    /**
     * 检查流是否存在且可播放
     * @param streamId 流ID
     * @return 是否可播放
     */
    boolean isStreamPlayable(String streamId);
    
    /**
     * 检查用户是否有播放权限
     * @param userId 用户ID
     * @param streamId 流ID
     * @return 是否有权限
     */
    boolean hasPlayPermission(String userId, String streamId);
} 