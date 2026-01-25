package net.enjoy.springboot.registrationlogin.dto;

public class SmartCdnRelayRegisterRequest {

    private String clientId;

    private String lanId;

    private String streamId;

    private String parentUrl;

    private String mediamtxPullUrl;

    private String mediamtxPlayUrl;

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

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    public String getMediamtxPullUrl() {
        return mediamtxPullUrl;
    }

    public void setMediamtxPullUrl(String mediamtxPullUrl) {
        this.mediamtxPullUrl = mediamtxPullUrl;
    }

    public String getMediamtxPlayUrl() {
        return mediamtxPlayUrl;
    }

    public void setMediamtxPlayUrl(String mediamtxPlayUrl) {
        this.mediamtxPlayUrl = mediamtxPlayUrl;
    }
}

