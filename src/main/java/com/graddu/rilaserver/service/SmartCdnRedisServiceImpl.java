package com.graddu.rilaserver.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

import com.graddu.rilaserver.config.SmartCdnConfig;
import com.graddu.rilaserver.config.ZLMediaKitConfig;
import com.graddu.rilaserver.dto.SmartCdnClientRegisterRequest;
import com.graddu.rilaserver.dto.SmartCdnPlayUrlResponse;
import com.graddu.rilaserver.dto.SmartCdnRelayRegisterRequest;
import com.graddu.rilaserver.entity.StreamInfo;
import com.graddu.rilaserver.repository.StreamInfoRepository;
import com.graddu.rilaserver.constant.SmartCdnRedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service("smartCdnRedisService")
public class SmartCdnRedisServiceImpl implements SmartCdnService {

    @Autowired
    private SmartCdnConfig smartCdnConfig;

    @Autowired
    private StreamInfoRepository streamInfoRepository;

    @Autowired
    private ZLMediaKitConfig zlMediaKitConfig;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public void registerClient(SmartCdnClientRegisterRequest request) {
        if (!smartCdnConfig.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(request.getClientId()) || !StringUtils.hasText(request.getLanId())) {
            return;
        }

        String clientKey = SmartCdnRedisKey.CLIENT_INFO_KEY + request.getClientId();
        Map<String, String> clientMap = new HashMap<>();
        clientMap.put("lanId", request.getLanId());
        clientMap.put("lanIp", request.getLanIp());
        clientMap.put("capabilities", request.getCapabilities() != null ? String.join(",", request.getCapabilities()) : "");
        clientMap.put("lastSeenAt", LocalDateTime.now().toString());
        
        redisTemplate.opsForHash().putAll(clientKey, clientMap);
        redisTemplate.expire(clientKey, SmartCdnRedisKey.CLIENT_EXPIRE_SECONDS, TimeUnit.SECONDS);

        String lanKey = SmartCdnRedisKey.LAN_CLIENTS_KEY + request.getLanId();
        redisTemplate.opsForSet().add(lanKey, request.getClientId());
        redisTemplate.expire(lanKey, SmartCdnRedisKey.CLIENT_EXPIRE_SECONDS * 2, TimeUnit.SECONDS);
    }

