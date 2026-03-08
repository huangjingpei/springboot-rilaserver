package com.graddu.rilaserver.zlmediakit.controller;

import io.github.lunasaw.zlm.hook.param.OnPublishHookParam;
import io.github.lunasaw.zlm.hook.service.ZlmHookService;
import com.graddu.rilaserver.config.ZLMediaKitConfig;
import com.graddu.rilaserver.entity.StreamInfo;
import com.graddu.rilaserver.repository.StreamInfoRepository;
import com.graddu.rilaserver.service.PlayAuthService;
import com.graddu.rilaserver.service.SmartCdnService;
import com.graddu.rilaserver.service.StreamEventService;
import com.graddu.rilaserver.service.StreamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExZlmHookControllerTest {

    @Mock
    private ZlmHookService zlmHookService;
    @Mock
    private StreamService streamService;
    @Mock
    private StreamInfoRepository streamInfoRepository;
    @Mock
    private StreamEventService streamEventService;
    @Mock
    private AsyncTaskExecutor executor;
    @Mock
    private PlayAuthService playAuthService;
    @Mock
    private ZLMediaKitConfig zlmediaKitConfig;
    @Mock
    private SmartCdnService smartCdnService;
    @Mock
    private StringRedisTemplate redisTemplate;

    private ExZlmHookController controller;

    @BeforeEach
    void setUp() {
        controller = new ExZlmHookController(
                zlmHookService,
                streamService,
                streamInfoRepository,
                streamEventService,
                executor,
                playAuthService,
                zlmediaKitConfig,
                smartCdnService,
                redisTemplate
        );
        
        // Mock executor to run immediately
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executor).execute(any(Runnable.class));
    }

    @Test
    void onPublish_ShouldExtractLanIdAndCallEnsureRootNode() {
        // Arrange
        String streamId = "testStream";
        String lanId = "testLanId";
        String params = "token=123&lanId=" + lanId;
        String ip = "127.0.0.1";
        
        OnPublishHookParam param = new OnPublishHookParam();
        param.setStream(streamId);
        param.setParams(params);
        param.setIp(ip);

        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setStreamId(streamId);
        streamInfo.setStatus(StreamInfo.StreamStatus.CREATED);
        streamInfo.setUserId(1L);
        streamInfo.setPushUrl("pushUrl");

        when(streamInfoRepository.findByStreamId(streamId)).thenReturn(Optional.of(streamInfo));
        when(zlmediaKitConfig.generatePlayUrl(any(), any())).thenReturn("rtmpUrl");

        // Act
        controller.onPublish(param);

        // Assert
        verify(smartCdnService).ensureRootNode(eq(streamId), eq("rtmpUrl"), eq(lanId));
    }
    
    @Test
    void onPublish_ShouldHandleMissingLanId() {
        // Arrange
        String streamId = "testStreamNoLan";
        String params = "token=123";
        String ip = "127.0.0.1";
        
        OnPublishHookParam param = new OnPublishHookParam();
        param.setStream(streamId);
        param.setParams(params);
        param.setIp(ip);

        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setStreamId(streamId);
        streamInfo.setStatus(StreamInfo.StreamStatus.CREATED);
        streamInfo.setUserId(1L);
        streamInfo.setPushUrl("pushUrl");

        when(streamInfoRepository.findByStreamId(streamId)).thenReturn(Optional.of(streamInfo));
        when(zlmediaKitConfig.generatePlayUrl(any(), any())).thenReturn("rtmpUrl");

        // Act
        controller.onPublish(param);

        // Assert
        verify(smartCdnService).ensureRootNode(eq(streamId), eq("rtmpUrl"), isNull());
    }
}
