package dev.hrutvik.fraud.observability;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.*;
import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {
    private final CorrelationIdFilter filter=new CorrelationIdFilter();
    @Test void preservesSafeCallerCorrelationId()throws Exception{var request=new MockHttpServletRequest("GET","/v1/decisions/id");request.addHeader(CorrelationIdFilter.HEADER,"checkout-42");var response=new MockHttpServletResponse();var chain=new MockFilterChain();filter.doFilter(request,response,chain);assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("checkout-42");assertThat(MDC.get("correlationId")).isNull();}
    @Test void replacesUnsafeCorrelationId()throws Exception{var request=new MockHttpServletRequest("GET","/v1/decisions/id");request.addHeader(CorrelationIdFilter.HEADER,"bad id\nforged");var response=new MockHttpServletResponse();filter.doFilter(request,response,new MockFilterChain());assertThat(response.getHeader(CorrelationIdFilter.HEADER)).matches("[0-9a-f-]{36}");}
    @Test void generatesCorrelationIdWhenMissing()throws Exception{var request=new MockHttpServletRequest("GET","/actuator/health");var response=new MockHttpServletResponse();filter.doFilter(request,response,new MockFilterChain());assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isNotBlank();}
}
