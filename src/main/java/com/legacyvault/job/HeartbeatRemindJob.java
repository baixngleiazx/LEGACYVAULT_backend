package com.legacyvault.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.mock.MockEmailService;
import com.legacyvault.mock.MockSmsService;
import com.legacyvault.module.heartbeat.entity.HeartbeatConfig;
import com.legacyvault.module.heartbeat.entity.RemindRecord;
import com.legacyvault.module.heartbeat.mapper.HeartbeatConfigMapper;
import com.legacyvault.module.heartbeat.mapper.RemindRecordMapper;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 心跳提醒定时任务
 * 按照提醒策略（T-14/T-7/T-3/T+0）自动发送提醒
 *
 * @author LegacyVault
 */
@Slf4j
@Component
public class HeartbeatRemindJob {

    @Autowired
    private HeartbeatConfigMapper heartbeatConfigMapper;

    @Autowired
    private RemindRecordMapper remindRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MockEmailService mockEmailService;

    @Autowired
    private MockSmsService mockSmsService;

    /**
     * 每天早上9点执行提醒检查
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkAndSendReminders() {
        log.info("【心跳提醒任务】开始检查...");

        List<HeartbeatConfig> configs = heartbeatConfigMapper.selectList(null);
        int reminderCount = 0;

        for (HeartbeatConfig config : configs) {
            if (config.getNextDeadline() == null) continue;

            long daysRemaining = ChronoUnit.DAYS.between(
                    LocalDateTime.now().toLocalDate(),
                    config.getNextDeadline().toLocalDate());

            User user = userMapper.selectById(config.getUserId());
            if (user == null) continue;

            // T-14天：邮件提醒
            if (daysRemaining == 14 && config.getRemind14dSent() == 0) {
                sendReminder(config, user, "remind_14d", Constants.CHANNEL_EMAIL);
                config.setRemind14dSent(1);
                heartbeatConfigMapper.updateById(config);
                reminderCount++;
            }

            // T-7天：邮件+短信+App推送（每天1次）
            if (daysRemaining <= 7 && daysRemaining > 3 && config.getRemind7dSent() == 0) {
                sendReminder(config, user, "remind_7d", Constants.CHANNEL_EMAIL);
                sendReminder(config, user, "remind_7d", Constants.CHANNEL_SMS);
                config.setRemind7dSent(1);
                heartbeatConfigMapper.updateById(config);
                reminderCount++;
            }

            // T-3天：邮件+短信+电话（每天2次）
            if (daysRemaining <= 3 && daysRemaining > 0 && config.getRemind3dSent() == 0) {
                sendReminder(config, user, "remind_3d", Constants.CHANNEL_EMAIL);
                sendReminder(config, user, "remind_3d", Constants.CHANNEL_SMS);
                config.setRemind3dSent(1);
                heartbeatConfigMapper.updateById(config);
                reminderCount++;
            }

            // T+0（超时）：所有渠道+紧急联系人
            if (daysRemaining <= 0 && config.getRemind0dSent() == 0) {
                sendReminder(config, user, "remind_0d", Constants.CHANNEL_EMAIL);
                sendReminder(config, user, "remind_0d", Constants.CHANNEL_SMS);
                config.setRemind0dSent(1);
                heartbeatConfigMapper.updateById(config);
                reminderCount++;
            }
        }

        log.info("【心跳提醒任务】完成 | 发送提醒数={}", reminderCount);
    }

    /**
     * 发送单条提醒
     */
    private void sendReminder(HeartbeatConfig config, User user, String remindType, String channel) {
        String deadline = config.getNextDeadline().toString();

        // 记录提醒
        RemindRecord record = new RemindRecord();
        record.setUserId(user.getId());
        record.setHeartbeatConfigId(config.getId());
        record.setRemindType(remindType);
        record.setChannel(channel);
        record.setSendStatus(1); // 已发送
        record.setSendAt(LocalDateTime.now());
        record.setMockData(String.format("{\"mock\":true,\"channel\":\"%s\"}", channel));

        // 根据渠道发送
        if (Constants.CHANNEL_EMAIL.equals(channel)) {
            record.setTarget(user.getEmail());
            mockEmailService.sendHeartbeatRemind(user.getEmail(), user.getNickname(), deadline);
        } else if (Constants.CHANNEL_SMS.equals(channel) && user.getPhone() != null) {
            record.setTarget(user.getPhone());
            mockSmsService.sendHeartbeatRemind(user.getPhone(), user.getNickname(), deadline);
        } else {
            record.setTarget(user.getEmail());
            record.setSendStatus(0); // 待发送
        }

        remindRecordMapper.insert(record);
    }
}
