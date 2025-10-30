package com.tiffin.api.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String USER = "user";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
    String incoming = request.getHeader("X-Request-Id");
    String traceId = (incoming != null && !incoming.isBlank())
        ? incoming.trim()
        : UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID, traceId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
        MDC.put(USER, user);

        String method = request.getMethod();
        String path = request.getRequestURI();

        try {
            log.info("--> {} {}", method, path);
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus();
            response.setHeader("X-Trace-Id", traceId);
            log.info("<-- {} {} {} {}ms", method, path, status, duration);
            MDC.remove(TRACE_ID);
            MDC.remove(USER);
        }
    }
}
