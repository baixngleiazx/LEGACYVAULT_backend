package com.legacyvault.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Result;
import com.legacyvault.module.admin.dto.AdminLoginRequest;
import com.legacyvault.module.admin.dto.AdminLoginResponse;
import com.legacyvault.module.admin.entity.AdminUser;
import com.legacyvault.module.admin.mapper.AdminUserMapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.config.LegacyVaultProperties;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.util.JwtUtil;
import com.legacyvault.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 管理员认证控制器
 * 独立登录入口，与用户体系物理隔离
 *
 * @author LegacyVault
 */
@Slf4j
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private LegacyVaultProperties properties;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 管理员登录
     * POST /api/admin/auth/login
     */
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request,
                                            HttpServletRequest httpRequest) {
        AdminUser admin = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, request.getUsername()));
        if (admin == null) {
            throw new BusinessException("账号或密码错误" );
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (!SecurityUtil.verifyPassword(request.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号或密码错误");
        }

        // 生成管理员 JWT（type=admin）
        String token = jwtUtil.generateToken(admin.getId(), "admin");
        String redisKey = Constants.REDIS_ADMIN_TOKEN_PREFIX + admin.getId();
        redisTemplate.opsForValue().set(redisKey, token,
                properties.getJwt().getExpireHours(), TimeUnit.HOURS);

        admin.setLastLoginAt(LocalDateTime.now());
        adminUserMapper.updateById(admin);

        AdminLoginResponse resp = new AdminLoginResponse();
        resp.setToken(token);
        resp.setAdminId(admin.getId());
        resp.setUsername(admin.getUsername());
        resp.setRealName(admin.getRealName());
        resp.setRole(admin.getRole());

        auditLogService.log(admin.getId(), Constants.AUDIT_MODULE_ADMIN, "login",
                String.format("{\"username\":\"%s\"}", admin.getUsername()));
        log.info("管理员登录 | adminId={} | username={}", admin.getId(), admin.getUsername());
        return Result.success("登录成功", resp);
    }

    /**
     * 管理员登出
     * POST /api/admin/auth/logout
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("currentAdminId");
        if (adminId != null) {
            String redisKey = Constants.REDIS_ADMIN_TOKEN_PREFIX + adminId;
            redisTemplate.delete(redisKey);
            auditLogService.log(adminId, Constants.AUDIT_MODULE_ADMIN, "logout", null);
        }
        return Result.success("登出成功");
    }
}
