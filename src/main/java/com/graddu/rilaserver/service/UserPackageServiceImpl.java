package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.dto.UserPackageDto;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.entity.Package;
import net.enjoy.springboot.registrationlogin.entity.UserPackage;
import net.enjoy.springboot.registrationlogin.repository.UserPackageRepository;
import net.enjoy.springboot.registrationlogin.repository.UserRepository;
import net.enjoy.springboot.registrationlogin.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserPackageServiceImpl implements UserPackageService {
    @Autowired
    private UserPackageRepository userPackageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PackageRepository packageRepository;

    @Override
    public UserPackage addUserPackage(UserPackageDto userPackageDto) {
        // 根据用户ID查找用户
        User user = userRepository.findByUserId(userPackageDto.getUser());
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userPackageDto.getUser());
        }
        
        // 根据套餐ID查找套餐
        Package pkg = packageRepository.findById(Long.parseLong(userPackageDto.getPkg())).orElseThrow();
        
        UserPackage userPackage = new UserPackage();
        userPackage.setUser(user);
        userPackage.setPkg(pkg);
        userPackage.setSubscribeDate(userPackageDto.getSubscribeDate());
        userPackage.setExpireDate(userPackageDto.getExpireDate());
        userPackage.setRemark(userPackageDto.getRemark());
        return userPackageRepository.save(userPackage);
    }

    @Override
    public List<UserPackage> getUserPackages(Long userId) {
        // 首先根据userId查找用户，然后获取用户套餐
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }
        return userPackageRepository.findByUserUserId(user.getUserId());
    }

    @Override
    public Optional<UserPackage> getUserPackageById(Long userPackageId) {
        return userPackageRepository.findById(userPackageId);
    }

    @Override
    public void removeUserPackage(Long userPackageId) {
        userPackageRepository.deleteById(userPackageId);
    }
    
    @Override
    public Package getUserCurrentPackage(String userId) {
        // 查询用户当前套餐（通过直接关联）
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            return null;
        }
        return user.getUserPackage();
    }
    
    @Override
    public List<UserPackage> getUserPackageHistory(String userId) {
        // 查询用户所有套餐历史（通过中间表）
        return userPackageRepository.findByUserUserId(userId);
    }
    
    @Override
    public List<UserPackageDto> getUserPackageDetails(String userId) {
        // 查询用户套餐详情（包含套餐信息）
        List<UserPackage> userPackages = userPackageRepository.findByUserUserId(userId);
        
        return userPackages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    private UserPackageDto convertToDto(UserPackage userPackage) {
        UserPackageDto dto = new UserPackageDto();
        dto.setId(userPackage.getId());
        dto.setUser(userPackage.getUser().getUserId());
        dto.setPkg(userPackage.getPkg().getId().toString());
        dto.setSubscribeDate(userPackage.getSubscribeDate());
        dto.setExpireDate(userPackage.getExpireDate());
        dto.setRemark(userPackage.getRemark());
        return dto;
    }
} 