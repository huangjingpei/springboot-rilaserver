package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.dto.PackageDto;
import java.util.List;

public interface PackageService {
    PackageDto createPackage(PackageDto packageDto);
    List<PackageDto> getAllPackages();
    PackageDto getPackageById(Long id);
    PackageDto updatePackage(Long id, PackageDto packageDto);
    void deletePackage(Long id);
} 