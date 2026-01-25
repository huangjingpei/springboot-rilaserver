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

@Service
public class SmartCdnServiceImpl implements SmartCdnService {

    @Autowired
    private SmartCdnConfig smartCdnConfig;

    @Autowired
    private StreamRelayNodeRepository streamRelayNodeRepository;

    @Autowired
    private SmartCdnClientRepository smartCdnClientRepository;

    @Autowired
    private StreamInfoRepository streamInfoRepository;

    @Autowired
    private ZLMediaKitConfig zlMediaKitConfig;

    @Override
    @Transactional
    public void registerClient(SmartCdnClientRegisterRequest request) {
        // 1. 检查功能开关
        if (!smartCdnConfig.isEnabled()) {
            return;
        }
        // 2. 基础参数校验
        if (!StringUtils.hasText(request.getClientId()) || !StringUtils.hasText(request.getLanId())) {
            return;
        }

        // 3. 查找或新建客户端记录 (Upsert)
        // 使用 ClientId 作为唯一标识
        SmartCdnClient client = smartCdnClientRepository.findFirstByClientIdOrderByUpdatedAtDesc(request.getClientId());
        if (client == null) {
            client = new SmartCdnClient();
            client.setClientId(request.getClientId());
        }

        // 4. 更新客户端状态信息
        // 这些信息对于拓扑构建至关重要
        client.setLanId(request.getLanId()); // 所在的局域网ID
        client.setLanIp(request.getLanIp()); // 内网IP，供同局域网其他客户端连接
        client.setMediamtxHttpUrl(request.getMediamtxHttpUrl()); // MediaMTX API地址(可选)
        client.setMediamtxRtmpUrlPrefix(request.getMediamtxRtmpUrlPrefix()); // 拉流前缀
        
        // 5. 更新能力标签
        // 如 "prefer-as-lan-relay" 表示该设备性能强，适合做中继
        if (request.getCapabilities() != null && !request.getCapabilities().isEmpty()) {
            client.setCapabilities(String.join(",", request.getCapabilities()));
        }

        // 6. 更新心跳时间
        // 系统会定期清理长时间未更新心跳的客户端
        client.setLastSeenAt(LocalDateTime.now());
        
        smartCdnClientRepository.save(client);
    }

