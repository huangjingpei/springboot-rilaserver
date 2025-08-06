package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.dto.PackageDto;
import net.enjoy.springboot.registrationlogin.dto.UserDto;
import net.enjoy.springboot.registrationlogin.entity.Role;
import net.enjoy.springboot.registrationlogin.entity.User;
import net.enjoy.springboot.registrationlogin.entity.Package;
import net.enjoy.springboot.registrationlogin.entity.UserStatus;
import net.enjoy.springboot.registrationlogin.repository.RoleRepository;
import net.enjoy.springboot.registrationlogin.repository.UserRepository;
import net.enjoy.springboot.registrationlogin.repository.PackageRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PackageRepository packageRepository;
    
    @Autowired
    private SecurityService securityService;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void saveUser(UserDto userDto) {
        // 密码验证
        if (userDto.getPassword() != null && !securityService.validatePassword(userDto.getPassword())) {
            throw new IllegalArgumentException("密码不符合安全要求");
        }
        
        User user = new User();
        user.setUserId(userDto.getUserId());
        user.setPhone(userDto.getPhone() != null ? userDto.getPhone() : userDto.getUserId()); // 如果没有phone，使用userId作为phone
        user.setName(userDto.getName() != null ? userDto.getName() : generateUserName(userDto.getUserId())); // 自动生成用户名
        //encrypt the password using spring security
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        
        // 设置用户类型和最大设备数
        user.setType(userDto.getType() != null ? userDto.getType() : "register");
        user.setMaxDevices(userDto.getMaxDevices() != null ? userDto.getMaxDevices() : 1);
        user.setMaxStreams(userDto.getMaxStreams() != null ? userDto.getMaxStreams() : 
                net.enjoy.springboot.registrationlogin.utils.UserTypeUtils.getDefaultMaxStreams(user.getType()));
        
        // 设置用户状态和安全相关字段
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());

        Role role = roleRepository.findByName("ROLE_USER");
        if (role == null) {
            role = checkRoleExist();
        }
        user.setRoles(List.of(role));
        
        // 处理套餐信息
        if (userDto.getUserPackage() != null) {
            Package pkg = new Package();
            pkg.setType(userDto.getUserPackage().getType());
            pkg.setDescription(userDto.getUserPackage().getDescription());
            pkg.setPrice(userDto.getUserPackage().getPrice());
            pkg.setDiscount(userDto.getUserPackage().getDiscount());
            pkg.setMaxUsers(userDto.getUserPackage().getMaxUsers());
            
            // 设置自动续费和支付方式
            pkg.setAutoRenew(userDto.getUserPackage().getAutoRenew() != null ? 
                userDto.getUserPackage().getAutoRenew() : false);
            pkg.setPaymentMethod(userDto.getUserPackage().getPaymentMethod() != null ? 
                userDto.getUserPackage().getPaymentMethod() : "yearly");
            
            // 设置试用期和宽限期
            pkg.setTrialPeriod(userDto.getUserPackage().getTrialPeriod() != null ? 
                userDto.getUserPackage().getTrialPeriod() : 0);
            pkg.setGracePeriod(userDto.getUserPackage().getGracePeriod() != null ? 
                userDto.getUserPackage().getGracePeriod() : 0);
            
            // 计算订阅日期和过期日期
            LocalDate today = LocalDate.now();
            LocalDate subscribeDate = today.plusDays(1); // 次日激活
            
            // 如果有试用期，订阅日期延后试用期天数
            if (pkg.getTrialPeriod() > 0) {
                subscribeDate = subscribeDate.plusDays(pkg.getTrialPeriod());
            }
            
            pkg.setSubscribeDate(subscribeDate);
            
            // 计算过期日期
            LocalDate expireDate = subscribeDate;
            if (userDto.getUserPackage().getSubscriptionDuration() != null && 
                userDto.getUserPackage().getDurationUnit() != null) {
                
                int duration = userDto.getUserPackage().getSubscriptionDuration();
                String unit = userDto.getUserPackage().getDurationUnit();
                
                switch (unit) {
                    case "days":
                        expireDate = subscribeDate.plusDays(duration);
                        break;
                    case "months":
                        expireDate = subscribeDate.plusMonths(duration);
                        break;
                    case "years":
                        expireDate = subscribeDate.plusYears(duration);
                        break;
                    default:
                        expireDate = subscribeDate.plusYears(1); // 默认1年
                }
            } else {
                expireDate = subscribeDate.plusYears(1); // 默认1年
            }
            
            // 如果有宽限期，过期日期延后宽限期天数
            if (pkg.getGracePeriod() > 0) {
                expireDate = expireDate.plusDays(pkg.getGracePeriod());
            }
            
            pkg.setExpireDate(expireDate);
            
            // 计算套餐总价
            if (pkg.getPrice() != null && pkg.getDiscount() != null) {
                double basePrice = pkg.getPrice();
                double discountRate = pkg.getDiscount() / 100.0; // 转换为小数
                double discountedPrice = basePrice * discountRate;
                
                // 根据支付方式计算总价
                int multiplier = 1;
                switch (pkg.getPaymentMethod()) {
                    case "monthly":
                        multiplier = 1;
                        break;
                    case "quarterly":
                        multiplier = 3;
                        break;
                    case "yearly":
                        multiplier = 12;
                        break;
                    case "lifetime":
                        multiplier = 120; // 假设终身为10年
                        break;
                }
                
                pkg.setTotalPrice(discountedPrice * multiplier);
            }
            
            // 保存套餐
            Package savedPackage = packageRepository.save(pkg);
            user.setUserPackage(savedPackage);
        }
        
        userRepository.save(user);
        
        // 记录审计日志
        securityService.logAction(userDto.getUserId(), "USER_REGISTRATION", "USER", null, null, 
                "用户注册成功", "SUCCESS");
    }

    private Role checkRoleExist() {
        Role role = new Role();
        role.setName("ROLE_USER");
        return roleRepository.save(role);
    }

    @Override
    public User findUserByEmail(String phone) {
        return userRepository.findByUserId(phone);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map((user) -> convertEntityToDto(user))
                .collect(Collectors.toList());
    }

    @Override
    public void assignPackageToUser(Long userId, Long packageId) {
        User user = userRepository.findById(userId).orElseThrow();
        Package pkg = packageRepository.findById(packageId).orElseThrow();
        user.setUserPackage(pkg);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 记录审计日志
        securityService.logAction(user.getUserId(), "ASSIGN_PACKAGE", "USER_PACKAGE", null, null, 
                "分配套餐: " + pkg.getType(), "SUCCESS");
    }

    @Override
    public PackageDto getUserPackageDto(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Package pkg = user.getUserPackage();
        if (pkg == null) return null;
        PackageDto dto = new PackageDto();
        BeanUtils.copyProperties(pkg, dto);
        return dto;
    }

    @Override
    public PackageDto updateUserPackage(Long userId, PackageDto packageDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        Package pkg = user.getUserPackage();

        // 如果用户还没有套餐，就创建一个新的套餐实例
        if (pkg == null) {
            pkg = new Package();
            user.setUserPackage(pkg); // 关键：将新套餐与用户关联
        }

        // 更新套餐的属性
        pkg.setType(packageDto.getType());
        pkg.setDescription(packageDto.getDescription());
        pkg.setPrice(packageDto.getPrice());
        pkg.setDiscount(packageDto.getDiscount());
        pkg.setMaxUsers(packageDto.getMaxUsers());
        
        // 如果有订阅日期，也进行更新
        if (packageDto.getSubscribeDate() != null) {
            pkg.setSubscribeDate(packageDto.getSubscribeDate());
        }
        if (packageDto.getExpireDate() != null) {
            pkg.setExpireDate(packageDto.getExpireDate());
        }

        // 保存套餐信息，因为与User的级联关系，user也会被更新
        Package saved = packageRepository.save(pkg);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 记录审计日志
        securityService.logAction(user.getUserId(), "UPDATE_PACKAGE", "USER_PACKAGE", null, null, 
                "更新套餐: " + saved.getType(), "SUCCESS");

        // 返回更新后的数据
        PackageDto dto = new PackageDto();
        BeanUtils.copyProperties(saved, dto);
        return dto;
    }

    private UserDto convertEntityToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUserId(user.getUserId());
        userDto.setPhone(user.getPhone());
        userDto.setName(user.getName());
        userDto.setType(user.getType());
        userDto.setMaxDevices(user.getMaxDevices());
        userDto.setMaxStreams(user.getMaxStreams());
        
        // 安全相关字段
        userDto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        userDto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        userDto.setAccountLockedUntil(user.getAccountLockedUntil());
        userDto.setPasswordChangedAt(user.getPasswordChangedAt());
        userDto.setLastLoginAt(user.getLastLoginAt());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        
        // 套餐信息
        if (user.getUserPackage() != null) {
            PackageDto packageDto = new PackageDto();
            BeanUtils.copyProperties(user.getUserPackage(), packageDto);
            userDto.setUserPackage(packageDto);
        }
        
        // 注意：不复制密码字段，保证安全
        return userDto;
    }

    /**
     * 根据userId生成用户名
     * 如果是手机号：显示为 用户138****5678
     * 如果是邮箱：显示为 用户abc@example.com的前缀部分
     */
    private String generateUserName(String userId) {
        if (userId == null || userId.isEmpty()) {
            return "用户" + System.currentTimeMillis() % 10000;
        }
        
        // 判断是手机号还是邮箱
        if (userId.matches("^1[3-9]\\d{9}$")) {
            // 手机号：显示前3位和后4位，中间用****代替
            return "用户" + userId.substring(0, 3) + "****" + userId.substring(7);
        } else if (userId.contains("@")) {
            // 邮箱：显示@前的部分
            String prefix = userId.substring(0, userId.indexOf('@'));
            return "用户" + prefix;
        } else {
            // 其他情况：显示前几个字符
            return "用户" + userId.substring(0, Math.min(userId.length(), 8));
        }
    }
    
    @Override
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
