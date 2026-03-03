package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.LiveStreamTaskDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LiveStreamTaskService {
    
    /**
     * 批量处理Excel文件上传的任务
     */
    LiveStreamTaskDto.BatchTaskDto processExcelUpload(String clientId, MultipartFile file);
    
    /**
     * 创建单个任务
     */
    LiveStreamTaskDto createTask(LiveStreamTaskDto taskDto);
    
    /**
     * 更新任务
     */
    LiveStreamTaskDto updateTask(Long taskId, LiveStreamTaskDto taskDto);
    
    /**
     * 删除任务
     */
    void deleteTask(Long taskId);
    
    /**
     * 批量删除任务
     */
    void deleteTasks(String clientId, List<Integer> taskIndexes);
    
    /**
     * 根据ID获取任务
     */
    LiveStreamTaskDto getTaskById(Long taskId);
    
    /**
     * 根据客户端ID获取所有任务
     */
    List<LiveStreamTaskDto> getTasksByClientId(String clientId);
    
    /**
     * 根据客户端ID分页获取任务
     */
    Page<LiveStreamTaskDto> getTasksByClientId(String clientId, Pageable pageable);
    
    /**
     * 获取任务状态响应
     */
    LiveStreamTaskDto.StatusResponseDto getTaskStatus(String clientId);
    
    /**
     * 手动执行任务检查
     */
    void executeTaskCheck(Long taskId);
    
    /**
     * 执行客户端的所有任务检查
     */
    void executeClientTaskChecks(String clientId);
    
    /**
     * 执行所有需要检查的任务
     */
    void executeAllTaskChecks();
    
    /**
     * 设置任务激活状态
     */
    void setTaskActive(Long taskId, boolean active);
    
    /**
     * 更新任务检查间隔
     */
    void updateTaskCheckInterval(Long taskId, Integer intervalSeconds);
    
    /**
     * 获取任务统计信息
     */
    TaskStatistics getTaskStatistics(String clientId);
    
    /**
     * 任务统计信息
     */
    class TaskStatistics {
        private String clientId;
        private Long totalTasks;
        private Long activeTasks;
        private Long liveTasks;
        private Long failedTasks;
        
        // getters and setters
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public Long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(Long totalTasks) { this.totalTasks = totalTasks; }
        public Long getActiveTasks() { return activeTasks; }
        public void setActiveTasks(Long activeTasks) { this.activeTasks = activeTasks; }
        public Long getLiveTasks() { return liveTasks; }
        public void setLiveTasks(Long liveTasks) { this.liveTasks = liveTasks; }
        public Long getFailedTasks() { return failedTasks; }
        public void setFailedTasks(Long failedTasks) { this.failedTasks = failedTasks; }
    }
}
