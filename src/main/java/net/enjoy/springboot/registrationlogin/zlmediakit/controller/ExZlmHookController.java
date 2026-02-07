package net.enjoy.springboot.registrationlogin.zlmediakit.controller;

import io.github.lunasaw.zlm.entity.ServerNodeConfig;
import io.github.lunasaw.zlm.hook.param.*;
import io.github.lunasaw.zlm.hook.service.ZlmHookService;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

import net.enjoy.springboot.registrationlogin.entity.StreamInfo;
import net.enjoy.springboot.registrationlogin.repository.StreamInfoRepository;
import net.enjoy.springboot.registrationlogin.service.StreamService;
import net.enjoy.springboot.registrationlogin.service.StreamEventService;
import net.enjoy.springboot.registrationlogin.service.PlayAuthService;
import net.enjoy.springboot.registrationlogin.dto.PlayAuthResult;
import net.enjoy.springboot.registrationlogin.config.ZLMediaKitConfig;
import net.enjoy.springboot.registrationlogin.service.SmartCdnService;
import net.enjoy.springboot.registrationlogin.constant.SmartCdnRedisKey;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

@Tag(name = "zlm-hook", description = "zlm-hook")
@Slf4j
@RestController
@ConditionalOnProperty(value = "zlm.hook-enable", havingValue = "true")
@RequestMapping("/index/hook/")
public class ExZlmHookController {

