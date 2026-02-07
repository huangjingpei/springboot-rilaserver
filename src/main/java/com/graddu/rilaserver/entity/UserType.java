package net.enjoy.springboot.registrationlogin.entity;

/**
 * 用户类型枚举
 */
public enum UserType {
    
    REGISTER("register", "普通注册用户", 1, 1),
    AUTH("auth", "授权用户", 20, 5),
    ENTERPRISE("enterprise", "企业用户", 100, 20);
    
    private final String code;
    private final String description;
    private final int defaultMaxDevices;
    private final int defaultMaxStreams;
    
    UserType(String code, String description, int defaultMaxDevices, int defaultMaxStreams) {
        this.code = code;
        this.description = description;
        this.defaultMaxDevices = defaultMaxDevices;
        this.defaultMaxStreams = defaultMaxStreams;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getDefaultMaxDevices() {
        return defaultMaxDevices;
    }
    
    public int getDefaultMaxStreams() {
        return defaultMaxStreams;
    }
    
    /**
     * 根据代码获取用户类型
     */
    public static UserType fromCode(String code) {
        for (UserType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return REGISTER; // 默认返回普通用户
    }
    
    /**
     * 检查是否为有效的用户类型代码
     */
    public static boolean isValidCode(String code) {
        for (UserType type : values()) {
            if (type.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
} 