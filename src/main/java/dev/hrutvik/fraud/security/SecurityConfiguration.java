package dev.hrutvik.fraud.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration {
    @Bean FilterRegistrationBean<ApiKeyAuthenticationFilter> disableContainerRegistration(ApiKeyAuthenticationFilter filter){var registration=new FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;}
    @Bean SecurityFilterChain security(HttpSecurity http,ApiKeyAuthenticationFilter apiKeys)throws Exception{
        return http.csrf(csrf->csrf.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->error(res,401,"UNAUTHENTICATED","Authentication is required")).accessDeniedHandler((req,res,ex)->error(res,403,"INSUFFICIENT_SCOPE","Credential lacks the required scope")))
            .authorizeHttpRequests(auth->auth
                .requestMatchers("/actuator/health","/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/v1/decisions").hasAuthority("SCOPE_DECISIONS_WRITE")
                .requestMatchers(HttpMethod.GET,"/v1/decisions/**").hasAuthority("SCOPE_DECISIONS_READ")
                .requestMatchers(HttpMethod.GET,"/v1/reviews/**").hasAuthority("SCOPE_REVIEWS_READ")
                .requestMatchers(HttpMethod.POST,"/v1/reviews/**").hasAuthority("SCOPE_REVIEWS_WRITE")
                .requestMatchers(HttpMethod.POST,"/v1/feedback").hasAuthority("SCOPE_FEEDBACK_WRITE")
                .requestMatchers(HttpMethod.GET,"/v1/feedback/**").hasAuthority("SCOPE_FEEDBACK_READ")
                .requestMatchers(HttpMethod.GET,"/v1/rule-sets/**").hasAuthority("SCOPE_RULES_READ")
                .requestMatchers(HttpMethod.POST,"/v1/rule-sets/**").hasAuthority("SCOPE_RULES_WRITE")
                .requestMatchers("/actuator/prometheus").hasAuthority("SCOPE_OPERATIONS_READ")
                .anyRequest().authenticated())
            .addFilterBefore(apiKeys,UsernamePasswordAuthenticationFilter.class).build();
    }
    private void error(HttpServletResponse response,int status,String code,String message)throws java.io.IOException{response.setStatus(status);response.setContentType(MediaType.APPLICATION_JSON_VALUE);response.getWriter().write("{\"code\":\""+code+"\",\"message\":\""+message+"\"}");}
}
