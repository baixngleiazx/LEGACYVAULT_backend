package com.legacyvault.module.heartbeat.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.heartbeat.dto.*;
import com.legacyvault.module.heartbeat.service.HeartbeatService;
import com.legacyvault.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 心跳打卡控制器
 *
 * @author LegacyVault
 */
@RestController
@RequestMapping("/heartbeat")
public class HeartbeatController {

    @Autowired
    private HeartbeatService heartbeatService;

    /**
     * 获取心跳状态
     * GET /api/heartbeat/status
     */
    @GetMapping("/status")
    public Result<HeartbeatStatusResponse> getStatus(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(heartbeatService.getHeartbeatStatus(userId));
    }

    /**
     * 执行心跳打卡
     * POST /api/heartbeat/check-in
     */
    @PostMapping("/check-in")
    public Result<String> checkIn(@Valid @RequestBody CheckInRequest request, HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        String ip = RequestUtil.getIpAddress(httpRequest);
        String ua = RequestUtil.getUserAgent(httpRequest);
        heartbeatService.checkIn(userId, request, ip, ua);
        return Result.success("打卡成功！您仍然活跃。");
    }

    /**
     * 设置打卡周期
     * PUT /api/heartbeat/period
     */
    @PutMapping("/period")
    public Result<String> setPeriod(@Valid @RequestBody CheckInPeriodRequest request, HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        heartbeatService.setCheckInPeriod(userId, request);
        return Result.success("打卡周期已更新");
    }

    /**
     * 开启旅行模式
     * POST /api/heartbeat/travel-mode
     */
    @PostMapping("/travel-mode")
    public Result<String> enableTravelMode(@Valid @RequestBody TravelModeRequest request, HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        heartbeatService.setTravelMode(userId, request);
        return Result.success("旅行模式已开启");
    }

    /**
     * 关闭旅行模式
     * DELETE /api/heartbeat/travel-mode
     */
    @DeleteMapping("/travel-mode")
    public Result<String> disableTravelMode(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        heartbeatService.disableTravelMode(userId);
        return Result.success("旅行模式已关闭");
    }
}
