package com.legacyvault.config;

import com.legacyvault.config.interceptor.AdminAuthInterceptor;
import com.legacyvault.config.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册用户 + 管理员两套独立的认证拦截器
 *
 * @author LegacyVault
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户认证拦截器：覆盖所有路径，排除管理员路径 + 公开路径
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 管理员接口（由 AdminAuthInterceptor 处理）
                        "/admin/**",
                        // 公开认证接口
                        "/auth/register",
                        "/auth/login",
                        "/auth/login/code",
                        "/auth/send-code",
                        "/auth/verify-code",
                        // 继承人确认 / 交付核验（公开）
                        "/heir/confirm/**",
                        "/delivery/verify/**",
                        "/delivery/access/**",
                        // 套餐列表（公开）
                        "/plan/list",
                        // 错误页
                        "/error"
                );

        // 管理员认证拦截器：仅覆盖 /admin/**，排除登录接口
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/auth/login"
                );
    }
}
