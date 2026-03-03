package com.graddu.rilaserver.controller;

import com.graddu.rilaserver.dto.UserDto;
import com.graddu.rilaserver.entity.User;
import com.graddu.rilaserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// 统一响应结构
class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}

@RestController
@RequestMapping("/api")
public class RegisterController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody UserDto userDto) {
        try {
            // 检查用户是否已存在
            User existingUser = userService.findUserByEmail(userDto.getUserId());
            if (existingUser != null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "用户已存在", null));
            }
            
            // 设置默认值
            if (userDto.getType() == null) {
                userDto.setType("register");
            }
            if (userDto.getMaxDevices() == null) {
                userDto.setMaxDevices(1);
            }
            if (userDto.getMaxStreams() == null) {
                userDto.setMaxStreams(com.graddu.rilaserver.utils.UserTypeUtils.getDefaultMaxStreams(userDto.getType()));
            }
            
            userService.saveUser(userDto);
            return ResponseEntity.ok(new ApiResponse<>(true, "注册成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "注册失败: " + e.getMessage(), null));
        }
    }

    @PostMapping("/admin/addAuthUser")
    public ResponseEntity<ApiResponse<Void>> addAuthUser(@RequestBody UserDto userDto) {
        try {
            // 检查用户是否已存在
            User existingUser = userService.findUserByEmail(userDto.getUserId());
            if (existingUser != null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "用户已存在", null));
            }
            
            // 设置授权用户默认值
            userDto.setType("auth");
            if (userDto.getMaxDevices() == null) {
                userDto.setMaxDevices(20);
            }
            
            userService.saveUser(userDto);
            return ResponseEntity.ok(new ApiResponse<>(true, "授权用户添加成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "添加失败: " + e.getMessage(), null));
        }
    }

    @GetMapping("/admin/listAuthUsers")
    public ResponseEntity<ApiResponse<List<UserDto>>> listAuthUsers() {
        try {
            List<UserDto> users = userService.findAllUsers().stream()
                    .filter(u -> "auth".equals(u.getType()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "查询成功", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "查询失败: " + e.getMessage(), null));
        }
    }

    @PostMapping("/admin/addEnterpriseUser")
    public ResponseEntity<ApiResponse<Void>> addEnterpriseUser(@RequestBody UserDto userDto) {
        try {
            // 检查用户是否已存在
            User existingUser = userService.findUserByEmail(userDto.getUserId());
            if (existingUser != null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "用户已存在", null));
            }
            
            // 设置企业用户默认值
            userDto.setType("enterprise");
            if (userDto.getMaxDevices() == null) {
                userDto.setMaxDevices(100); // 企业用户默认100个设备
            }
            if (userDto.getMaxStreams() == null) {
                userDto.setMaxStreams(com.graddu.rilaserver.utils.UserTypeUtils.getDefaultMaxStreams("enterprise"));
            }
            
            userService.saveUser(userDto);
            return ResponseEntity.ok(new ApiResponse<>(true, "企业用户添加成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "添加失败: " + e.getMessage(), null));
        }
    }

    @GetMapping("/admin/listEnterpriseUsers")
    public ResponseEntity<ApiResponse<List<UserDto>>> listEnterpriseUsers() {
        try {
            List<UserDto> users = userService.findAllUsers().stream()
                    .filter(u -> "enterprise".equals(u.getType()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "查询成功", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "查询失败: " + e.getMessage(), null));
        }
    }

    @GetMapping("/admin/listUsersByType")
    public ResponseEntity<ApiResponse<List<UserDto>>> listUsersByType(@RequestParam String type) {
        try {
            List<UserDto> users = userService.findAllUsers().stream()
                    .filter(u -> type.equals(u.getType()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "查询成功", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "查询失败: " + e.getMessage(), null));
        }
    }

    @GetMapping("/admin/all-users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        try {
            List<UserDto> users = userService.findAllUsers();
            return ResponseEntity.ok(new ApiResponse<>(true, "查询成功", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "查询失败: " + e.getMessage(), null));
        }
    }
}