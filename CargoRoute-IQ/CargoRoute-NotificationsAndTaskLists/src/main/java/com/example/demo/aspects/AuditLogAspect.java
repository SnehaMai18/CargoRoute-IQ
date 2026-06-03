package com.example.demo.aspects;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.demo.annotations.AuditableAction;
import com.example.demo.clients.AuditLogClient;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogAspect.class);

    private final AuditLogClient auditLogClient;

    public AuditLogAspect(AuditLogClient auditLogClient) {
        this.auditLogClient = auditLogClient;
    }

    @AfterReturning(pointcut = "@annotation(auditableAction)", returning = "result")
    public void logAfterSuccess(JoinPoint joinPoint, AuditableAction auditableAction, Object result) {
        try {
            Long userId = resolveUserId(joinPoint, result, auditableAction.userIdArgIndex());
            Long resourceId = resolveResourceId(joinPoint, result, auditableAction.resourceIdArgIndex());
            String details = auditableAction.details().isBlank()
                ? "Action executed via " + joinPoint.getSignature().getName()
                : auditableAction.details();

            auditLogClient.log(
                userId,
                auditableAction.action().name(),
                auditableAction.resourceType().name(),
                resourceId,
                details
            );
        } catch (Exception ex) {
            logger.warn("Failed to send audit log for {}", joinPoint.getSignature().toShortString(), ex);
        }
    }

    private Long resolveUserId(JoinPoint joinPoint, Object result, int userIdArgIndex) {
        Long requestUserId = getUserIdFromRequest();
        if (requestUserId != null) {
            return requestUserId;
        }

        Object[] args = joinPoint.getArgs();
        if (userIdArgIndex >= 0 && userIdArgIndex < args.length) {
            Long value = extractLong(args[userIdArgIndex]);
            if (value != null) {
                return value;
            }
        }

        Long fromResult = extractLongProperty(result, "getUserID", "getAssignedTo", "getId");
        if (fromResult != null) {
            return fromResult;
        }

        for (Object arg : args) {
            Long fromArg = extractLongProperty(arg, "getUserID", "getAssignedTo", "getId");
            if (fromArg != null) {
                return fromArg;
            }
        }

        return null;
    }

    private Long resolveResourceId(JoinPoint joinPoint, Object result, int resourceIdArgIndex) {
        Object[] args = joinPoint.getArgs();

        if (resourceIdArgIndex >= 0 && resourceIdArgIndex < args.length) {
            Long value = extractLong(args[resourceIdArgIndex]);
            if (value != null) {
                return value;
            }
        }

        Long fromResult = extractLongProperty(result, "getNotificationID", "getTaskID", "getId");
        if (fromResult != null) {
            return fromResult;
        }

        return null;
    }

    private Long getUserIdFromRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }

        HttpServletRequest request = servletAttributes.getRequest();
        return extractLong(request.getAttribute("userId"));
    }

    private Long extractLongProperty(Object source, String... getterNames) {
        if (source == null || getterNames == null) {
            return null;
        }

        for (String getterName : getterNames) {
            try {
                Method method = source.getClass().getMethod(getterName);
                Long value = extractLong(method.invoke(source));
                if (value != null) {
                    return value;
                }
            } catch (Exception ignored) {
                // try next getter
            }
        }

        return null;
    }

    private Long extractLong(Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Long.valueOf(stringValue);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
