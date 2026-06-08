package com.wms.interceptor;

import com.wms.common.BusinessException;
import com.wms.util.JwtUtil;
import com.wms.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(401, "未登录");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (jwtUtil.isTokenExpired(token)) {
            throw new BusinessException(401, "登录已过期");
        }
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        UserContext.setUserId(userId);
        UserContext.setUsername(username);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
