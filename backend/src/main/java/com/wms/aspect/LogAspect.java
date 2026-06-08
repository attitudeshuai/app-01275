package com.wms.aspect;

import cn.hutool.json.JSONUtil;
import com.wms.annotation.Log;
import com.wms.entity.SysLog;
import com.wms.mapper.SysLogMapper;
import com.wms.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysLogMapper sysLogMapper;

    @Around("@annotation(com.wms.annotation.Log)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = point.proceed();
        long execTime = System.currentTimeMillis() - startTime;
        saveLog(point, execTime);
        return result;
    }

    private void saveLog(ProceedingJoinPoint point, long execTime) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);

            SysLog sysLog = new SysLog();
            sysLog.setModule(logAnnotation.module());
            sysLog.setOperation(logAnnotation.operation());
            sysLog.setMethod(point.getTarget().getClass().getName() + "." + method.getName());
            
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                String params = JSONUtil.toJsonStr(args[0]);
                sysLog.setParams(params.length() > 2000 ? params.substring(0, 2000) : params);
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                sysLog.setIp(getIpAddress(request));
            }

            sysLog.setUserId(UserContext.getUserId());
            sysLog.setUsername(UserContext.getUsername());
            sysLog.setExecTime(execTime);
            sysLog.setCreateTime(LocalDateTime.now());

            sysLogMapper.insert(sysLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
