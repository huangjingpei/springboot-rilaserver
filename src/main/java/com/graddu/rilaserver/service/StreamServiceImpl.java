package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.entity.StreamInfo;
import net.enjoy.springboot.registrationlogin.repository.StreamInfoRepository;
import net.enjoy.springboot.registrationlogin.config.ZLMediaKitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class StreamServiceImpl implements StreamService {

    @Autowired
    private StreamInfoRepository streamInfoRepository;
    
    @Autowired
    private ZLMediaKitConfig zlmediaKitConfig;

    @Override
    public Map<String, String> generatePushUrl(String userId) {
        // 1. 生成唯一的流ID
        String streamId = UUID.randomUUID().toString().replaceAll("-", "");
        String appName = "live";
        String streamName = streamId;

        // 2. 创建并保存流信息到数据库
        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setUserId(userId);
        streamInfo.setStreamId(appName + "/" + streamName);
        streamInfo.setStatus(StreamInfo.StreamStatus.CREATED);
        streamInfo.setCreatedAt(LocalDateTime.now());
        streamInfoRepository.save(streamInfo);

        // 3. 使用配置的动态主机地址生成RTMP推流地址
        String rtmpUrl = zlmediaKitConfig.generatePushUrl(appName, streamName);

        // 4. 返回给Controller
        Map<String, String> response = new HashMap<>();
        response.put("rtmpUrl", rtmpUrl);
        response.put("streamId", streamInfo.getStreamId());
        response.put("appName", appName);
        response.put("streamName", streamName);
        return response;
    }

    // 额外的方法：创建流信息（可能被其他地方使用）
    public StreamInfo createStream(String userId, String appName, String streamName) {
        // 生成流ID
        String streamId = String.format("%s_%s_%d", userId, streamName, System.currentTimeMillis());
        
        // 使用配置的动态主机地址生成RTMP URL
        String rtmpUrl = zlmediaKitConfig.generatePushUrl(appName, streamName);

        // 创建流信息
        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setStreamId(streamId);
        streamInfo.setUserId(userId);
        streamInfo.setPushUrl(rtmpUrl);
        streamInfo.setStatus(StreamInfo.StreamStatus.CREATED);
        streamInfo.setCreatedAt(LocalDateTime.now());

        return streamInfoRepository.save(streamInfo);
    }
} 