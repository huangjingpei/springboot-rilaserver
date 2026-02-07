package net.enjoy.springboot.registrationlogin.service;

import net.enjoy.springboot.registrationlogin.dto.SmartCdnClientRegisterRequest;
import net.enjoy.springboot.registrationlogin.dto.SmartCdnPlayUrlResponse;
import net.enjoy.springboot.registrationlogin.dto.SmartCdnRelayRegisterRequest;

public interface SmartCdnService {

    void registerClient(SmartCdnClientRegisterRequest request);

    boolean registerRelayNode(SmartCdnRelayRegisterRequest request);

    SmartCdnPlayUrlResponse getBestPlayUrl(String streamId, String lanId, String exclude);

    void ensureRootNode(String streamId, String rtmpUrl, String lanId);

    int deleteClient(String clientId);

    int deleteRelays(String streamId, String lanId);
}
