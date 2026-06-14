package com.legacyvault.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.config.LegacyVaultProperties;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.mock.MockEmailService;
import com.legacyvault.mock.MockSmsService;
import com.legacyvault.module.auth.dto.*;
import com.legacyvault.module.auth.entity.RecoveryCode;
import com.legacyvault.module.auth.entity.TotpConfig;
import com.legacyvault.module.auth.entity.VerificationCode;
import com.legacyvault.module.auth.mapper.RecoveryCodeMapper;
import com.legacyvault.module.auth.mapper.TotpConfigMapper;
import com.legacyvault.module.auth.mapper.VerificationCodeMapper;
import com.legacyvault.module.auth.service.AuthService;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.heartbeat.entity.HeartbeatConfig;
import com.legacyvault.module.heartbeat.mapper.HeartbeatConfigMapper;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.UserMapper;
import com.legacyvault.util.JwtUtil;
import com.legacyvault.util.PhoneUtil;
import com.legacyvault.util.SecurityUtil;
import com.legacyvault.util.TotpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /** 验证码最短发送间隔（秒） */
    private static final long CODE_SEND_INTERVAL_SECONDS = 60;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeMapper verificationCodeMapper;

    @Autowired
    private TotpConfigMapper totpConfigMapper;

    @Autowired
    private RecoveryCodeMapper recoveryCodeMapper;

    @Autowired
    private HeartbeatConfigMapper heartbeatConfigMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private LegacyVaultProperties properties;

    @Autowired
    private MockEmailService mockEmailService;

    @Autowired
    private MockSmsService mockSmsService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 发送验证码
     * 支持自动识别渠道、频率限制
     */
    @Override
    public void sendVerifyCode(SendCodeRequest request) {
        String target = request.getTarget().trim();
        String codeType = request.getCodeType();
        String channel = request.getChannel();

        // 自动识别目标类型和渠道
        String targetType = PhoneUtil.detectTargetType(target);
        if (targetType == null) {
            throw new BusinessException(ResultCode.TARGET_FORMAT_ERROR);
        }
        if (channel == null || channel.isEmpty()) {
            channel = PhoneUtil.getChannelByTargetType(targetType);
        }

        // 校验手机号格式
        if ("phone".equals(targetType) && !PhoneUtil.isValidPhone(target)) {
            throw new BusinessException(ResultCode.PHONE_FORMAT_ERROR);
        }

        // 频率限制：同一目标 60 秒内只能发送一次
        String rateLimitKey = "code:rate_limit:" + target;
        Boolean canSend = redisTemplate.opsForValue().setIfAbsent(rateLimitKey, "1",
                CODE_SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
        if (canSend == null || !canSend) {
            throw new BusinessException(ResultCode.CODE_SEND_TOO_FREQUENT);
        }

        // 生成6位随机验证码
        String code = SecurityUtil.generateVerifyCode(6);

        // 保存验证码记录（5分钟有效）
        VerificationCode vc = new VerificationCode();
        vc.setTarget(target);
        vc.setCode(code);
        vc.setCodeType(codeType);
        vc.setChannel(channel);
        vc.setIsUsed(0);
        vc.setExpireAt(LocalDateTime.now().plusMinutes(5));
        vc.setMockData(String.format("{\"mock\":true,\"channel\":\"%s\",\"target\":\"%s\"}", channel, target));
        verificationCodeMapper.insert(vc);

        // 同时存入Redis（方便快速校验）
        String redisKey = Constants.REDIS_CODE_PREFIX + codeType + ":" + target;
        redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        // 通过Mock渠道发送
        if ("email".equals(channel)) {
            mockEmailService.sendVerifyCode(target, code, codeType);
        } else if ("sms".equals(channel)) {
            mockSmsService.sendVerifyCode(target, code);
        }

        auditLogService.log(null, Constants.AUDIT_MODULE_AUTH, "send_verify_code",
                String.format("{\"target\":\"%s\",\"type\":\"%s\",\"channel\":\"%s\"}", target, codeType, channel));
    }

    /**
     * 用户注册
     * 支持邮箱注册和手机号注册（二选一）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        String phone = request.getPhone() != null ? request.getPhone().trim() : null;

        // 判断注册方式：至少填写邮箱或手机号之一
        boolean hasEmail = email != null && !email.isEmpty();
        boolean hasPhone = phone != null && !phone.isEmpty();
        if (!hasEmail && !hasPhone) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "邮箱和手机号至少填写一项");
        }

        // 邮箱格式校验
        if (hasEmail && !PhoneUtil.isValidEmail(email)) {
            throw new BusinessException(ResultCode.TARGET_FORMAT_ERROR, "邮箱格式不正确");
        }

        // 手机号格式校验
        if (hasPhone && !PhoneUtil.isValidPhone(phone)) {
            throw new BusinessException(ResultCode.PHONE_FORMAT_ERROR);
        }

        // 验证码校验目标：优先使用手机号，否则使用邮箱
        String verifyTarget = hasPhone ? phone : email;
        verifyCode(verifyTarget, request.getVerifyCode(), Constants.CODE_TYPE_REGISTER);

        // 检查邮箱是否已注册
        if (hasEmail) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, email));
            if (emailCount > 0) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "该邮箱已被注册");
            }
        }

        // 检查手机号是否已注册
        if (hasPhone) {
            Long phoneCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
            if (phoneCount > 0) {
                throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS);
            }
        }

        // 创建用户
        User user = new User();
        if (hasEmail) {
            user.setEmail(email);
        }
        if (hasPhone) {
            user.setPhone(phone);
        }
        user.setPasswordHash(SecurityUtil.hashPassword(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : "用户" + System.currentTimeMillis() % 10000);
        user.setStatus(Constants.USER_STATUS_NORMAL);
        user.setPlanId(1L); // 默认Free套餐
        user.setTotpBound(Constants.TOTP_NOT_BOUND);
        user.setBiometricBound(0);
        user.setKycStatus(Constants.KYC_STATUS_NONE);
        user.setSecurityScore(0);
        user.setTravelModeEnabled(0);
        userMapper.insert(user);

        // 初始化心跳配置（默认90天周期）
        HeartbeatConfig heartbeatConfig = new HeartbeatConfig();
        heartbeatConfig.setUserId(user.getId());
        heartbeatConfig.setCheckInPeriodDays(properties.getHeartbeat().getDefaultPeriodDays());
        heartbeatConfig.setNextDeadline(LocalDateTime.now().plusDays(properties.getHeartbeat().getDefaultPeriodDays()));
        heartbeatConfig.setRemind14dSent(0);
        heartbeatConfig.setRemind7dSent(0);
        heartbeatConfig.setRemind3dSent(0);
        heartbeatConfig.setRemind0dSent(0);
        heartbeatConfigMapper.insert(heartbeatConfig);

        auditLogService.log(user.getId(), Constants.AUDIT_MODULE_AUTH, "register",
                String.format("{\"email\":\"%s\",\"phone\":\"%s\"}", email, phone));
        log.info("用户注册成功 | userId={} | email={} | phone={}", user.getId(), email, phone);
    }

    /**
     * 用户登录（账号密码）
     * 支持邮箱+密码 和 手机号+密码 两种方式登录
     * account 字段（LoginRequest.email）同时兼容邮箱或手机号输入
     */
    @Override
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String account = request.getEmail();
        String password = request.getPassword();

        // 1. 账号不能为空
        if (account == null || account.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "邮箱或手机号不能为空 / Email or phone number is required");
        }
        account = account.trim();

        // 2. 密码不能为空
        if (password == null || password.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "密码不能为空 / Password is required");
        }

        // 3. 识别账号类型（邮箱 / 手机号）
        String accountType = PhoneUtil.detectTargetType(account);
        if (accountType == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "请输入有效的邮箱或手机号 / Please enter a valid email or phone number");
        }

        // 4. 按类型查找用户
        User user;
        if ("phone".equals(accountType)) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, account));
        } else {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, account));
        }
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "该账号未注册");
        }

        // 校验状态
        if (user.getStatus() == Constants.USER_STATUS_DISABLED) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (user.getStatus() == Constants.USER_STATUS_LOCKED) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 校验密码
        if (!SecurityUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 校验TOTP（如果已绑定）
        if (user.getTotpBound() == Constants.TOTP_BOUND) {
            if (request.getTotpCode() == null || request.getTotpCode().isEmpty()) {
                throw new BusinessException(ResultCode.TOTP_VERIFY_ERROR, "请输入TOTP验证码");
            }
            TotpConfig totpConfig = totpConfigMapper.selectOne(
                    new LambdaQueryWrapper<TotpConfig>().eq(TotpConfig::getUserId, user.getId()));
            if (totpConfig == null || !TotpUtil.verifyCode(totpConfig.getSecretKey(), request.getTotpCode())) {
                throw new BusinessException(ResultCode.TOTP_VERIFY_ERROR);
            }
        }

        return buildLoginResponse(user, ipAddress);
    }

    /**
     * 验证码登录（手机号/邮箱 + 验证码）
     */
    @Override
    public LoginResponse loginByCode(CodeLoginRequest request, String ipAddress, String userAgent) {
        String target = request.getTarget().trim();
        String verifyCode = request.getVerifyCode();

        // 自动识别目标类型
        String targetType = PhoneUtil.detectTargetType(target);
        if (targetType == null) {
            throw new BusinessException(ResultCode.TARGET_FORMAT_ERROR);
        }

        // 查找用户
        User user;
        if ("phone".equals(targetType)) {
            if (!PhoneUtil.isValidPhone(target)) {
                throw new BusinessException(ResultCode.PHONE_FORMAT_ERROR);
            }
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, target));
        } else {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, target));
        }

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "该账号未注册");
        }

        // 校验状态
        if (user.getStatus() == Constants.USER_STATUS_DISABLED) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (user.getStatus() == Constants.USER_STATUS_LOCKED) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 校验验证码
        verifyCode(target, verifyCode, Constants.CODE_TYPE_LOGIN);

        return buildLoginResponse(user, ipAddress);
    }

    /**
     * 构建登录响应（统一逻辑）
     */
    private LoginResponse buildLoginResponse(User user, String ipAddress) {
        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId());

        // 存入Redis（支持主动注销）
        String redisKey = Constants.REDIS_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(redisKey, token,
                properties.getJwt().getExpireHours(), TimeUnit.HOURS);

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setTotpBound(user.getTotpBound());
        response.setSecurityScore(user.getSecurityScore());
        response.setPlanId(user.getPlanId());

        auditLogService.log(user.getId(), Constants.AUDIT_MODULE_AUTH, "login",
                String.format("{\"ip\":\"%s\"}", ipAddress));
        return response;
    }

    /**
     * 用户登出
     */
    @Override
    public void logout(Long userId) {
        // 删除Redis中的Token
        String redisKey = Constants.REDIS_TOKEN_PREFIX + userId;
        redisTemplate.delete(redisKey);
        auditLogService.log(userId, Constants.AUDIT_MODULE_AUTH, "logout", null);
    }

    /**
     * 初始化TOTP绑定
     */
    @Override
    public TotpBindResponse initTotpBind(Long userId) {
        User user = userMapper.selectById(userId);
        if (user.getTotpBound() == Constants.TOTP_BOUND) {
            throw new BusinessException(ResultCode.TOTP_ALREADY_BOUND);
        }

        // 生成TOTP密钥
        String secret = TotpUtil.generateSecret();

        // 临时存入Redis（等待用户确认）
        String redisKey = Constants.REDIS_TOTP_TEMP_PREFIX + userId;
        redisTemplate.opsForValue().set(redisKey, secret, 10, TimeUnit.MINUTES);

        // 生成otpauth URI
        String emailForTotp = user.getEmail() != null ? user.getEmail() : (user.getPhone() != null ? user.getPhone() : String.valueOf(userId));
        String otpAuthUri = TotpUtil.getOtpAuthUri(
                properties.getTotp().getIssuer(), emailForTotp, secret);

        TotpBindResponse response = new TotpBindResponse();
        response.setSecret(secret);
        response.setOtpAuthUri(otpAuthUri);
        // 使用在线二维码API生成QR码URL
        response.setQrCodeUrl("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + otpAuthUri);

        log.info("TOTP绑定初始化 | userId={}", userId);
        return response;
    }

    /**
     * 确认TOTP绑定
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmTotpBind(Long userId, String totpCode) {
        // 从Redis获取临时密钥
        String redisKey = Constants.REDIS_TOTP_TEMP_PREFIX + userId;
        String secret = (String) redisTemplate.opsForValue().get(redisKey);
        if (secret == null) {
            throw new BusinessException("TOTP绑定已过期，请重新初始化");
        }

        // 验证TOTP码
        if (!TotpUtil.verifyCode(secret, totpCode)) {
            throw new BusinessException(ResultCode.TOTP_VERIFY_ERROR);
        }

        // 保存TOTP配置
        TotpConfig totpConfig = new TotpConfig();
        totpConfig.setUserId(userId);
        totpConfig.setSecretKey(secret);
        totpConfig.setIssuer(properties.getTotp().getIssuer());
        totpConfig.setDeviceType("app");
        totpConfig.setBoundAt(LocalDateTime.now());
        totpConfigMapper.insert(totpConfig);

        // 更新用户状态
        User user = userMapper.selectById(userId);
        user.setTotpBound(Constants.TOTP_BOUND);
        // 更新安全分（绑定TOTP +20分）
        user.setSecurityScore(Math.min(100, user.getSecurityScore() + 20));
        userMapper.updateById(user);

        // 删除Redis临时密钥
        redisTemplate.delete(redisKey);

        auditLogService.log(userId, Constants.AUDIT_MODULE_AUTH, "bind_totp", null);
        log.info("TOTP绑定成功 | userId={}", userId);
    }

    /**
     * 生成紧急恢复码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> generateRecoveryCodes(Long userId) {
        // 生成5个恢复码
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String plainCode = SecurityUtil.generateRecoveryCode();
            codes.add(plainCode);

            RecoveryCode rc = new RecoveryCode();
            rc.setUserId(userId);
            rc.setCodeHash(SecurityUtil.sha256(plainCode));
            rc.setIsUsed(0);
            recoveryCodeMapper.insert(rc);
        }

        auditLogService.log(userId, Constants.AUDIT_MODULE_AUTH, "generate_recovery_codes", null);
        log.info("紧急恢复码已生成 | userId={} | count={}", userId, codes.size());
        return codes;
    }

    /**
     * 使用恢复码（中止触发流程）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useRecoveryCode(Long userId, String recoveryCode) {
        String codeHash = SecurityUtil.sha256(recoveryCode);
        RecoveryCode rc = recoveryCodeMapper.selectOne(
                new LambdaQueryWrapper<RecoveryCode>()
                        .eq(RecoveryCode::getUserId, userId)
                        .eq(RecoveryCode::getCodeHash, codeHash)
                        .eq(RecoveryCode::getIsUsed, 0));

        if (rc == null) {
            throw new BusinessException(ResultCode.RECOVERY_CODE_ERROR);
        }

        // 标记已使用
        rc.setIsUsed(1);
        rc.setUsedAt(LocalDateTime.now());
        recoveryCodeMapper.updateById(rc);

        auditLogService.log(userId, Constants.AUDIT_MODULE_AUTH, "use_recovery_code", null);
        log.info("恢复码已使用 | userId={}", userId);
    }

    /**
     * 校验验证码（内部方法）
     */
    private void verifyCode(String target, String code, String codeType) {
        // 先从Redis快速校验
        String redisKey = Constants.REDIS_CODE_PREFIX + codeType + ":" + target;
        String cachedCode = (String) redisTemplate.opsForValue().get(redisKey);

        if (cachedCode != null) {
            if (!cachedCode.equals(code)) {
                throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
            }
            redisTemplate.delete(redisKey);
            return;
        }

        // Redis无缓存，查数据库
        VerificationCode vc = verificationCodeMapper.selectOne(
                new LambdaQueryWrapper<VerificationCode>()
                        .eq(VerificationCode::getTarget, target)
                        .eq(VerificationCode::getCodeType, codeType)
                        .eq(VerificationCode::getIsUsed, 0)
                        .gt(VerificationCode::getExpireAt, LocalDateTime.now())
                        .orderByDesc(VerificationCode::getCreatedAt)
                        .last("LIMIT 1"));

        if (vc == null) {
            throw new BusinessException(ResultCode.VERIFY_CODE_EXPIRED);
        }
        if (!vc.getCode().equals(code)) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }

        // 标记已使用
        vc.setIsUsed(1);
        verificationCodeMapper.updateById(vc);
    }
}
