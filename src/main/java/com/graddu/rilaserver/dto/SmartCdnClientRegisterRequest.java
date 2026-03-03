package com.graddu.rilaserver.dto;

import java.util.List;

public class SmartCdnClientRegisterRequest {

    private String clientId;

    private String lanId;

    private String lanIp;

    private String mediamtxHttpUrl;

    private String mediamtxRtmpUrlPrefix;

    private List<String> capabilities;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getLanId() {
        return lanId;
    }

    public void setLanId(String lanId) {
        this.lanId = lanId;
    }

    public String getLanIp() {
        return lanIp;
    }

    public void setLanIp(String lanIp) {
        this.lanIp = lanIp;
    }

    public String getMediamtxHttpUrl() {
        return mediamtxHttpUrl;
    }

    public void setMediamtxHttpUrl(String mediamtxHttpUrl) {
        this.mediamtxHttpUrl = mediamtxHttpUrl;
    }

    public String getMediamtxRtmpUrlPrefix() {
        return mediamtxRtmpUrlPrefix;
    }

    public void setMediamtxRtmpUrlPrefix(String mediamtxRtmpUrlPrefix) {
        this.mediamtxRtmpUrlPrefix = mediamtxRtmpUrlPrefix;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }
}

