package com.example.checkin.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Ghi log cho tất cả các phương thức trong service
    @Around("execution(* com.example..service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        UUID uuid = UUID.randomUUID();
        logger.info("👉 ID: {}  Entering method: {} with arguments: {}",uuid, methodName, Arrays.toString(args));
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            logger.info("✅ ID: {} Exiting method: {} with result: {}, took {} ms",uuid, methodName, result, duration);
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;
            logger.info("❌ ID: {} Exception method: {}  took {} ms",uuid, methodName, duration);

            throw e;
        }
    }
}