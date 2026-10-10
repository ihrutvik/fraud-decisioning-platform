package dev.hrutvik.fraud.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private final ApiSecurityProperties properties;
    public ApiKeyAuthenticationFilter(ApiSecurityProperties properties){this.properties=properties;}
    @Override protected boolean shouldNotFilter(HttpServletRequest request){return request.getRequestURI().equals("/actuator/health")||request.getRequestURI().startsWith("/actuator/health/");}
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
        String raw=request.getHeader("X-API-Key");
        if(raw==null||raw.isBlank()){unauthorized(response,"API key is required");return;}
        byte[] supplied=sha256(raw); ApiSecurityProperties.Credential match=null;
        for(var candidate:properties.getCredentials()){byte[] expected=hex(candidate.getKeySha256());if(expected.length==supplied.length&&MessageDigest.isEqual(expected,supplied)){match=candidate;break;}}
        if(match==null){unauthorized(response,"API key is invalid");return;}
        String requestedTenant=request.getHeader("X-Tenant-Id");
        if(requestedTenant!=null&&!MessageDigest.isEqual(match.getTenantId().getBytes(StandardCharsets.UTF_8),requestedTenant.getBytes(StandardCharsets.UTF_8))){forbidden(response,"Credential cannot access this tenant");return;}
        var authorities=match.getScopes().stream().map(scope->new SimpleGrantedAuthority("SCOPE_"+scope)).toList();
        var authentication=new UsernamePasswordAuthenticationToken(match.getName(),null,authorities); authentication.setDetails(match.getTenantId()); SecurityContextHolder.getContext().setAuthentication(authentication);
        try{chain.doFilter(request,response);}finally{SecurityContextHolder.clearContext();}
    }
    private byte[] sha256(String value){try{return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));}catch(Exception impossible){throw new IllegalStateException(impossible);}}
    private byte[] hex(String value){try{return HexFormat.of().parseHex(value);}catch(IllegalArgumentException invalid){return new byte[0];}}
    private void unauthorized(HttpServletResponse response,String message)throws IOException{error(response,401,"UNAUTHENTICATED",message);}
    private void forbidden(HttpServletResponse response,String message)throws IOException{error(response,403,"TENANT_ACCESS_DENIED",message);}
    private void error(HttpServletResponse response,int status,String code,String message)throws IOException{response.setStatus(status);response.setContentType(MediaType.APPLICATION_JSON_VALUE);response.getWriter().write("{\"code\":\""+code+"\",\"message\":\""+message+"\"}");}
}
