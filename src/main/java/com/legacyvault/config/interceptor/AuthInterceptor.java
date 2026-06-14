package com.legacyvault.config.interceptor;

import com.legacyvault.common.Constants;
import com.legacyvault.common.Result;
import com.legacyvault.common.ResultCode;
import com.legacyvault.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证拦截器
 * 校验JWT Token，将用户信息注入请求上下文
 *
 * @author LegacyVault
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头获取Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            writeError(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        // 去除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 解析Token获取用户ID
            Long userId = jwtUtil.parseToken(token);
            if (userId == null) {
                writeError(response, ResultCode.TOKEN_INVALID);
                return false;
            }

            // 校验Redis中Token是否存在（支持主动注销）
            String redisKey = Constants.REDIS_TOKEN_PREFIX + userId;
            Object storedToken = redisTemplate.opsForValue().get(redisKey);
            if (storedToken == null || !token.equals(storedToken.toString())) {
                writeError(response, ResultCode.UNAUTHORIZED);
                return false;
            }

            // 将用户ID存入请求属性，供Controller使用
            request.setAttribute("currentUserId", userId);
            return true;

        } catch (Exception e) {
            log.warn("Token解析失败: {}", e.getMessage());
            writeError(response, ResultCode.TOKEN_INVALID);
            return false;
        }
    }

    /**
     * 写入错误响应
     */
    private void writeError(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        Result<?> result = Result.error(resultCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
