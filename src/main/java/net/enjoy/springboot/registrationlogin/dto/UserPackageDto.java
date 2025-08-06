package net.enjoy.springboot.registrationlogin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPackageDto {
    private Long id;
    private String user;
    private String pkg;
    private LocalDate subscribeDate;
    private LocalDate expireDate;
    private String remark;
    // 可根据业务需要添加更多字段
}