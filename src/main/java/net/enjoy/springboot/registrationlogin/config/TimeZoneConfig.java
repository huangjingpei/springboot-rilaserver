package net.enjoy.springboot.registrationlogin.config;

import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

    /**
     * 默认时区配置 - 中国标准时间
     */
    public static final String DEFAULT_TIMEZONE = "Asia/Shanghai";
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of(DEFAULT_TIMEZONE);
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone(DEFAULT_TIMEZONE);
    
    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);

    /**
     * 获取当前北京时间
     */
    public static LocalDateTime getNow() {
        return LocalDateTime.now(DEFAULT_ZONE_ID);
    }
    
    /**
     * 格式化日期时间为字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_DATETIME_FORMATTER);
    }
    
    /**
     * 将LocalDateTime转换为时间戳（毫秒）
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
    }
} 