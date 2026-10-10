package dev.hrutvik.fraud.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.*;

@Component @ConfigurationProperties(prefix="fraud.security")
public class ApiSecurityProperties {
    private List<Credential> credentials=new ArrayList<>();
    public List<Credential> getCredentials(){return credentials;} public void setCredentials(List<Credential> credentials){this.credentials=credentials;}
    public static class Credential {
        private String name; private String tenantId; private String keySha256; private Set<String> scopes=new HashSet<>();
        public String getName(){return name;} public void setName(String name){this.name=name;}
        public String getTenantId(){return tenantId;} public void setTenantId(String tenantId){this.tenantId=tenantId;}
        public String getKeySha256(){return keySha256;} public void setKeySha256(String keySha256){this.keySha256=keySha256;}
        public Set<String> getScopes(){return scopes;} public void setScopes(Set<String> scopes){this.scopes=scopes;}
    }
}
