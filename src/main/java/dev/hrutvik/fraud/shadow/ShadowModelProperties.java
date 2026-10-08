package dev.hrutvik.fraud.shadow;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="fraud.shadow-model")
public class ShadowModelProperties {
    private boolean enabled=true; private String version="candidate-2026-10"; private int reviewThreshold=40; private int declineThreshold=75;
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean enabled){this.enabled=enabled;}
    public String getVersion(){return version;} public void setVersion(String version){this.version=version;}
    public int getReviewThreshold(){return reviewThreshold;} public void setReviewThreshold(int value){reviewThreshold=value;}
    public int getDeclineThreshold(){return declineThreshold;} public void setDeclineThreshold(int value){declineThreshold=value;}
}