    private final ZlmHookService zlmHookService;
    private final StreamService streamService;
    private final StreamInfoRepository streamInfoRepository;
    private final AsyncTaskExecutor executor;
    private final StreamEventService streamEventService;
    private final PlayAuthService playAuthService;
    private final ZLMediaKitConfig zlmediaKitConfig;
    private final SmartCdnService smartCdnService;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public ExZlmHookController(
            ZlmHookService zlmHookService,
            StreamService streamService,
            StreamInfoRepository streamInfoRepository,
            StreamEventService streamEventService,
            @Qualifier("taskExecutor") AsyncTaskExecutor executor,
            PlayAuthService playAuthService,
            ZLMediaKitConfig zlmediaKitConfig,
            SmartCdnService smartCdnService,
            StringRedisTemplate redisTemplate) {
        this.zlmHookService = zlmHookService;
        this.streamService = streamService;
        this.streamInfoRepository = streamInfoRepository;
        this.streamEventService = streamEventService;
        this.executor = executor;
        this.playAuthService = playAuthService;
        this.zlmediaKitConfig = zlmediaKitConfig;
        this.smartCdnService = smartCdnService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 流量统计事件，播放器或推流器断开时并且耗用流量超过特定阈值时会触发此事件， 阈值通过配置文件general.flowThreshold配置；此事件对回复不敏感。
     *
     * @param param
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/on_flow_report", produces = "application/json;charset=UTF-8")
    public HookResult onFlowReport(@RequestBody OnFlowReportHookParam param) {
        executor.execute(() -> zlmHookService.onFlowReport(param));
        return HookResult.SUCCESS();
    }

    /**
     * 访问http文件服务器上hls之外的文件时触发。结果会被缓存Cookie
     *
     * @param param
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/on_http_access", produces = "application/json;charset=UTF-8")
    public HookResultForOnHttpAccess onHttpAccess(@RequestBody OnHttpAccessParam param) {
        return zlmHookService.onHttpAccess(param);
    }

    /** 播放器鉴权事件，rtsp/rtmp/http-flv/ws-flv/hls的播放都将触发此鉴权事件。 */
    @ResponseBody
    @PostMapping(value = "/on_play", produces = "application/json;charset=UTF-8")
    public HookResult onPlay(@RequestBody OnPlayHookParam param) {
        String streamId = param.getStream();
        String clientIp = param.getIp();
        String protocol = param.getSchema();
        String app = param.getApp();
        String vhost = param.getVhost();
        String port = String.valueOf(param.getPort());
        String params = param.getParams();
        
        log.info("播放鉴权请求: streamId={}, ip={}, protocol={}, app={}, vhost={}, port={}, params={}", 
            streamId, clientIp, protocol, app, vhost, port, params);
        
        try {
            // 从URL参数中提取认证信息
            String userId = extractParam(params, "userId");
            String token = extractParam(params, "token");
            String playToken = extractParam(params, "playToken");
            
            // 使用播放鉴权服务进行验证
            PlayAuthResult authResult = playAuthService.verifyPlayAuth(
                streamId, userId, playToken != null ? playToken : token, clientIp, protocol);
            
            if (authResult.isAllowed()) {
                log.info("播放鉴权成功: streamId={}, userId={}, ip={}, protocol={}", 
                    streamId, userId, clientIp, protocol);
                
                // 异步增加观看者计数和发送播放开始通知
                executor.execute(() -> {
                    try {
                        // 增加观看者计数，但不改变流状态
                        incrementViewerCount(streamId);
                        
                        // 使用配置的动态主机地址生成各种播放URL
                        String rtmpUrl = zlmediaKitConfig.generatePlayUrl("live", streamId);
                        String hlsUrl = zlmediaKitConfig.generateHlsUrl("live", streamId);
                        String flvUrl = zlmediaKitConfig.generateFlvUrl("live", streamId);
                        
                        // 发送播放开始通知，但不改变流状态
                        streamEventService.notifyPlayStarted(streamId, userId, rtmpUrl, hlsUrl, flvUrl);
                        log.info("播放开始通知已发送: streamId={}, userId={}, rtmpUrl={}, hlsUrl={}, flvUrl={}", 
                                streamId, userId, rtmpUrl, hlsUrl, flvUrl);
                    } catch (Exception e) {
                        log.error("发送播放开始通知失败: streamId={}, userId={}, error={}", 
                            streamId, userId, e.getMessage(), e);
                    }
                });
                
                return HookResult.SUCCESS();
            } else {
                log.warn("播放鉴权失败: streamId={}, userId={}, reason={}", 
                    streamId, userId, authResult.getMessage());
                return new HookResult(-1, authResult.getMessage());
            }
            
        } catch (Exception e) {
            log.error("播放鉴权处理异常: streamId={}, error={}", streamId, e.getMessage(), e);
            return new HookResult(-1, "播放鉴权处理异常: " + e.getMessage());
        }
    }

    /**
     * 增加观看者计数
     */
    private void incrementViewerCount(String streamId) {
        try {
            Optional<StreamInfo> streamInfoOptional = streamInfoRepository.findByStreamId(streamId);
            if (streamInfoOptional.isPresent()) {
                StreamInfo streamInfo = streamInfoOptional.get();
                int currentCount = streamInfo.getViewerCount() != null ? streamInfo.getViewerCount() : 0;
                streamInfo.setViewerCount(currentCount + 1);
                streamInfo.setUpdatedAt(LocalDateTime.now());
                streamInfoRepository.save(streamInfo);
                
                log.info("观看者计数增加: streamId={}, 当前观看者数={}", streamId, streamInfo.getViewerCount());
            }
        } catch (Exception e) {
            log.error("增加观看者计数失败: streamId={}, error={}", streamId, e.getMessage(), e);
        }
    }

    /**
     * 减少观看者计数
     */
    private void decrementViewerCount(String streamId) {
        try {
            Optional<StreamInfo> streamInfoOptional = streamInfoRepository.findByStreamId(streamId);
            if (streamInfoOptional.isPresent()) {
                StreamInfo streamInfo = streamInfoOptional.get();
                int currentCount = streamInfo.getViewerCount() != null ? streamInfo.getViewerCount() : 0;
                if (currentCount > 0) {
                    streamInfo.setViewerCount(currentCount - 1);
                    streamInfo.setUpdatedAt(LocalDateTime.now());
                    streamInfoRepository.save(streamInfo);
                    
                    log.info("观看者计数减少: streamId={}, 当前观看者数={}", streamId, streamInfo.getViewerCount());
                }
            }
        } catch (Exception e) {
            log.error("减少观看者计数失败: streamId={}, error={}", streamId, e.getMessage(), e);
        }
    }

    /**
     * rtsp/rtmp/rtp推流鉴权事件。
     */
    @ResponseBody
    @PostMapping(value = "/on_publish", produces = "application/json;charset=UTF-8")
    public HookResultForOnPublish onPublish(@RequestBody OnPublishHookParam param) {
        String streamId = param.getStream();
        log.info("on_publish Hook received: streamId={}, params={}, ip={}", streamId, param.getParams(), param.getIp());
        
        // 查找流信息
        Optional<StreamInfo> streamInfoOptional = streamInfoRepository.findByStreamId(streamId);

        if (streamInfoOptional.isEmpty()) {
            return new HookResultForOnPublish(-1, "Stream not found or expired");
        }

        StreamInfo streamInfo = streamInfoOptional.get();

        // 验证流状态是否为CREATED
        if (StreamInfo.StreamStatus.CREATED.equals(streamInfo.getStatus())) {
            // 验证通过，更新数据库
            streamInfo.setStatus(StreamInfo.StreamStatus.PUSHING);
            streamInfo.setStartTime(LocalDateTime.now());
            streamInfo.setIpAddress(param.getIp());
            streamInfoRepository.save(streamInfo);
            
            // 异步发送推流开始通知给用户的所有在线设备
            executor.execute(() -> {
                try {
                    // 使用配置的动态主机地址生成各种播放URL
                    String rtmpUrl = zlmediaKitConfig.generatePlayUrl("live", streamId);
                    String hlsUrl = zlmediaKitConfig.generateHlsUrl("live", streamId);
                    String flvUrl = zlmediaKitConfig.generateFlvUrl("live", streamId);
                    
                    // 通知用户的所有在线设备
                    streamEventService.notifyPublishStarted(
                        streamId, 
                        streamInfo.getUserId(), 
                        streamInfo.getPushUrl(), 
                        rtmpUrl, 
                        hlsUrl, 
                        flvUrl
                    );
                    String lanId = extractParam(param.getParams(), "lanId");
                    
                    // [Robustness Fix] 如果参数中没有 lanId，尝试从 Redis 临时映射中获取
                    if (!StringUtils.hasText(lanId)) {
                        String mappingKey = SmartCdnRedisKey.TEMP_STREAM_LAN_MAPPING + streamId;
                        String cachedLanId = redisTemplate.opsForValue().get(mappingKey);
                        if (StringUtils.hasText(cachedLanId)) {
                            lanId = cachedLanId;
                            log.info("从Redis临时映射中找回 lanId: streamId={}, lanId={}", streamId, lanId);
                            // 找到后可以删除临时key，或者等待过期
                            redisTemplate.delete(mappingKey);
                        } else {
                            log.warn("无法获取 lanId: 参数中不存在且Redis映射未找到. streamId={}", streamId);
                        }
                    }

                    smartCdnService.ensureRootNode(streamId, rtmpUrl, lanId);
                    
                    log.info("推流开始通知已发送: userId={}, streamId={}", streamInfo.getUserId(), streamId);
                } catch (Exception e) {
                    log.error("发送推流开始通知失败: userId={}, streamId={}, error={}", 
                        streamInfo.getUserId(), streamId, e.getMessage(), e);
                }
            });

            return HookResultForOnPublish.SUCCESS();
        } else {
            return new HookResultForOnPublish(-1, "Invalid stream status: " + streamInfo.getStatus());
        }
    }

    @ResponseBody
    @PostMapping(value = "/on_record_mp4", produces = "application/json;charset=UTF-8")
    public HookResult onRecordMp4(@RequestBody OnRecordMp4HookParam param) {
        executor.execute(() -> zlmHookService.onRecordMp4(param));
        return HookResult.SUCCESS();
    }

    /** rtpServer收流超时 调用openRtpServer 接口，rtp server 长时间未收到数据,执行此web hook,对回复不敏感 */
    @ResponseBody
    @PostMapping(value = "/on_rtp_server_timeout", produces = "application/json;charset=UTF-8")
    public HookResult onRtpServerTimeout(@RequestBody OnRtpServerTimeoutHookParam param) {
        executor.execute(() -> zlmHookService.onRtpServerTimeout(param));
        return HookResult.SUCCESS();
    }

    /**
     * rtsp专用的鉴权事件，先触发on_rtsp_realm事件然后才会触发on_rtsp_auth事件。
     *
     * @param param
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/on_rtsp_auth", produces = "application/json;charset=UTF-8")
    public HookResultForOnRtspAuth onRtspAuth(@RequestBody OnRtspAuthHookParam param) {
        return zlmHookService.onRtspAuth(param);
    }

    /**
     * 该rtsp流是否开启rtsp专用方式的鉴权事件，开启后才会触发on_rtsp_auth事件。
     *
     * <p>需要指出的是rtsp也支持url参数鉴权，它支持两种方式鉴权。
     *
     * @param param
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/on_rtsp_realm", produces = "application/json;charset=UTF-8")
    public HookResultForOnRtspRealm onRtspRealm(@RequestBody OnRtspRealmHookParam param) {
        return zlmHookService.onRtspRealm(param);
    }

    /** 发送rtp(startSendRtp)被动关闭时回调 */
    @ResponseBody
    @PostMapping(value = "/on_send_rtp_stopped", produces = "application/json;charset=UTF-8")
    public HookResult onSendRtpStopped(@RequestBody OnSendRtpStoppedHookParam param) {
        executor.execute(() -> zlmHookService.onSendRtpStopped(param));
        return HookResult.SUCCESS();
    }

    @ResponseBody
    @PostMapping(value = "/on_server_exited", produces = "application/json;charset=UTF-8")
    public HookResult onServerExited(@RequestBody HookParam param) {
        executor.execute(() -> zlmHookService.onServerExited(param));
        return HookResult.SUCCESS();
    }

    /**
     * 服务器定时上报时间，上报间隔可配置，默认10s上报一次
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/on_server_keepalive", produces = "application/json;charset=UTF-8")
    public HookResult onServerKeepalive(@RequestBody OnServerKeepaliveHookParam param) {
        executor.execute(() -> zlmHookService.onServerKeepLive(param));
        return HookResult.SUCCESS();
    }

    /** 服务器启动事件，可以用于监听服务器崩溃重启；此事件对回复不敏感。 */
    @ResponseBody
    @PostMapping(value = "/on_server_started", produces = "application/json;charset=UTF-8")
    public HookResult onServerStarted(@RequestBody ServerNodeConfig param) {
        log.info("onServerStarted::param = {}", param);
        // executor.execute(() ->
        // zlmHookService.onServerStarted(JSON.parseObject(param.toJSONString(),
        // ServerNodeConfig.class)));
        return HookResult.SUCCESS();
    }

    /** rtsp/rtmp流注册或注销时触发此事件；此事件对回复不敏感。 */
    @ResponseBody
    @PostMapping(value = "/on_stream_changed", produces = "application/json;charset=UTF-8")
    public HookResult onStreamChanged(@RequestBody OnStreamChangedHookParam param) {
        // 处理流状态变化
        String streamId = param.getStream();
        boolean isRegist = param.isRegist(); // true表示注册，false表示注销
        
        Optional<StreamInfo> streamInfoOptional = streamInfoRepository.findByStreamId(streamId);
        if (streamInfoOptional.isPresent()) {
            StreamInfo streamInfo = streamInfoOptional.get();
            
            if (isRegist) {
                // 流注册（推流开始）
                streamInfo.setStatus(StreamInfo.StreamStatus.PUSHING);
                streamInfo.setStartTime(LocalDateTime.now());
                log.info("流注册: streamId={}, userId={}", streamId, streamInfo.getUserId());
            } else {
                // 流注销（推流结束）
                streamInfo.setStatus(StreamInfo.StreamStatus.STOPPED);
                streamInfo.setEndTime(LocalDateTime.now());
                
                // 计算推流时长
                if (streamInfo.getStartTime() != null) {
                    long duration = java.time.Duration.between(streamInfo.getStartTime(), streamInfo.getEndTime()).getSeconds();
                    streamInfo.setDuration(duration);
                }
                
                log.info("流注销: streamId={}, userId={}, duration={}s", streamId, streamInfo.getUserId(), streamInfo.getDuration());
                
                // 异步发送推流结束通知
                executor.execute(() -> {
                    try {
                        streamEventService.notifyPublishStopped(streamId, streamInfo.getUserId());
                        log.info("推流结束通知已发送: userId={}, streamId={}", streamInfo.getUserId(), streamId);
                    } catch (Exception e) {
                        log.error("发送推流结束通知失败: userId={}, streamId={}, error={}", 
                            streamInfo.getUserId(), streamId, e.getMessage(), e);
                    }
                });
            }
            
            streamInfoRepository.save(streamInfo);
        }
        
        executor.execute(() -> zlmHookService.onStreamChanged(param));
        return HookResult.SUCCESS();
    }

    /**
     * 流无人观看时事件，用户可以通过此事件选择是否关闭无人看的流。
     *
     * <p>流无人观看时事件，用户可以通过此事件选择是否关闭无人看的流。
     * 一个直播流注册上线了，如果一直没人观看也会触发一次无人观看事件，触发时的协议schema是随机的，看哪种协议最晚注册(一般为hls)。
     * 后续从有人观看转为无人观看，触发协议schema为最后一名观看者使用何种协议。
     * 目前mp4/hls录制不当做观看人数(mp4录制可以通过配置文件mp4_as_player控制，但是rtsp/rtmp/rtp转推算观看人数，也会触发该事件。
     */
    @ResponseBody
    @PostMapping(value = "/on_stream_none_reader", produces = "application/json;charset=UTF-8")
    public HookResultForStreamNoneReader onStreamNoneReader(
            @RequestBody OnStreamNoneReaderHookParam param) {
        return zlmHookService.onStreamNoneReader(param);
    }

    /**
     * 流未找到事件，用户可以在此事件触发时，立即去拉流，这样可以实现按需拉流；此事件对回复不敏感。
     *
     * @return code int 错误代码，0代表允许播放 msg string 不允许播放时的错误提示
     */
    @ResponseBody
    @PostMapping(value = "/on_stream_not_found", produces = "application/json;charset=UTF-8")
    public HookResult onStreamNotFound(@RequestBody OnStreamNotFoundHookParam param) {
        executor.execute(() -> zlmHookService.onStreamNotFound(param));
        return HookResult.SUCCESS();
    }

    /**
     * 从URL参数字符串中提取指定参数的值
     * @param params 参数字符串，格式如 "userId=123&token=abc"
     * @param paramName 参数名
     * @return 参数值，如果不存在则返回null
     */
    private String extractParam(String params, String paramName) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        
        try {
            String[] pairs = params.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2 && paramName.equals(keyValue[0])) {
                    return java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                }
            }
        } catch (Exception e) {
            log.error("解析URL参数失败: params={}, paramName={}, error={}", params, paramName, e.getMessage());
        }
        
        return null;
    }
}
