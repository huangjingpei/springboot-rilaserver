package net.enjoy.springboot.registrationlogin.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.enjoy.springboot.registrationlogin.config.SmartCdnConfig;
import net.enjoy.springboot.registrationlogin.config.ZLMediaKitConfig;
import net.enjoy.springboot.registrationlogin.dto.SmartCdnClientRegisterRequest;
import net.enjoy.springboot.registrationlogin.dto.SmartCdnPlayUrlResponse;
import net.enjoy.springboot.registrationlogin.dto.SmartCdnRelayRegisterRequest;
import net.enjoy.springboot.registrationlogin.entity.SmartCdnClient;
import net.enjoy.springboot.registrationlogin.entity.StreamInfo;
import net.enjoy.springboot.registrationlogin.entity.StreamRelayNode;
import net.enjoy.springboot.registrationlogin.repository.SmartCdnClientRepository;
import net.enjoy.springboot.registrationlogin.repository.StreamInfoRepository;
import net.enjoy.springboot.registrationlogin.repository.StreamRelayNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("smartCdnMysqlService")
public class SmartCdnMysqlServiceImpl implements SmartCdnService {

    @Autowired
    private SmartCdnConfig smartCdnConfig;

    @Autowired
    private StreamInfoRepository streamInfoRepository;

    @Autowired
    private ZLMediaKitConfig zlMediaKitConfig;

    @Autowired
    private SmartCdnClientRepository smartCdnClientRepository;

    @Autowired
    private StreamRelayNodeRepository streamRelayNodeRepository;

    @Override
    @Transactional
    public void registerClient(SmartCdnClientRegisterRequest request) {
        if (!smartCdnConfig.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(request.getClientId()) || !StringUtils.hasText(request.getLanId())) {
            return;
        }

        SmartCdnClient client = smartCdnClientRepository.findFirstByClientIdOrderByUpdatedAtDesc(request.getClientId());
        if (client == null) {
            client = new SmartCdnClient();
            client.setClientId(request.getClientId());
        }
        
        client.setLanId(request.getLanId());
        client.setLanIp(request.getLanIp());
        if (request.getCapabilities() != null) {
            client.setCapabilities(String.join(",", request.getCapabilities()));
        } else {
            client.setCapabilities("");
        }
        client.setLastSeenAt(LocalDateTime.now());
        
        smartCdnClientRepository.save(client);
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

        Optional<StreamRelayNode> parentOpt = streamRelayNodeRepository.findByStreamIdAndPullUrl(streamId, parentUrl);
        if (parentOpt.isEmpty()) {
            return false;
        }
        StreamRelayNode parent = parentOpt.get();

        if (parent.getDepth() >= smartCdnConfig.getMaxDepth()) {
            return false;
        }

        if (parent.getCurrentSubscribers() >= parent.getMaxSubscribers()) {
            return false;
        }

        String lanId = request.getLanId();
        boolean isFirstLayerFromZlm = parent.getDepth() == 1 && parent.getPlatform() == StreamRelayNode.Platform.ZLMEDIAKIT;

        if (isFirstLayerFromZlm && StringUtils.hasText(lanId)) {
            List<SmartCdnClient> lanClients = smartCdnClientRepository.findByLanId(lanId);
            boolean hasPreferClient = false;
            if (lanClients != null) {
                for (SmartCdnClient c : lanClients) {
                    if (c.getCapabilities() != null && c.getCapabilities().contains("prefer-as-lan-relay")) {
                        hasPreferClient = true;
                        break;
                    }
                }
            }

            boolean currentIsPrefer = false;
            if (StringUtils.hasText(request.getClientId())) {
                SmartCdnClient currentClient = smartCdnClientRepository.findFirstByClientIdOrderByUpdatedAtDesc(request.getClientId());
                if (currentClient != null && currentClient.getCapabilities() != null && currentClient.getCapabilities().contains("prefer-as-lan-relay")) {
                    currentIsPrefer = true;
                }
            }

            if (hasPreferClient && !currentIsPrefer) {
                return false;
            }
        }

        String playUrl = StringUtils.hasText(request.getMediamtxPlayUrl()) ? request.getMediamtxPlayUrl() : request.getMediamtxPullUrl();
        
        // Check if node already exists to avoid duplicates
        Optional<StreamRelayNode> existingNode = streamRelayNodeRepository.findByStreamIdAndPullUrl(streamId, playUrl);
        if (existingNode.isPresent()) {
            StreamRelayNode node = existingNode.get();
            node.setUpdatedAt(LocalDateTime.now());
            node.setStatus(StreamRelayNode.Status.ACTIVE);
            streamRelayNodeRepository.save(node);
            return true;
        }

        StreamRelayNode node = new StreamRelayNode();
        node.setStreamId(streamId);
        node.setPullUrl(playUrl);
        node.setPlatform(StreamRelayNode.Platform.MEDIAMTX);
        node.setDepth(parent.getDepth() + 1);
        node.setParentId(parent.getId());
        node.setRootId(parent.getRootId() != null ? parent.getRootId() : parent.getId()); // Approximate root logic
        node.setMaxSubscribers(smartCdnConfig.getMaxSubscribersPerNode());
        node.setCurrentSubscribers(0);
        node.setLanId(lanId);
        node.setStatus(StreamRelayNode.Status.ACTIVE);
        
        streamRelayNodeRepository.save(node);

        // Update parent subscribers
        parent.setCurrentSubscribers(parent.getCurrentSubscribers() + 1);
        streamRelayNodeRepository.save(parent);

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
            return createFallbackResponse(streamId);
        }

