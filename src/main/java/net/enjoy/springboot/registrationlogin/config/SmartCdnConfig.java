package net.enjoy.springboot.registrationlogin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "smartcdn")
public class SmartCdnConfig {

    private boolean enabled = false;

    private int maxDepth = 3;

    private int maxSubscribersPerNode = 3;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getMaxSubscribersPerNode() {
        return maxSubscribersPerNode;
    }

    public void setMaxSubscribersPerNode(int maxSubscribersPerNode) {
        this.maxSubscribersPerNode = maxSubscribersPerNode;
    }
}
