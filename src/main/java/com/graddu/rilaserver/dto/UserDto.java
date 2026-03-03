package com.graddu.rilaserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    
    @NotBlank(message = "用户ID不能为空")
    @Pattern(regexp = "^(1[3-9]\\d{9}|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$", 
             message = "用户ID必须是有效的手机号码或邮箱格式")
    private String userId; // 手机号或邮箱作为用户ID
    
    @Pattern(regexp = "^(1[3-9]\\d{9}|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$", 
             message = "手机号码或邮箱格式不正确")
    private String phone; // 手机号码或邮箱
    
    private String name; // 用户姓名
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;
    
    private String type = "register"; // 默认注册类型
    
    private Integer maxDevices = 1; // 默认最大设备数
    
    private Integer maxStreams = 1; // 默认最大推流数
    
    // 新增安全相关字段
    private String status; // 用户状态：ACTIVE, INACTIVE, SUSPENDED, BANNED, PENDING_VERIFICATION
    
    private Integer failedLoginAttempts; // 登录失败次数
    
    private LocalDateTime accountLockedUntil; // 账户锁定时间
    
    private LocalDateTime passwordChangedAt; // 密码修改时间
    
    private LocalDateTime lastLoginAt; // 最后登录时间
    
    private LocalDateTime createdAt; // 创建时间
    
    private LocalDateTime updatedAt; // 更新时间
    
    private List<RoleDto> roles;
    
    private PackageDto userPackage;
    
    private List<UserPackageDto> userPackages;
}