package net.enjoy.springboot.registrationlogin.entity;

public enum UserStatus {
    ACTIVE("激活"),
    INACTIVE("未激活"),
    SUSPENDED("暂停"),
    BANNED("封禁"),
    PENDING_VERIFICATION("待验证");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 