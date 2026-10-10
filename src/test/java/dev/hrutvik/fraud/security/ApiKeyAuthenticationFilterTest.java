package dev.hrutvik.fraud.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthenticationFilterTest {
    private final ApiKeyAuthenticationFilter filter=new ApiKeyAuthenticationFilter(properties());
    @Test void authenticatesKeyAndBindsTenant()throws Exception{var request=new MockHttpServletRequest("POST","/v1/decisions");request.addHeader("X-API-Key","demo-merchant-key");request.addHeader("X-Tenant-Id","merchant-demo");var response=new MockHttpServletResponse();var chain=new MockFilterChain();filter.doFilter(request,response,chain);assertThat(response.getStatus()).isEqualTo(200);assertThat(chain.getRequest()).isNotNull();}
    @Test void rejectsMissingKey()throws Exception{var request=new MockHttpServletRequest("POST","/v1/decisions");var response=new MockHttpServletResponse();filter.doFilter(request,response,new MockFilterChain());assertThat(response.getStatus()).isEqualTo(401);assertThat(response.getContentAsString()).contains("UNAUTHENTICATED");}
    @Test void rejectsCrossTenantCredentialUse()throws Exception{var request=new MockHttpServletRequest("POST","/v1/decisions");request.addHeader("X-API-Key","demo-merchant-key");request.addHeader("X-Tenant-Id","another-tenant");var response=new MockHttpServletResponse();filter.doFilter(request,response,new MockFilterChain());assertThat(response.getStatus()).isEqualTo(403);assertThat(response.getContentAsString()).contains("TENANT_ACCESS_DENIED");}
    @Test void healthProbeRemainsPublic()throws Exception{var request=new MockHttpServletRequest("GET","/actuator/health");var response=new MockHttpServletResponse();var chain=new MockFilterChain();filter.doFilter(request,response,chain);assertThat(chain.getRequest()).isNotNull();}
    private ApiSecurityProperties properties(){var p=new ApiSecurityProperties();var c=new ApiSecurityProperties.Credential();c.setName("merchant-demo");c.setTenantId("merchant-demo");c.setKeySha256("d09453e5d5a47120605987b7b9cda1e9469ccf81d6864edb0c9af39287900749");c.setScopes(Set.of("DECISIONS_WRITE"));p.setCredentials(List.of(c));return p;}
}
