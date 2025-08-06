package net.enjoy.springboot.registrationlogin.controller;

import net.enjoy.springboot.registrationlogin.dto.PackageDto;
import net.enjoy.springboot.registrationlogin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/package")
public class PackageController {

    @Autowired
    private UserService userService;

    // 查询用户当前套餐
    @GetMapping
    public ResponseEntity<PackageDto> getUserPackage(@PathVariable Long userId) {
        PackageDto dto = userService.getUserPackageDto(userId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    // 分配/变更用户套餐（userId 绑定 packageId）
    @PutMapping("/{packageId}")
    public ResponseEntity<?> assignPackage(@PathVariable Long userId, @PathVariable Long packageId) {
        userService.assignPackageToUser(userId, packageId);
        return ResponseEntity.ok("套餐分配成功");
    }

    // 修改用户当前套餐的内容（如价格、名称等）
    @PutMapping
    public ResponseEntity<PackageDto> updateUserPackage(
            @PathVariable Long userId,
            @RequestBody PackageDto packageDto) {
        PackageDto updated = userService.updateUserPackage(userId, packageDto);
        return ResponseEntity.ok(updated);
    }
} 