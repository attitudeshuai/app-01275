package com.wms.aspect;

import com.wms.annotation.RequirePermission;
import com.wms.common.BusinessException;
import com.wms.entity.SysRole;
import com.wms.mapper.SysMenuMapper;
import com.wms.mapper.SysRoleMapper;
import com.wms.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限控制切面
 * 拦截带有 @RequirePermission 注解的方法，验证用户是否具有相应权限
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    
    /**
     * 超级管理员角色编码，可通过配置文件动态配置
     * 支持多个角色编码，用逗号分隔
     */
    @Value("${security.super-admin-roles:SUPER_ADMIN}")
    private String superAdminRoles;

    @Around("@annotation(com.wms.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 检查是否是超级管理员角色（从配置动态获取）
        Set<String> superAdminRoleSet = Arrays.stream(superAdminRoles.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        
        List<SysRole> userRoles = roleMapper.selectRolesByUserId(userId);
        boolean isSuperAdmin = userRoles.stream()
                .anyMatch(role -> superAdminRoleSet.contains(role.getRoleCode()));
        if (isSuperAdmin) {
            return joinPoint.proceed();
        }

        // 获取注解中的权限标识
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        String requiredPerm = annotation.value();

        // 查询用户权限列表
        List<String> userPerms = menuMapper.selectPermsByUserId(userId);

        // 检查是否拥有 *:*:* 通配权限
        if (userPerms.contains("*:*:*")) {
            return joinPoint.proceed();
        }

        // 检查是否拥有所需权限
        if (!userPerms.contains(requiredPerm)) {
            log.warn("用户 {} 无权限访问: {}", userId, requiredPerm);
            throw new BusinessException(403, "无权限访问");
        }

        return joinPoint.proceed();
    }
}
