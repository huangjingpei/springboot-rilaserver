package net.enjoy.springboot.registrationlogin.service;

import java.util.Map;

public interface StreamService {

    /**
     * 为指定用户生成一个推流地址
     * @param userId 用户ID
     * @return 包含rtmpUrl的Map
     */
    Map<String, String> generatePushUrl(String userId);

} 