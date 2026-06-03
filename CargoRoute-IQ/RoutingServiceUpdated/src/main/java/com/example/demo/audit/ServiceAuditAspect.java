package com.example.demo.audit;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.demo.clients.AuditLogClient;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class ServiceAuditAspect {

    private final AuditLogClient auditLogClient;

    public ServiceAuditAspect(AuditLogClient auditLogClient) {
        this.auditLogClient = auditLogClient;
    }

    @AfterReturning(
        pointcut = "execution(* com.example.demo.serviceimpl..*.create*(..)) || " +
            "execution(* com.example.demo.serviceimpl..*.save*(..)) || " +
            "execution(* com.example.demo.serviceimpl..*.insert*(..)) || " +
            "execution(* com.example.demo.serviceimpl..*.update*(..)) || " +
            "execution(* com.example.demo.serviceimpl..*.edit*(..)) || " +
            "execution(* com.example.demo.serviceimpl..*.delete*(..)) || " +
            "execution(* com.example.demo.serviceimpl..*.remove*(..)) || " +
            "execution(* com.example.demo.serviceImpl..*.create*(..)) || " +
            "execution(* com.example.demo.serviceImpl..*.save*(..)) || " +
            "execution(* com.example.demo.serviceImpl..*.insert*(..)) || " +
            "execution(* com.example.demo.serviceImpl..*.update*(..)) || " +
            "execution(* com.example.demo.serviceImpl..*.edit*(..)) || " +
            "execution(* com.example.demo.serviceImpl..*.delete*(..)) || " +
            "execution(* com.example.demo.serviceImpl..*.remove*(..)) || " +
            "execution(* com.example.demo.ServiceImpl..*.create*(..)) || " +
            "execution(* com.example.demo.ServiceImpl..*.save*(..)) || " +
            "execution(* com.example.demo.ServiceImpl..*.insert*(..)) || " +
            "execution(* com.example.demo.ServiceImpl..*.update*(..)) || " +
            "execution(* com.example.demo.ServiceImpl..*.edit*(..)) || " +
            "execution(* com.example.demo.ServiceImpl..*.delete*(..)) || " +
            "execution(* com.example.demo.ServiceImpl..*.remove*(..)) || " +
            "execution(* com.example.demo.servicesImplementation..*.create*(..)) || " +
            "execution(* com.example.demo.servicesImplementation..*.save*(..)) || " +
            "execution(* com.example.demo.servicesImplementation..*.insert*(..)) || " +
            "execution(* com.example.demo.servicesImplementation..*.update*(..)) || " +
            "execution(* com.example.demo.servicesImplementation..*.edit*(..)) || " +
            "execution(* com.example.demo.servicesImplementation..*.delete*(..)) || " +
            "execution(* com.example.demo.servicesImplementation..*.remove*(..))",
        returning = "result"
    )
    public void afterServiceSuccess(JoinPoint joinPoint, Object result) {
        Long userId = getUserIdFromRequest();
        Long resourceId = extractResourceId(result, joinPoint.getArgs());
        String action = mapAction(joinPoint.getSignature().getName());
        String resourceType = mapResourceType(joinPoint.getTarget().getClass().getSimpleName());

        if (!isMutatingAction(action)) {
            return;
        }

        if (userId == null) {
            userId = 0L;
        }
        if (resourceId == null) {
            resourceId = 0L;
        }

        auditLogClient.log(
            userId,
            action,
            resourceType,
            resourceId,
            buildDetails(action, resourceType, resourceId, joinPoint, result)
        );
    }

    private Long extractResourceId(Object result, Object[] args) {
        Long fromResult = extractIdFromObject(result);
        if (fromResult != null) {
            return fromResult;
        }
        return extractResourceId(args);
    }

    private Long getUserIdFromRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            Object userId = request.getAttribute("userId");
            if (userId instanceof Long value) {
                return value;
            }
            if (userId instanceof Integer value) {
                return value.longValue();
            }
            if (userId instanceof String value) {
                try {
                    return Long.valueOf(value);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private Long extractResourceId(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            Long fromObject = extractIdFromObject(arg);
            if (fromObject != null) {
                return fromObject;
            }
            if (arg instanceof Long value) {
                return value;
            }
            if (arg instanceof Integer value) {
                return value.longValue();
            }
        }
        return null;
    }

    private String mapAction(String methodName) {
        String m = methodName.toLowerCase();
        if (m.startsWith("create") || m.startsWith("save") || m.startsWith("insert") || m.startsWith("register")) {
            return "CREATE";
        }
        if (m.startsWith("update") || m.startsWith("edit")) {
            return "UPDATE";
        }
        if (m.startsWith("delete") || m.startsWith("remove")) {
            return "DELETE";
        }
        return "READ";
    }

    private String mapResourceType(String className) {
        return className
                .replace("ServiceImpl", "")
                .replace("serviceImpl", "")
                .replace("Serviceimpl", "")
                .replace("Service", "")
                .toUpperCase();
    }

    private boolean isMutatingAction(String action) {
        return "CREATE".equals(action) || "UPDATE".equals(action) || "DELETE".equals(action);
    }

    private Long extractIdFromObject(Object source) {
        if (source == null) {
            return null;
        }
        Method[] methods = source.getClass().getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (method.getParameterCount() != 0 || !name.startsWith("get")) {
                continue;
            }
            if (!(name.endsWith("ID") || name.endsWith("Id"))) {
                continue;
            }
            try {
                Object value = method.invoke(source);
                if (value instanceof Number number) {
                    return number.longValue();
                }
            } catch (Exception ignored) {
                // ignore and continue searching
            }
        }
        return null;
    }

    private String buildDetails(String action, String resourceType, Long resourceId, JoinPoint joinPoint, Object result) {
        StringBuilder details = new StringBuilder()
                .append(action)
                .append(" ")
                .append(resourceType)
                .append(" resourceId=")
                .append(resourceId)
                .append(" via ")
                .append(joinPoint.getSignature().toShortString());

        Object snapshot = result;
        if (snapshot == null) {
            Object[] args = joinPoint.getArgs();
            if (args != null) {
                for (Object arg : args) {
                    if (arg != null) {
                        snapshot = arg;
                        break;
                    }
                }
            }
        }

        if (snapshot != null) {
            // WHY: include a lightweight payload snapshot for traceability.
            details.append(" | payload=").append(String.valueOf(snapshot));
        }

        return details.toString();
    }
}