        if (StringUtils.hasText(lanId)) {
            List<StreamRelayNode> candidates = streamRelayNodeRepository.findActiveMediamtxNodesByStreamAndLanOrdered(streamId, lanId);
            
            for (StreamRelayNode node : candidates) {
                if (excludedUrls.contains(node.getPullUrl())) continue;
                if (node.getCurrentSubscribers() >= node.getMaxSubscribers()) continue;
                
                response.setSuccess(true);
                response.setPullUrl(node.getPullUrl());
                response.setPlatform("MEDIAMTX");
                response.setLanId(lanId);
                return response;
            }
        }

        // Fallback to root node (ZLMEDIAKIT)
        List<StreamRelayNode> rootNodes = streamRelayNodeRepository.findByStreamIdAndLanIdAndPlatformAndDepth(streamId, null, StreamRelayNode.Platform.ZLMEDIAKIT, 1);
        // Note: findByStreamIdAndLanIdAndPlatformAndDepth might not handle null lanId correctly if query uses =. 
        // Let's use findByStreamIdAndDepthAndPlatform instead since root nodes usually don't have lanId or it's ignored.
        Optional<StreamRelayNode> rootNodeOpt = streamRelayNodeRepository.findByStreamIdAndDepthAndPlatform(streamId, 1, StreamRelayNode.Platform.ZLMEDIAKIT);
        
        if (rootNodeOpt.isPresent()) {
            StreamRelayNode root = rootNodeOpt.get();
            if (!excludedUrls.contains(root.getPullUrl())) {
                response.setSuccess(true);
                response.setPullUrl(root.getPullUrl());
                response.setPlatform("ZLMEDIAKIT");
                response.setLanId(null);
                return response;
            }
        }

        String fallbackUrl = buildZlmPlayUrl(streamId);
        if (fallbackUrl != null && excludedUrls.contains(fallbackUrl)) {
             response.setSuccess(false);
             response.setMessage("All available nodes are excluded");
             return response;
        }
        
        return createFallbackResponse(streamId);
    }

    @Override
    @Transactional
    public void ensureRootNode(String streamId, String rtmpUrl, String lanId) {
        if (!smartCdnConfig.isEnabled()) {
            return;
        }
        
        Optional<StreamRelayNode> existing = streamRelayNodeRepository.findByStreamIdAndPullUrl(streamId, rtmpUrl);
        if (existing.isEmpty()) {
            StreamRelayNode node = new StreamRelayNode();
            node.setStreamId(streamId);
            node.setPullUrl(rtmpUrl);
            node.setPlatform(StreamRelayNode.Platform.ZLMEDIAKIT);
            node.setDepth(1);
            node.setMaxSubscribers(smartCdnConfig.getMaxSubscribersPerNode());
            node.setCurrentSubscribers(0);
            node.setStatus(StreamRelayNode.Status.ACTIVE);
            if (StringUtils.hasText(lanId)) {
                node.setLanId(lanId);
            }
            
            streamRelayNodeRepository.save(node);
        } else {
            // Update timestamp or ensure status
            StreamRelayNode node = existing.get();
            node.setUpdatedAt(LocalDateTime.now());
            node.setStatus(StreamRelayNode.Status.ACTIVE);
            if (StringUtils.hasText(lanId)) {
                node.setLanId(lanId);
            }
            streamRelayNodeRepository.save(node);
        }
    }

    @Override
    @Transactional
    public int deleteClient(String clientId) {
        if (!StringUtils.hasText(clientId)) {
            return 0;
        }
        return (int) smartCdnClientRepository.deleteByClientId(clientId);
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
            // Delete Mediamtx nodes in this LAN
            return (int) streamRelayNodeRepository.deleteByStreamIdAndLanIdAndPlatform(streamId, lanId, StreamRelayNode.Platform.MEDIAMTX);
        } else {
            // Delete root nodes? Or all nodes for stream?
            // Redis impl deletes INDEX_STREAM_ROOT which effectively hides the root.
            return (int) streamRelayNodeRepository.deleteByStreamIdAndPlatform(streamId, StreamRelayNode.Platform.ZLMEDIAKIT);
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
    
    private SmartCdnPlayUrlResponse createFallbackResponse(String streamId) {
        SmartCdnPlayUrlResponse response = new SmartCdnPlayUrlResponse();
        response.setStreamId(streamId);
        String fallbackUrl = buildZlmPlayUrl(streamId);
        
        response.setSuccess(fallbackUrl != null);
        response.setPullUrl(fallbackUrl);
        response.setPlatform("ZLMEDIAKIT");
        response.setLanId(null);
        if (fallbackUrl == null) {
            response.setMessage("Stream not found");
        }
        return response;
    }
}
