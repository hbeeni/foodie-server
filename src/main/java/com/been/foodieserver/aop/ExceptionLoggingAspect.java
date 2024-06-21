package com.been.foodieserver.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class ExceptionLoggingAspect {

    @Before("execution(* com.been.foodieserver.exception.handler.GlobalExceptionHandler.*(..))")
    public void doBefore(JoinPoint joinPoint) {
        try {
            printLog((Exception) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Error occurs! [ExceptionLoggingAspect: {}]", e.getMessage());
        }
    }

    private static void printLog(Exception ex) {
        log.error("Error occurs! [{}: {}]", ex.getClass().getSimpleName(), ex.getMessage(), ex);
    }
}
