package com.example.demo.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.AuditLogDTO;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuditLogClient {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogClient.class);

    private final RestTemplate restTemplate;
    private final String auditCreateUrl;

    public AuditLogClient(RestTemplate restTemplate,
                          @Value("${audit.log.create-url:http://IDENTITY-ACCESS-MANAGEMENT/cargoRoute/internal/auditLogs/create}") String auditCreateUrl) {
        this.restTemplate = restTemplate;
        this.auditCreateUrl = auditCreateUrl;
    }

    public void log(Long userId, String action, String resourceType, Long resourceId, String details) {
        try {
            AuditLogDTO dto = new AuditLogDTO();
            dto.setUserID(userId);
            dto.setAction(action);
            dto.setResourceType(resourceType);
            dto.setResourceID(resourceId);
            dto.setDetails(details);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String authorization = resolveAuthorizationHeader();
            if (authorization != null && !authorization.isBlank()) {
                headers.set("Authorization", authorization);
            }

            HttpEntity<AuditLogDTO> request = new HttpEntity<>(dto, headers);
            restTemplate.postForObject(auditCreateUrl, request, AuditLogDTO.class);
        } catch (Exception ex) {
            logger.warn("Audit publish failed", ex);
        }
    }

    private String resolveAuthorizationHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            return request.getHeader("Authorization");
        }
        return null;
    }
}