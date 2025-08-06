package net.enjoy.springboot.registrationlogin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageDto {
    private Long id;
    private String type;
    private String description;
    private LocalDate subscribeDate;
    private LocalDate expireDate;
    private Double price;
    private Double discount;
    private Integer maxUsers;
    
    // 新增字段
    private Boolean autoRenew = false;
    private String paymentMethod = "yearly";
    private Integer trialPeriod = 0;
    private Integer gracePeriod = 0;
    private Double totalPrice;
    
    // 前端计算字段（不保存到数据库）
    private Integer subscriptionDuration; // 订阅时长
    private String durationUnit; // 时长单位
    
    private List<UserPackageDto> userPackages;
} 