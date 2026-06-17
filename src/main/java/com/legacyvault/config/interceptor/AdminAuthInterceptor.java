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
 * 管理员专用认证拦截器
 * 校验管理员 JWT Token（type=admin），与用户 Token 完全隔离
 *
 * @author LegacyVault
 */
@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            writeError(response, ResultCode.UNAUTHORIZED);
            return false;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 必须是管理员 token
            String type = jwtUtil.parseTokenType(token);
            if (!"admin".equals(type)) {
                writeError(response, ResultCode.FORBIDDEN);
                return false;
            }

            Long adminId = jwtUtil.parseToken(token);
            if (adminId == null) {
                writeError(response, ResultCode.TOKEN_INVALID);
                return false;
            }

            String redisKey = Constants.REDIS_ADMIN_TOKEN_PREFIX + adminId;
            Object storedToken = redisTemplate.opsForValue().get(redisKey);
            if (storedToken == null || !token.equals(storedToken.toString())) {
                writeError(response, ResultCode.UNAUTHORIZED);
                return false;
            }

            request.setAttribute("currentAdminId", adminId);
            return true;

        } catch (Exception e) {
            log.warn("管理员Token解析失败: {}", e.getMessage());
            writeError(response, ResultCode.TOKEN_INVALID);
            return false;
        }
    }

    private void writeError(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        Result<?> result = Result.error(resultCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
