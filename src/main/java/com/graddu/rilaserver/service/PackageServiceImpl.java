package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.dto.PackageDto;
import net.enjoy.springboot.registrationlogin.entity.Package;
import net.enjoy.springboot.registrationlogin.repository.PackageRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageServiceImpl implements PackageService {
    @Autowired
    private PackageRepository packageRepository;

    @Override
    public PackageDto createPackage(PackageDto packageDto) {
        Package pkg = new Package();
        BeanUtils.copyProperties(packageDto, pkg);
        Package saved = packageRepository.save(pkg);
        PackageDto result = new PackageDto();
        BeanUtils.copyProperties(saved, result);
        return result;
    }

    @Override
    public List<PackageDto> getAllPackages() {
        return packageRepository.findAll().stream().map(pkg -> {
            PackageDto dto = new PackageDto();
            BeanUtils.copyProperties(pkg, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public PackageDto getPackageById(Long id) {
        Package pkg = packageRepository.findById(id).orElseThrow();
        PackageDto dto = new PackageDto();
        BeanUtils.copyProperties(pkg, dto);
        return dto;
    }

    @Override
    public PackageDto updatePackage(Long id, PackageDto packageDto) {
        Package pkg = packageRepository.findById(id).orElseThrow();
        BeanUtils.copyProperties(packageDto, pkg, "id");
        Package saved = packageRepository.save(pkg);
        PackageDto dto = new PackageDto();
        BeanUtils.copyProperties(saved, dto);
        return dto;
    }

    @Override
    public void deletePackage(Long id) {
        packageRepository.deleteById(id);
    }
} 