    @Override
    @Transactional
    public boolean registerRelayNode(SmartCdnRelayRegisterRequest request) {
        // 1. 检查 SmartCDN 是否启用
        if (!smartCdnConfig.isEnabled()) {
            return false;
        }
        // 2. 参数校验
        if (!StringUtils.hasText(request.getStreamId()) || !StringUtils.hasText(request.getParentUrl())) {
            return false;
        }

        // 3. 查找父节点（即我从哪里拉的流）
        // 注意：客户端必须诚实上报 parentUrl，服务器据此建立拓扑关系
        Optional<StreamRelayNode> parentOpt = streamRelayNodeRepository.findByStreamIdAndPullUrl(request.getStreamId(), request.getParentUrl());
        if (parentOpt.isEmpty()) {
            return false;
        }
        StreamRelayNode parent = parentOpt.get();

        // 4. 检查深度限制 (MaxDepth)
        // 防止无限级联导致延迟过高
        if (parent.getDepth() >= smartCdnConfig.getMaxDepth()) {
            return false;
        }

        // 5. 检查父节点负载 (MaxSubscribers)
        // 如果父节点已经带不动更多人了，就不允许新节点挂在它下面
        if (parent.getCurrentSubscribers() >= parent.getMaxSubscribers()) {
            return false;
        }

        String lanId = request.getLanId();
        // 6. LAN 入口节点选举逻辑 (Critical Section)
        // 如果父节点是 ZLM (Depth=1)，说明当前申请者想成为该 LAN 的第一个中继 (LAN Gateway, Depth=2)
        boolean isFirstLayerFromZlm = parent.getDepth() == 1 && parent.getPlatform() == StreamRelayNode.Platform.ZLMEDIAKIT;
        
        if (isFirstLayerFromZlm && StringUtils.hasText(lanId)) {
            // 6.1 检查该 LAN 是否已经有入口节点了
            // 原则：同一个 LAN、同一个 Stream，只允许一个 Depth=2 的节点（避免重复回源）
            List<StreamRelayNode> existingLanNodes = streamRelayNodeRepository.findByStreamIdAndLanIdAndPlatformAndDepth(request.getStreamId(), lanId, StreamRelayNode.Platform.MEDIAMTX, 2);
            if (!existingLanNodes.isEmpty()) {
                // 已经有人捷足先登了，拒绝本次注册
                // 客户端应当去连接那个已存在的节点，而不是连接 ZLM
                return false;
            }

            // 6.2 检查 "高性能节点优先" 策略 (Prefer As Lan Relay)
            // 如果 LAN 里有高性能机器 (如 PC/NAS)，则优先让它们做入口，而不是手机/机顶盒
            List<SmartCdnClient> lanClients = smartCdnClientRepository.findByLanId(lanId);
            
            // 只考虑最近 5 分钟活跃的客户端
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            boolean hasPreferClient = lanClients.stream()
                .filter(c -> c.getLastSeenAt() != null && c.getLastSeenAt().isAfter(fiveMinutesAgo))
                .anyMatch(this::hasPreferAsLanRelayCapability);
            
            SmartCdnClient currentClient = null;
            if (StringUtils.hasText(request.getClientId())) {
                currentClient = smartCdnClientRepository.findFirstByClientIdOrderByUpdatedAtDesc(request.getClientId());
            }
            boolean currentIsPrefer = currentClient != null && hasPreferAsLanRelayCapability(currentClient);

            // 如果局域网里有高性能节点在线，但当前申请者不是高性能节点 -> 拒绝
            // 目的：把位置留给高性能节点
            if (hasPreferClient && !currentIsPrefer) {
                return false;
            }
        }

        // 7. 创建新节点记录
        StreamRelayNode node = new StreamRelayNode();
        node.setStreamId(request.getStreamId());
        node.setPlatform(StreamRelayNode.Platform.MEDIAMTX);
        node.setDepth(parent.getDepth() + 1);
        node.setParentId(parent.getId());
        node.setRootId(parent.getRootId() != null ? parent.getRootId() : parent.getId());
        
        // 优先使用 playUrl (客户端自报的)，如果没有则用 pullUrl (通常不对)
        String playUrl = StringUtils.hasText(request.getMediamtxPlayUrl()) ? request.getMediamtxPlayUrl() : request.getMediamtxPullUrl();
        node.setPullUrl(playUrl);
        
        node.setMaxSubscribers(smartCdnConfig.getMaxSubscribersPerNode());
        node.setCurrentSubscribers(0);
        node.setLanId(lanId);
        node.setStatus(StreamRelayNode.Status.ACTIVE);
        streamRelayNodeRepository.save(node);

        // 8. 更新父节点的订阅数 (+1)
        // 使用锁机制防止并发计数错误
        Optional<StreamRelayNode> lockedParentOpt = streamRelayNodeRepository.lockById(parent.getId());
        lockedParentOpt.ifPresent(locked -> {
            locked.setCurrentSubscribers(locked.getCurrentSubscribers() + 1);
            streamRelayNodeRepository.save(locked);
        });
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public SmartCdnPlayUrlResponse getBestPlayUrl(String streamId, String lanId, String exclude) {
        SmartCdnPlayUrlResponse response = new SmartCdnPlayUrlResponse();
        response.setStreamId(streamId);
        
        // 0. 解析 exclude 列表 (客户端报告的故障节点)
        Set<String> excludedUrls = new HashSet<>();
        if (StringUtils.hasText(exclude)) {
            String[] parts = exclude.split(",");
            for (String part : parts) {
                if (StringUtils.hasText(part)) {
                    excludedUrls.add(part.trim());
                }
            }
        }

        // 1. 如果 SmartCDN 功能关闭，直接回退到 ZLMediaKit 原始地址
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

        // 2. 尝试寻找同 LAN 内的可用中继节点 (P2P 模式)
        if (StringUtils.hasText(lanId)) {
            // 获取该 LAN 下所有活跃的 MediaMTX 节点，并按层级(Depth)和负载(Subscribers)排序
            // 排序逻辑：优先选层级浅的，其次选负载低的
            List<StreamRelayNode> candidates = streamRelayNodeRepository.findActiveMediamtxNodesByStreamAndLanOrdered(streamId, lanId);
            
            // 过滤掉已满载的节点，以及被客户端排除的节点 (故障自愈逻辑)
            Optional<StreamRelayNode> bestNode = candidates.stream()
                .filter(node -> !excludedUrls.contains(node.getPullUrl())) // 排除故障节点
                .filter(node -> node.getCurrentSubscribers() < node.getMaxSubscribers())
                .findFirst();

            if (bestNode.isPresent()) {
                StreamRelayNode node = bestNode.get();
                response.setSuccess(true);
                response.setPullUrl(node.getPullUrl());
                response.setPlatform(node.getPlatform().name()); // MEDIAMTX
                response.setLanId(node.getLanId());
                return response;
            }
        }

        // 3. 如果 LAN 内没找到（或都满了，或都被排除了），尝试寻找公网源站节点 (CDN 模式)
        // 通常是 Depth=1 的 ZLMediaKit 节点
        Optional<StreamRelayNode> rootOpt = streamRelayNodeRepository.findByStreamIdAndDepthAndPlatform(streamId, 1, StreamRelayNode.Platform.ZLMEDIAKIT);
        
        // 即使是根节点，如果也被排除了 (极端情况)，也应该跳过
        if (rootOpt.isPresent()) {
            StreamRelayNode root = rootOpt.get();
            if (!excludedUrls.contains(root.getPullUrl())) {
                response.setSuccess(true);
                response.setPullUrl(root.getPullUrl());
                response.setPlatform(root.getPlatform().name());
                response.setLanId(null);
                return response;
            }
        }

        // 4. 如果数据库里完全没有记录，尝试直接构造原始 ZLM 地址
        // 这属于兜底逻辑，防止 SmartCDN 数据库数据丢失导致无法播放
        String fallbackUrl = buildZlmPlayUrl(streamId);
        
        // 最后一道防线：如果构造出的 fallbackUrl 也在排除列表中，那真没办法了
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
    public void ensureRootNode(String streamId, String rtmpUrl) {
        if (!smartCdnConfig.isEnabled()) {
            return;
        }
        Optional<StreamRelayNode> existing = streamRelayNodeRepository.findByStreamIdAndDepthAndPlatform(streamId, 1, StreamRelayNode.Platform.ZLMEDIAKIT);
        if (existing.isPresent()) {
            return;
        }
        StreamRelayNode root = new StreamRelayNode();
        root.setStreamId(streamId);
        root.setPullUrl(rtmpUrl);
        root.setDepth(1);
        root.setParentId(null);
        root.setRootId(null);
        root.setPlatform(StreamRelayNode.Platform.ZLMEDIAKIT);
        root.setMaxSubscribers(smartCdnConfig.getMaxSubscribersPerNode());
        root.setCurrentSubscribers(0);
        root.setLanId(null);
        root.setStatus(StreamRelayNode.Status.ACTIVE);
        streamRelayNodeRepository.save(root);
        root.setRootId(root.getId());
        streamRelayNodeRepository.save(root);
    }

    @Override
    @Transactional
    public int deleteClient(String clientId) {
        if (!StringUtils.hasText(clientId)) {
            return 0;
        }
        long count = smartCdnClientRepository.deleteByClientId(clientId);
        if (count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) count;
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
        long count;
        if (StringUtils.hasText(lanId)) {
            count = streamRelayNodeRepository.deleteByStreamIdAndLanIdAndPlatform(streamId, lanId, StreamRelayNode.Platform.MEDIAMTX);
        } else {
            count = streamRelayNodeRepository.deleteByStreamIdAndPlatform(streamId, StreamRelayNode.Platform.MEDIAMTX);
        }
        if (count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) count;
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

    private boolean hasPreferAsLanRelayCapability(SmartCdnClient client) {
        String caps = client.getCapabilities();
        if (!StringUtils.hasText(caps)) {
            return false;
        }
        String[] parts = caps.split(",");
        for (String part : parts) {
            if ("prefer-as-lan-relay".equals(part.trim())) {
                return true;
            }
        }
        return false;
    }
}
