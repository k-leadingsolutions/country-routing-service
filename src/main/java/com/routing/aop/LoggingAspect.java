package com.routing.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    
    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        
        log.info("Executing: {} with args: {}", methodName, Arrays.toString(joinPoint.getArgs()));
        
        Object result = joinPoint.proceed();
        
        long duration = System.currentTimeMillis() - start;
        log.info("Completed: {} in {}ms", methodName, duration);
        
        return result;
    }
}