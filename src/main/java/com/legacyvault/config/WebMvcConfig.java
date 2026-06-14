package com.legacyvault.config;

import com.legacyvault.config.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册拦截器等
 *
 * @author LegacyVault
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                // 排除不需要认证的路径
                .excludePathPatterns(
                        "/auth/register",
                        "/auth/login",
                        "/auth/login/code",
                        "/auth/send-code",
                        "/auth/verify-code",
                        "/heir/confirm/**",
                        "/delivery/verify/**",
                        "/delivery/access/**",
                        "/plan/list",
                        "/error"
                );
    }
}
