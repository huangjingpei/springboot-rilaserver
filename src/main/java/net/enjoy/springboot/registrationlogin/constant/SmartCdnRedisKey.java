package net.enjoy.springboot.registrationlogin.constant;

public class SmartCdnRedisKey {
    // 客户端相关
    public static final String CLIENT_INFO_KEY = "smartcdn:client:info:"; // + clientId (Hash)
    public static final String LAN_CLIENTS_KEY = "smartcdn:lan:clients:"; // + lanId (Set<clientId>)
    public static final long CLIENT_EXPIRE_SECONDS = 300; // 5分钟无心跳则视为离线

    // 节点相关
    public static final String NODE_INFO_PREFIX = "smartcdn:node:info:"; // + streamId + ":" + urlHash (Hash)
    public static final String NODE_SUBS_PREFIX = "smartcdn:node:subs:"; // + streamId + ":" + urlHash (Counter)
    public static final String INDEX_LAN_NODES = "smartcdn:index:lan:"; // + streamId + ":" + lanId (Set<urlHash>)
    public static final String INDEX_STREAM_ROOT = "smartcdn:index:root:"; // + streamId (String: urlHash)
    
    // 临时映射：StreamId -> LanId (用于解决推流参数丢失问题)
    public static final String TEMP_STREAM_LAN_MAPPING = "smartcdn:temp:stream_lan:"; // + streamId (String: lanId)
    public static final long TEMP_MAPPING_EXPIRE_SECONDS = 600; // 10分钟有效期，足够推流启动

    public static final long NODE_EXPIRE_SECONDS = 60; // 节点状态过期时间
    
    // 辅助方法：简单的 URL Hash，防止 Key 过长
    public static String getUrlHash(String url) {
        if (url == null) return "null";
        return org.springframework.util.DigestUtils.md5DigestAsHex(url.getBytes());
    }
}
