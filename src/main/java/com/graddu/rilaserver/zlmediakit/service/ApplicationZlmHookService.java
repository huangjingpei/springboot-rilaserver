package com.graddu.rilaserver.zlmediakit.service;

import io.github.lunasaw.zlm.entity.ServerNodeConfig;
import io.github.lunasaw.zlm.hook.param.*;
import io.github.lunasaw.zlm.hook.service.AbstractZlmHookService;
import com.graddu.rilaserver.entity.StreamInfo;
import com.graddu.rilaserver.repository.StreamInfoRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * zlm-spring-boot-starter的hook服务，默认实现，尚未测试
 *
 * <p>zlm-spring-boot-starter(1.0.3)此版本使用的springboot版本为2.7.8,本项目(open)使用spingboot版本为3.2.4。
 * 两个版本的yaml配置有所冲突与不同，io.github.lunasaw.zlm.config包下的配置都未曾生效。 ZlmRestService使用不受影响
 */
@Component
@Slf4j
public class ApplicationZlmHookService extends AbstractZlmHookService {

    @Autowired
    private StreamInfoRepository streamInfoRepository;

    @Override
    public void onServerKeepLive(OnServerKeepaliveHookParam param) {
        log.info("param-->{}", param);
    }

    @Override
    public HookResult onPlay(OnPlayHookParam param) {
        log.info("param-->{}", param);
        return HookResult.SUCCESS();
    }

    @Override
    public HookResultForOnPublish onPublish(OnPublishHookParam param) {
        log.info("param-->{}", param);
        return HookResultForOnPublish.SUCCESS();
    }

    @Override
    public void onStreamChanged(OnStreamChangedHookParam param) {}

    @Override
    public HookResultForStreamNoneReader onStreamNoneReader(OnStreamNoneReaderHookParam param) {
        log.info("param-->{}", param);
        
        // 处理无观看者事件 - 重置观看者计数
        String streamId = param.getStream();
        try {
            Optional<StreamInfo> streamInfoOptional = streamInfoRepository.findByStreamId(streamId);
            if (streamInfoOptional.isPresent()) {
                StreamInfo streamInfo = streamInfoOptional.get();
                
                // 重置观看者计数为0，但不改变流状态
                streamInfo.setViewerCount(0);
                streamInfo.setUpdatedAt(LocalDateTime.now());
                streamInfoRepository.save(streamInfo);
                
                log.info("流无观看者，重置观看者计数: streamId={}, 流状态保持: {}", 
                        streamId, streamInfo.getStatus());
            }
        } catch (Exception e) {
            log.error("处理无观看者事件失败: streamId={}, error={}", streamId, e.getMessage(), e);
        }
        
        return HookResultForStreamNoneReader.SUCCESS();
    }

    @Override
    public void onStreamNotFound(OnStreamNotFoundHookParam param) {
        log.info("param-->{}", param);
    }

    @Override
    public void onServerStarted(ServerNodeConfig param) {
        log.info("param-->{}", param);
    }

    @Override
    public void onSendRtpStopped(OnSendRtpStoppedHookParam param) {
        log.info("param-->{}", param);
    }

    @Override
    public void onFlowReport(OnFlowReportHookParam param) {
        log.info("param-->{}", param);
        
        // 处理流量报告 - 当播放会话结束时减少观看者计数
        String streamId = param.getStream();
        boolean isPlayer = param.isPlayer(); // true表示播放者，false表示推流者
        
        try {
            if (isPlayer) {
                // 只处理播放者的流量报告，减少观看者计数
                Optional<StreamInfo> streamInfoOptional = streamInfoRepository.findByStreamId(streamId);
                if (streamInfoOptional.isPresent()) {
                    StreamInfo streamInfo = streamInfoOptional.get();
                    int currentCount = streamInfo.getViewerCount() != null ? streamInfo.getViewerCount() : 0;
                    
                    if (currentCount > 0) {
                        streamInfo.setViewerCount(currentCount - 1);
                        streamInfo.setUpdatedAt(LocalDateTime.now());
                        streamInfoRepository.save(streamInfo);
                        
                        log.info("播放会话结束，观看者计数减少: streamId={}, 当前观看者数={}, 持续时间={}s", 
                                streamId, streamInfo.getViewerCount(), param.getDuration());
                    }
                }
            } else {
                log.debug("推流者会话结束: streamId={}, 持续时间={}s", streamId, param.getDuration());
            }
        } catch (Exception e) {
            log.error("处理流量报告失败: streamId={}, isPlayer={}, error={}", streamId, isPlayer, e.getMessage(), e);
        }
    }

    @Override
    public void onRtpServerTimeout(OnRtpServerTimeoutHookParam param) {

        log.info("param-->{}", param);
    }

    @Override
    public HookResultForOnHttpAccess onHttpAccess(OnHttpAccessParam param) {

        log.info("param-->{}", param);

        return HookResultForOnHttpAccess.SUCCESS();
    }

    @Override
    public HookResultForOnRtspRealm onRtspRealm(OnRtspRealmHookParam param) {

        log.info("param-->{}", param);
        return HookResultForOnRtspRealm.SUCCESS();
    }

    @Override
    public HookResultForOnRtspAuth onRtspAuth(OnRtspAuthHookParam param) {

        log.info("param-->{}", param);
        return HookResultForOnRtspAuth.SUCCESS();
    }

    @Override
    public void onRecordMp4(OnRecordMp4HookParam param) {

        log.info("param-->{}", param);
    }
}
