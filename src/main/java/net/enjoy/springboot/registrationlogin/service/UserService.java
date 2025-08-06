package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.dto.PackageDto;
import net.enjoy.springboot.registrationlogin.dto.UserDto;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.entity.Package;
import net.enjoy.springboot.registrationlogin.repository.UserRepository;
import net.enjoy.springboot.registrationlogin.repository.PackageRepository;

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