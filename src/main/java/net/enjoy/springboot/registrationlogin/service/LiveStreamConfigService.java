package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.dto.LiveStreamConfigDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LiveStreamConfigService {
    
    /**
     * 批量保存或更新直播间配置（通过Excel文件）
     */
    List<LiveStreamConfigDto> saveOrUpdateConfigsFromExcel(String clientId, byte[] excelData);
    
    /**
     * 保存或更新单个直播间配置
     */
    LiveStreamConfigDto saveOrUpdateConfig(LiveStreamConfigDto configDto);
    
    /**
     * 根据ID获取直播间配置
     */
    LiveStreamConfigDto getConfigById(Long id);
    
    /**
     * 根据客户端ID获取所有配置
     */
    List<LiveStreamConfigDto> getConfigsByClientId(String clientId);
    
    /**
     * 根据客户端ID分页获取配置
     */
    Page<LiveStreamConfigDto> getConfigsByClientId(String clientId, Pageable pageable);
    
    /**
     * 根据客户端ID删除所有配置
     */
    void deleteConfigsByClientId(String clientId);
    
    /**
     * 根据ID删除配置
     */
    void deleteConfigById(Long id);
    
    /**
     * 启用/禁用配置
     */
    LiveStreamConfigDto setConfigActive(Long id, boolean active);
    
    /**
     * 获取客户端统计信息
     */
    ClientStats getClientStats(String clientId);
    
    /**
     * 手动触发配置更新
     */
    void triggerConfigUpdate(Long configId);
    
    /**
     * 手动触发客户端所有配置更新
     */
    void triggerClientConfigsUpdate(String clientId);
    
    /**
     * 获取配置状态变更日志
     */
    List<StatusLogDto> getConfigStatusLogs(Long configId);
    
    /**
     * 获取客户端状态变更日志
     */
    List<StatusLogDto> getClientStatusLogs(String clientId);
    
    /**
     * 客户端统计信息
     */
    class ClientStats {
        private String clientId;
        private long totalConfigs;
        private long activeConfigs;
        private long liveConfigs;
        
        // getters and setters
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public long getTotalConfigs() { return totalConfigs; }
        public void setTotalConfigs(long totalConfigs) { this.totalConfigs = totalConfigs; }
        public long getActiveConfigs() { return activeConfigs; }
        public void setActiveConfigs(long activeConfigs) { this.activeConfigs = activeConfigs; }
        public long getLiveConfigs() { return liveConfigs; }
        public void setLiveConfigs(long liveConfigs) { this.liveConfigs = liveConfigs; }
    }
    
    /**
     * 状态变更日志DTO
     */
    class StatusLogDto {
        private Long id;
        private Long configId;
        private String clientId;
        private Boolean oldStatus;
        private Boolean newStatus;
        private String changeTime;
        private String changeReason;
        
        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getConfigId() { return configId; }
        public void setConfigId(Long configId) { this.configId = configId; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public Boolean getOldStatus() { return oldStatus; }
        public void setOldStatus(Boolean oldStatus) { this.oldStatus = oldStatus; }
        public Boolean getNewStatus() { return newStatus; }
        public void setNewStatus(Boolean newStatus) { this.newStatus = newStatus; }
        public String getChangeTime() { return changeTime; }
        public void setChangeTime(String changeTime) { this.changeTime = changeTime; }
        public String getChangeReason() { return changeReason; }
        public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    }
}

