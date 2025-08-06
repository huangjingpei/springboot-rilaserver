package net.enjoy.springboot.registrationlogin.controller;

import net.enjoy.springboot.registrationlogin.dto.UserPackageDto;
import net.enjoy.springboot.registrationlogin.entity.UserPackage;
import net.enjoy.springboot.registrationlogin.entity.Package;
import net.enjoy.springboot.registrationlogin.service.UserPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-packages")
public class UserPackageController {

    @Autowired
    private UserPackageService userPackageService;

    // 新增用户套餐
    @PostMapping
    public ResponseEntity<UserPackage> addUserPackage(@RequestBody UserPackageDto userPackageDto) {
        UserPackage up = userPackageService.addUserPackage(userPackageDto);
        return ResponseEntity.ok(up);
    }

    // 查询用户所有套餐
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserPackage>> getUserPackages(@PathVariable Long userId) {
        return ResponseEntity.ok(userPackageService.getUserPackages(userId));
    }

    // 查询单个用户套餐
    @GetMapping("/{userPackageId}")
    public ResponseEntity<UserPackage> getUserPackageById(@PathVariable Long userPackageId) {
        return userPackageService.getUserPackageById(userPackageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 删除用户套餐
    @DeleteMapping("/{userPackageId}")
    public ResponseEntity<?> removeUserPackage(@PathVariable Long userPackageId) {
        userPackageService.removeUserPackage(userPackageId);
        return ResponseEntity.ok("套餐解绑/删除成功");
    }
    
    // 新增：查询用户当前套餐
    @GetMapping("/current/{userId}")
    public ResponseEntity<Package> getUserCurrentPackage(@PathVariable String userId) {
        Package pkg = userPackageService.getUserCurrentPackage(userId);
        return pkg != null ? ResponseEntity.ok(pkg) : ResponseEntity.notFound().build();
    }
    
    // 新增：查询用户套餐历史
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<UserPackage>> getUserPackageHistory(@PathVariable String userId) {
        List<UserPackage> history = userPackageService.getUserPackageHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    // 新增：查询用户套餐详情
    @GetMapping("/details/{userId}")
    public ResponseEntity<List<UserPackageDto>> getUserPackageDetails(@PathVariable String userId) {
        List<UserPackageDto> details = userPackageService.getUserPackageDetails(userId);
        return ResponseEntity.ok(details);
    }
}