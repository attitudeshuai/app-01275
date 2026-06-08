package com.wms.annotation;

import java.lang.annotation.*;

/**
 * 权限控制注解
 * 用于Controller方法上，精确控制接口访问权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /**
     * 权限标识，如 "system:user:list"
     */
    String value();
}
