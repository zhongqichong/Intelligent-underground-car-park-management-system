package com.garage.aspect;

import com.garage.entity.OperationLog;
import com.garage.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {
    private final OperationLogRepository operationLogRepository;

    @After("execution(* com.garage.controller.AdminController.*(..))")
    public void logAdminAction(JoinPoint joinPoint) {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName() : "anonymous";
        operationLogRepository.save(OperationLog.builder()
                .username(username)
                .action(joinPoint.getSignature().getName())
                .method(joinPoint.getSignature().toShortString())
                .createdAt(LocalDateTime.now())
                .build());
    }
}
