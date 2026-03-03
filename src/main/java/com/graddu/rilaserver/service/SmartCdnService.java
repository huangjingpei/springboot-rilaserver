package com.graddu.rilaserver.service;

import com.graddu.rilaserver.dto.SmartCdnClientRegisterRequest;
import com.graddu.rilaserver.dto.SmartCdnPlayUrlResponse;
import com.graddu.rilaserver.dto.SmartCdnRelayRegisterRequest;

public interface SmartCdnService {

    void registerClient(SmartCdnClientRegisterRequest request);

    boolean registerRelayNode(SmartCdnRelayRegisterRequest request);

    SmartCdnPlayUrlResponse getBestPlayUrl(String streamId, String lanId, String exclude);

    void ensureRootNode(String streamId, String rtmpUrl, String lanId);

    int deleteClient(String clientId);

    int deleteRelays(String streamId, String lanId);
}
