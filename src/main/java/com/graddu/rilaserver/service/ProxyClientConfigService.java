package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.ProxyClientConfigDto;

public interface ProxyClientConfigService {
    
    /**
     * 保存或更新代理客户端配置
     * @param configDto 配置DTO
     * @return 保存后的配置DTO
     */
    ProxyClientConfigDto saveOrUpdateConfig(ProxyClientConfigDto configDto);
    
    /**
     * 根据用户ID获取配置
     * @param userId 用户ID
     * @return 配置DTO，如果不存在则返回null
     */
    ProxyClientConfigDto getConfigByUserId(String userId);
    
    /**
     * 根据ID删除配置
     * @param id 配置ID
     */
    void deleteConfig(Long id);
    
    /**
     * 根据用户ID删除配置
     * @param userId 用户ID
     */
    void deleteConfigByUserId(String userId);
}
