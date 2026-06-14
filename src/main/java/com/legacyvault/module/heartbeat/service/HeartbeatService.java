package com.legacyvault.module.heartbeat.service;

import com.legacyvault.module.heartbeat.dto.CheckInPeriodRequest;
import com.legacyvault.module.heartbeat.dto.CheckInRequest;
import com.legacyvault.module.heartbeat.dto.HeartbeatStatusResponse;
import com.legacyvault.module.heartbeat.dto.TravelModeRequest;

/**
 * 心跳打卡服务接口
 *
 * @author LegacyVault
 */
public interface HeartbeatService {

    /**
     * 获取心跳状态
     */
    HeartbeatStatusResponse getHeartbeatStatus(Long userId);

    /**
     * 执行心跳打卡
     */
    void checkIn(Long userId, CheckInRequest request, String ipAddress, String userAgent);

    /**
     * 设置打卡周期
     */
    void setCheckInPeriod(Long userId, CheckInPeriodRequest request);

    /**
     * 设置旅行模式
     */
    void setTravelMode(Long userId, TravelModeRequest request);

    /**
     * 关闭旅行模式
     */
    void disableTravelMode(Long userId);
}
