package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.PackageDto;
import com.graddu.rilaserver.dto.UserDto;
import com.graddu.rilaserver.entity.User;
import com.graddu.rilaserver.entity.Package;
import com.graddu.rilaserver.repository.UserRepository;
import com.graddu.rilaserver.repository.PackageRepository;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<UserDto> findAllUsers();

    void assignPackageToUser(Long userId, Long packageId);

    PackageDto getUserPackageDto(Long userId);

    PackageDto updateUserPackage(Long userId, PackageDto packageDto);
    
    User updateUser(User user);
}