    @Override
    @Transactional
    public boolean registerRelayNode(SmartCdnRelayRegisterRequest request) {
        if (!smartCdnConfig.isEnabled()) {
            return false;
        }
        if (!StringUtils.hasText(request.getStreamId()) || !StringUtils.hasText(request.getParentUrl())) {
            return false;
        }

        // LAN ID is required for SmartCDN relay nodes
        if (!StringUtils.hasText(request.getLanId())) {
            throw new IllegalArgumentException("LAN ID is required for SmartCDN relay registration. Please ensure the client has detected the LAN ID.");
        }

        String streamId = request.getStreamId();
        String parentUrl = request.getParentUrl();
        String parentHash = SmartCdnRedisKey.getUrlHash(parentUrl);
        String parentKey = SmartCdnRedisKey.NODE_INFO_PREFIX + streamId + ":" + parentHash;

        Map<Object, Object> parentInfo = redisTemplate.opsForHash().entries(parentKey);
        if (parentInfo.isEmpty()) {
            return false;
        }

        int parentDepth = Integer.parseInt(parentInfo.getOrDefault("depth", "0").toString());
        if (parentDepth >= smartCdnConfig.getMaxDepth()) {
            return false;
        }

        String parentSubsKey = SmartCdnRedisKey.NODE_SUBS_PREFIX + streamId + ":" + parentHash;
        String currentSubsStr = redisTemplate.opsForValue().get(parentSubsKey);
        int currentSubs = currentSubsStr != null ? Integer.parseInt(currentSubsStr) : 0;
        int maxSubs = Integer.parseInt(parentInfo.getOrDefault("maxSubscribers", "10").toString());
        
        if (currentSubs >= maxSubs) {
            return false;
        }

        String lanId = request.getLanId();
        boolean isFirstLayerFromZlm = parentDepth == 1 && "ZLMEDIAKIT".equals(parentInfo.get("platform"));

        if (isFirstLayerFromZlm && StringUtils.hasText(lanId)) {
            // 允许同一 LAN 下存在多个 Depth=2 的节点，以实现负载均衡和高可用
            // 原有的唯一性限制已移除
            
            Set<String> lanClientIds = redisTemplate.opsForSet().members(SmartCdnRedisKey.LAN_CLIENTS_KEY + lanId);
            boolean hasPreferClient = false;
            if (lanClientIds != null) {
                for (String cId : lanClientIds) {
                    Object capsObj = redisTemplate.opsForHash().get(SmartCdnRedisKey.CLIENT_INFO_KEY + cId, "capabilities");
                    if (capsObj != null && capsObj.toString().contains("prefer-as-lan-relay")) {
                        hasPreferClient = true;
                        break;
                    }
                }
            }
            
            boolean currentIsPrefer = false;
            if (StringUtils.hasText(request.getClientId())) {
                 Object capsObj = redisTemplate.opsForHash().get(SmartCdnRedisKey.CLIENT_INFO_KEY + request.getClientId(), "capabilities");
                 if (capsObj != null && capsObj.toString().contains("prefer-as-lan-relay")) {
                     currentIsPrefer = true;
                 }
            }

            if (hasPreferClient && !currentIsPrefer) {
                return false;
            }
        }

        String playUrl = StringUtils.hasText(request.getMediamtxPlayUrl()) ? request.getMediamtxPlayUrl() : request.getMediamtxPullUrl();
        String nodeHash = SmartCdnRedisKey.getUrlHash(playUrl);
        String nodeKey = SmartCdnRedisKey.NODE_INFO_PREFIX + streamId + ":" + nodeHash;
        String nodeSubsKey = SmartCdnRedisKey.NODE_SUBS_PREFIX + streamId + ":" + nodeHash;

        Map<String, String> nodeMap = new HashMap<>();
        nodeMap.put("streamId", streamId);
        nodeMap.put("pullUrl", playUrl);
        nodeMap.put("platform", "MEDIAMTX");
        nodeMap.put("depth", String.valueOf(parentDepth + 1));
        nodeMap.put("parentId", parentUrl);
        nodeMap.put("maxSubscribers", String.valueOf(smartCdnConfig.getMaxSubscribersPerNode()));
        nodeMap.put("lanId", lanId != null ? lanId : "");
        nodeMap.put("status", "ACTIVE");
        
        redisTemplate.opsForHash().putAll(nodeKey, nodeMap);
        redisTemplate.expire(nodeKey, SmartCdnRedisKey.NODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        redisTemplate.opsForValue().setIfAbsent(nodeSubsKey, "0");
        redisTemplate.expire(nodeSubsKey, SmartCdnRedisKey.NODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        if (StringUtils.hasText(lanId)) {
            String lanIndexKey = SmartCdnRedisKey.INDEX_LAN_NODES + streamId + ":" + lanId;
            redisTemplate.opsForSet().add(lanIndexKey, nodeHash);
            redisTemplate.expire(lanIndexKey, SmartCdnRedisKey.NODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        // 只有当节点不存在（新注册）时，才增加父节点的订阅数
        // 避免节点心跳续约导致父节点订阅数无限增加
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(nodeKey))) {
            redisTemplate.opsForValue().increment(parentSubsKey);
        }

        redisTemplate.expire(parentKey, SmartCdnRedisKey.NODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.expire(parentSubsKey, SmartCdnRedisKey.NODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public SmartCdnPlayUrlResponse getBestPlayUrl(String streamId, String lanId, String exclude) {
        SmartCdnPlayUrlResponse response = new SmartCdnPlayUrlResponse();
        response.setStreamId(streamId);
        
        Set<String> excludedUrls = new HashSet<>();
        if (StringUtils.hasText(exclude)) {
            String[] parts = exclude.split(",");
            for (String part : parts) {
                if (StringUtils.hasText(part)) {
                    excludedUrls.add(part.trim());
                }
            }
        }

        if (!smartCdnConfig.isEnabled()) {
            String fallbackUrl = buildZlmPlayUrl(streamId);
            response.setSuccess(fallbackUrl != null);
            response.setPullUrl(fallbackUrl);
            response.setPlatform("ZLMEDIAKIT");
            response.setLanId(null);
            if (fallbackUrl == null) {
                response.setMessage("SmartCDN disabled and stream not found");
            }
            return response;
        }

        if (StringUtils.hasText(lanId)) {
            String lanIndexKey = SmartCdnRedisKey.INDEX_LAN_NODES + streamId + ":" + lanId;
            Set<String> candidates = redisTemplate.opsForSet().members(lanIndexKey);
            
            if (candidates != null && !candidates.isEmpty()) {
                String bestNodeHash = null;
                int bestDepth = Integer.MAX_VALUE;
                int bestSubs = Integer.MAX_VALUE;
                String bestPullUrl = null;
                
                for (String hash : candidates) {
                     Map<Object, Object> info = redisTemplate.opsForHash().entries(SmartCdnRedisKey.NODE_INFO_PREFIX + streamId + ":" + hash);
                     if (info.isEmpty()) continue;
                     
                     String pullUrl = (String) info.get("pullUrl");
                     if (excludedUrls.contains(pullUrl)) continue;
                     
                     String subsStr = redisTemplate.opsForValue().get(SmartCdnRedisKey.NODE_SUBS_PREFIX + streamId + ":" + hash);
                     int currentSubs = subsStr != null ? Integer.parseInt(subsStr) : 0;
                     int maxSubs = Integer.parseInt(info.getOrDefault("maxSubscribers", "10").toString());
                     
                     if (currentSubs >= maxSubs) continue;
                     
                     int depth = Integer.parseInt(info.getOrDefault("depth", "999").toString());
                     
                     if (depth < bestDepth || (depth == bestDepth && currentSubs < bestSubs)) {
                         bestDepth = depth;
                         bestSubs = currentSubs;
                         bestNodeHash = hash;
                         bestPullUrl = pullUrl;
                     }
                }
                
                if (bestNodeHash != null) {
                    response.setSuccess(true);
                    response.setPullUrl(bestPullUrl);
                    response.setPlatform("MEDIAMTX");
                    response.setLanId(lanId);
                    return response;
                }
            }
        }

        String rootHash = redisTemplate.opsForValue().get(SmartCdnRedisKey.INDEX_STREAM_ROOT + streamId);
        if (rootHash != null) {
            Map<Object, Object> rootInfo = redisTemplate.opsForHash().entries(SmartCdnRedisKey.NODE_INFO_PREFIX + streamId + ":" + rootHash);
            if (!rootInfo.isEmpty()) {
                String rootUrl = (String) rootInfo.get("pullUrl");
                if (!excludedUrls.contains(rootUrl)) {
                    response.setSuccess(true);
                    response.setPullUrl(rootUrl);
                    response.setPlatform("ZLMEDIAKIT");
                    response.setLanId(null);
                    return response;
                }
            }
        }

        String fallbackUrl = buildZlmPlayUrl(streamId);
        
        if (fallbackUrl != null && excludedUrls.contains(fallbackUrl)) {
             response.setSuccess(false);
             response.setMessage("All available nodes are excluded");
             return response;
        }

        response.setSuccess(fallbackUrl != null);
        response.setPullUrl(fallbackUrl);
        response.setPlatform("ZLMEDIAKIT");
        response.setLanId(null);
        if (fallbackUrl == null) {
            response.setMessage("Stream not found");
        }
        return response;
    }

    @Override
    @Transactional
    public void ensureRootNode(String streamId, String rtmpUrl, String lanId) {
        if (!smartCdnConfig.isEnabled()) {
            return;
        }
        
        String rootHash = SmartCdnRedisKey.getUrlHash(rtmpUrl);
        String rootKey = SmartCdnRedisKey.NODE_INFO_PREFIX + streamId + ":" + rootHash;
        
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(rootKey))) {
             Map<String, String> nodeMap = new HashMap<>();
             nodeMap.put("streamId", streamId);
             nodeMap.put("pullUrl", rtmpUrl);
             nodeMap.put("platform", "ZLMEDIAKIT");
             nodeMap.put("depth", "1");
             nodeMap.put("maxSubscribers", String.valueOf(smartCdnConfig.getMaxSubscribersPerNode()));
             nodeMap.put("status", "ACTIVE");
             if (StringUtils.hasText(lanId)) {
                 nodeMap.put("lanId", lanId);
             }
             
             redisTemplate.opsForHash().putAll(rootKey, nodeMap);
             redisTemplate.expire(rootKey, SmartCdnRedisKey.NODE_EXPIRE_SECONDS * 10, TimeUnit.SECONDS); 
             
             redisTemplate.opsForValue().set(SmartCdnRedisKey.INDEX_STREAM_ROOT + streamId, rootHash);
             redisTemplate.expire(SmartCdnRedisKey.INDEX_STREAM_ROOT + streamId, SmartCdnRedisKey.NODE_EXPIRE_SECONDS * 10, TimeUnit.SECONDS);
        } else {
             // 如果已存在，检查是否需要更新 lanId
             if (StringUtils.hasText(lanId)) {
                 redisTemplate.opsForHash().put(rootKey, "lanId", lanId);
             }
             redisTemplate.expire(rootKey, SmartCdnRedisKey.NODE_EXPIRE_SECONDS * 10, TimeUnit.SECONDS);
             redisTemplate.expire(SmartCdnRedisKey.INDEX_STREAM_ROOT + streamId, SmartCdnRedisKey.NODE_EXPIRE_SECONDS * 10, TimeUnit.SECONDS);
        }
    }

    @Override
    @Transactional
    public int deleteClient(String clientId) {
        if (!StringUtils.hasText(clientId)) {
            return 0;
        }
        
        String clientKey = SmartCdnRedisKey.CLIENT_INFO_KEY + clientId;
        Object lanIdObj = redisTemplate.opsForHash().get(clientKey, "lanId");
        if (lanIdObj != null) {
            String lanId = lanIdObj.toString();
            redisTemplate.opsForSet().remove(SmartCdnRedisKey.LAN_CLIENTS_KEY + lanId, clientId);
        }
        Boolean deleted = redisTemplate.delete(clientKey);
        return Boolean.TRUE.equals(deleted) ? 1 : 0;
    }

    @Override
    @Transactional
    public int deleteRelays(String streamId, String lanId) {
        if (!smartCdnConfig.isEnabled()) {
            return 0;
        }
        if (!StringUtils.hasText(streamId)) {
            return 0;
        }
        
        if (StringUtils.hasText(lanId)) {
            String lanIndexKey = SmartCdnRedisKey.INDEX_LAN_NODES + streamId + ":" + lanId;
            Boolean deleted = redisTemplate.delete(lanIndexKey);
            return Boolean.TRUE.equals(deleted) ? 1 : 0;
        } else {
            String rootIndexKey = SmartCdnRedisKey.INDEX_STREAM_ROOT + streamId;
            Boolean deleted = redisTemplate.delete(rootIndexKey);
            return Boolean.TRUE.equals(deleted) ? 1 : 0;
        }
    }

    private String buildZlmPlayUrl(String streamId) {
        Optional<StreamInfo> infoOpt = streamInfoRepository.findByStreamId(streamId);
        if (infoOpt.isEmpty()) {
            return null;
        }
        StreamInfo info = infoOpt.get();
        String appName = info.getAppName();
        String streamName = info.getStreamName();
        if (!StringUtils.hasText(appName) || !StringUtils.hasText(streamName)) {
            String[] parts = streamId.split("/", 2);
            if (parts.length == 2) {
                appName = parts[0];
                streamName = parts[1];
            } else {
                appName = "live";
                streamName = streamId;
            }
        }
        return zlMediaKitConfig.generatePlayUrl(appName, streamName);
    }
}
