package com.tiffin.api.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(com.tiffin.api..controller..*) || within(com.tiffin.api..service..*)")
    public void applicationPackages() {}

    @Around("applicationPackages()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String signature = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        if (log.isDebugEnabled()) {
            log.debug("ENTER {} args={}", signature, safeArgs(args));
        } else {
            log.info("ENTER {}", signature);
        }

        try {
            Object result = joinPoint.proceed();
            long took = System.currentTimeMillis() - start;
            if (log.isDebugEnabled()) {
                log.debug("EXIT {} took={}ms result={}", signature, took, safeResult(result));
            } else {
                log.info("EXIT {} took={}ms", signature, took);
            }
            return result;
        } catch (Throwable ex) {
            long took = System.currentTimeMillis() - start;
            log.error("EXCEPTION in {} after {}ms: {}", signature, took, ex.getMessage(), ex);
            throw ex;
        }
    }

    private String safeArgs(Object[] args) {
        try {
            return Arrays.deepToString(Arrays.stream(args)
                    .map(this::truncate)
                    .toArray());
        } catch (Exception e) {
            return "[unavailable]";
        }
    }

    private Object truncate(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }

    private String safeResult(Object result) {
        if (result == null) return "null";
        String s = String.valueOf(result);
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }
}
