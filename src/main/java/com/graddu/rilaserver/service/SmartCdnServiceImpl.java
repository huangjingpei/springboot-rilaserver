package com.graddu.rilaserver.service;

import com.graddu.rilaserver.config.SmartCdnConfig;
import com.graddu.rilaserver.dto.SmartCdnClientRegisterRequest;
import com.graddu.rilaserver.dto.SmartCdnPlayUrlResponse;
import com.graddu.rilaserver.dto.SmartCdnRelayRegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class SmartCdnServiceImpl implements SmartCdnService {

    @Autowired
    private SmartCdnConfig smartCdnConfig;

    @Autowired
    @Qualifier("smartCdnRedisService")
    private SmartCdnService redisService;

    @Autowired
    @Qualifier("smartCdnMysqlService")
    private SmartCdnService mysqlService;

    private SmartCdnService getDelegate() {
        if ("mysql".equalsIgnoreCase(smartCdnConfig.getStorageType())) {
            return mysqlService;
        }
        return redisService;
    }

    @Override
    public void registerClient(SmartCdnClientRegisterRequest request) {
        getDelegate().registerClient(request);
    }

    @Override
    public boolean registerRelayNode(SmartCdnRelayRegisterRequest request) {
        return getDelegate().registerRelayNode(request);
    }

    @Override
    public SmartCdnPlayUrlResponse getBestPlayUrl(String streamId, String lanId, String exclude) {
        return getDelegate().getBestPlayUrl(streamId, lanId, exclude);
    }

    @Override
    public void ensureRootNode(String streamId, String rtmpUrl, String lanId) {
        getDelegate().ensureRootNode(streamId, rtmpUrl, lanId);
    }

    @Override
    public int deleteClient(String clientId) {
        return getDelegate().deleteClient(clientId);
    }

    @Override
    public int deleteRelays(String streamId, String lanId) {
        return getDelegate().deleteRelays(streamId, lanId);
    }
}
