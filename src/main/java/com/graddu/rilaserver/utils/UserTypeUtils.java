package com.graddu.rilaserver.utils;

import com.graddu.rilaserver.entity.UserType;

/**
 * 用户类型工具类
 */
public class UserTypeUtils {
    
    /**
     * 获取用户类型的默认最大设备数
     */
    public static int getDefaultMaxDevices(String userType) {
        return UserType.fromCode(userType).getDefaultMaxDevices();
    }
    
    /**
     * 获取用户类型的默认最大推流数
     */
    public static int getDefaultMaxStreams(String userType) {
        return UserType.fromCode(userType).getDefaultMaxStreams();
    }
    
    /**
     * 获取用户类型描述
     */
    public static String getUserTypeDescription(String userType) {
        return UserType.fromCode(userType).getDescription();
    }
    
    /**
     * 验证用户类型是否有效
     */
    public static boolean isValidUserType(String userType) {
        return UserType.isValidCode(userType);
    }
    
    /**
     * 检查用户是否为授权用户
     */
    public static boolean isAuthUser(String userType) {
        return UserType.AUTH.getCode().equals(userType);
    }
    
    /**
     * 检查用户是否为企业用户
     */
    public static boolean isEnterpriseUser(String userType) {
        return UserType.ENTERPRISE.getCode().equals(userType);
    }
    
    /**
     * 检查用户是否为普通注册用户
     */
    public static boolean isRegisterUser(String userType) {
        return UserType.REGISTER.getCode().equals(userType);
    }
    
    /**
     * 检查用户是否有高级权限（授权用户或企业用户）
     */
    public static boolean hasAdvancedPermissions(String userType) {
        return isAuthUser(userType) || isEnterpriseUser(userType);
    }
    
    /**
     * 根据用户类型获取推荐的最大设备数
     */
    public static int getRecommendedMaxDevices(String userType) {
        switch (UserType.fromCode(userType)) {
            case REGISTER:
                return 1; // 普通用户1个设备
            case AUTH:
                return 20; // 授权用户20个设备
            case ENTERPRISE:
                return 100; // 企业用户100个设备
            default:
                return 1;
        }
    }
} 