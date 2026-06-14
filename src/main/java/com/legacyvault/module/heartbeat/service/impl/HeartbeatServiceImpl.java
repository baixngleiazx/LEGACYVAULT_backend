package com.legacyvault.module.heartbeat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.module.auth.entity.TotpConfig;
import com.legacyvault.module.auth.mapper.TotpConfigMapper;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.heartbeat.dto.*;
import com.legacyvault.module.heartbeat.entity.HeartbeatConfig;
import com.legacyvault.module.heartbeat.entity.HeartbeatRecord;
import com.legacyvault.module.heartbeat.mapper.HeartbeatConfigMapper;
import com.legacyvault.module.heartbeat.mapper.HeartbeatRecordMapper;
import com.legacyvault.module.heartbeat.service.HeartbeatService;
import com.legacyvault.util.TotpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 心跳打卡服务实现
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class HeartbeatServiceImpl implements HeartbeatService {

    @Autowired
    private HeartbeatConfigMapper heartbeatConfigMapper;

    @Autowired
    private HeartbeatRecordMapper heartbeatRecordMapper;

    @Autowired
    private TotpConfigMapper totpConfigMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public HeartbeatStatusResponse getHeartbeatStatus(Long userId) {
        HeartbeatConfig config = heartbeatConfigMapper.selectOne(
                new LambdaQueryWrapper<HeartbeatConfig>().eq(HeartbeatConfig::getUserId, userId));
        if (config == null) {
            throw new BusinessException(ResultCode.HEARTBEAT_CONFIG_NOT_FOUND);
        }

        HeartbeatStatusResponse response = new HeartbeatStatusResponse();
        response.setCheckInPeriodDays(config.getCheckInPeriodDays());
        response.setNextDeadline(config.getNextDeadline());
        response.setLastCheckInAt(config.getLastCheckInAt());

        // 计算剩余天数和状态颜色
        if (config.getNextDeadline() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), config.getNextDeadline().toLocalDate());
            response.setDaysRemaining((int) daysRemaining);
            if (daysRemaining > 14) {
                response.setStatusColor("green");
            } else if (daysRemaining > 0) {
                response.setStatusColor("yellow");
            } else {
                response.setStatusColor("red");
            }
        }

        response.setTravelModeEnabled(config.getNextDeadline() != null);
        response.setTravelStartDate(config.getNextDeadline()); // 简化处理
        response.setTravelEndDate(config.getNextDeadline());

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long userId, CheckInRequest request, String ipAddress, String userAgent) {
        HeartbeatConfig config = heartbeatConfigMapper.selectOne(
                new LambdaQueryWrapper<HeartbeatConfig>().eq(HeartbeatConfig::getUserId, userId));
        if (config == null) {
            throw new BusinessException(ResultCode.HEARTBEAT_CONFIG_NOT_FOUND);
        }

        // 验证TOTP
        TotpConfig totpConfig = totpConfigMapper.selectOne(
                new LambdaQueryWrapper<TotpConfig>().eq(TotpConfig::getUserId, userId));
        if (totpConfig == null || !TotpUtil.verifyCode(totpConfig.getSecretKey(), request.getTotpCode())) {
            throw new BusinessException(ResultCode.TOTP_VERIFY_ERROR);
        }

        // 记录打卡
        HeartbeatRecord record = new HeartbeatRecord();
        record.setUserId(userId);
        record.setCheckInAt(LocalDateTime.now());
        record.setCheckInType("web");
        record.setIpAddress(ipAddress);
        record.setUserAgent(userAgent);
        record.setTotpVerified(1);
        heartbeatRecordMapper.insert(record);

        // 更新心跳配置：重置截止日期，清除提醒标记
        config.setLastCheckInAt(LocalDateTime.now());
        config.setNextDeadline(LocalDateTime.now().plusDays(config.getCheckInPeriodDays()));
        config.setRemind14dSent(0);
        config.setRemind7dSent(0);
        config.setRemind3dSent(0);
        config.setRemind0dSent(0);
        heartbeatConfigMapper.updateById(config);

        auditLogService.log(userId, Constants.AUDIT_MODULE_HEARTBEAT, "check_in",
                String.format("{\"ip\":\"%s\"}", ipAddress));
        log.info("心跳打卡成功 | userId={} | 下次截止={}", userId, config.getNextDeadline());
    }

    @Override
    public void setCheckInPeriod(Long userId, CheckInPeriodRequest request) {
        HeartbeatConfig config = heartbeatConfigMapper.selectOne(
                new LambdaQueryWrapper<HeartbeatConfig>().eq(HeartbeatConfig::getUserId, userId));
        if (config == null) {
            throw new BusinessException(ResultCode.HEARTBEAT_CONFIG_NOT_FOUND);
        }

        config.setCheckInPeriodDays(request.getPeriodDays());
        // 重新计算截止日期
        LocalDateTime base = config.getLastCheckInAt() != null ? config.getLastCheckInAt() : LocalDateTime.now();
        config.setNextDeadline(base.plusDays(request.getPeriodDays()));
        heartbeatConfigMapper.updateById(config);

        auditLogService.log(userId, Constants.AUDIT_MODULE_HEARTBEAT, "set_period",
                String.format("{\"periodDays\":%d}", request.getPeriodDays()));
    }

    @Override
    public void setTravelMode(Long userId, TravelModeRequest request) {
        // 验证TOTP
        TotpConfig totpConfig = totpConfigMapper.selectOne(
                new LambdaQueryWrapper<TotpConfig>().eq(TotpConfig::getUserId, userId));
        if (totpConfig == null || !TotpUtil.verifyCode(totpConfig.getSecretKey(), request.getTotpCode())) {
            throw new BusinessException(ResultCode.TOTP_VERIFY_ERROR);
        }

        HeartbeatConfig config = heartbeatConfigMapper.selectOne(
                new LambdaQueryWrapper<HeartbeatConfig>().eq(HeartbeatConfig::getUserId, userId));
        if (config == null) {
            throw new BusinessException(ResultCode.HEARTBEAT_CONFIG_NOT_FOUND);
        }

        // 解析日期并校验最长180天
        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());
        if (ChronoUnit.DAYS.between(startDate, endDate) > 180) {
            throw new BusinessException(ResultCode.TRAVEL_MODE_CONFLICT, "旅行模式最长180天");
        }

        // 更新心跳配置中的旅行模式字段
        // 简化处理：将截止日期设置为旅行结束日期
        config.setNextDeadline(endDate.atStartOfDay());
        heartbeatConfigMapper.updateById(config);

        auditLogService.log(userId, Constants.AUDIT_MODULE_HEARTBEAT, "enable_travel_mode",
                String.format("{\"start\":\"%s\",\"end\":\"%s\"}", startDate, endDate));
        log.info("旅行模式已开启 | userId={} | {} ~ {}", userId, startDate, endDate);
    }

    @Override
    public void disableTravelMode(Long userId) {
        HeartbeatConfig config = heartbeatConfigMapper.selectOne(
                new LambdaQueryWrapper<HeartbeatConfig>().eq(HeartbeatConfig::getUserId, userId));
        if (config == null) return;

        // 重置截止日期
        LocalDateTime base = config.getLastCheckInAt() != null ? config.getLastCheckInAt() : LocalDateTime.now();
        config.setNextDeadline(base.plusDays(config.getCheckInPeriodDays()));
        heartbeatConfigMapper.updateById(config);

        auditLogService.log(userId, Constants.AUDIT_MODULE_HEARTBEAT, "disable_travel_mode", null);
    }
}
