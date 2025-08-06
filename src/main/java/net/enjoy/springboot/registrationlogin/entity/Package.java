package net.enjoy.springboot.registrationlogin.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packages")
@Data
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String description;
    private LocalDate subscribeDate;
    private LocalDate expireDate;
    private Double price;
    private Double discount;
    private Integer maxUsers;
    
    // 新增字段
    private Boolean autoRenew = false; // 自动续费，默认不启用
    private String paymentMethod = "yearly"; // 支付方式，默认年付
    private Integer trialPeriod = 0; // 试用期天数，默认0
    private Integer gracePeriod = 0; // 宽限期天数，默认0
    private Double totalPrice; // 套餐总价（价格 × 订阅时长）

    @OneToMany(mappedBy = "pkg", cascade = CascadeType.ALL)
    private List<UserPackage> userPackages = new ArrayList<>();
} 