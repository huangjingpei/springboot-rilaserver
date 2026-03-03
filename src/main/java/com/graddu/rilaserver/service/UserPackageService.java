package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.UserPackageDto;
import com.graddu.rilaserver.entity.UserPackage;
import com.graddu.rilaserver.entity.Package;
import java.util.List;
import java.util.Optional;

public interface UserPackageService {
    UserPackage addUserPackage(UserPackageDto userPackageDto);
    List<UserPackage> getUserPackages(Long userId);
    Optional<UserPackage> getUserPackageById(Long userPackageId);
    void removeUserPackage(Long userPackageId);
    
    // 新增方法：查询用户当前套餐
    Package getUserCurrentPackage(String userId);
    
    // 新增方法：查询用户所有套餐历史
    List<UserPackage> getUserPackageHistory(String userId);
    
    // 新增方法：查询用户套餐详情（包含套餐信息）
    List<UserPackageDto> getUserPackageDetails(String userId);
}