package com.legacyvault.config;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 基础安全响应头，覆盖敏感页面缓存、嵌入和 MIME 嗅探风险。
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "camera=(self), microphone=(), geolocation=()");
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; img-src 'self' data: https:; style-src 'self' 'unsafe-inline'; script-src 'self'; connect-src 'self'");

        String path = request.getRequestURI();
        if (path != null && (path.contains("/delivery/") || path.contains("/auth/") || path.contains("/content/"))) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
        }

        filterChain.doFilter(request, response);
    }
}
