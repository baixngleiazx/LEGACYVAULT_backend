package com.legacyvault.module.trigger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.config.LegacyVaultProperties;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.mock.MockBlockchainService;
import com.legacyvault.mock.MockEmailService;
import com.legacyvault.mock.MockNotaryService;
import com.legacyvault.module.auth.entity.TotpConfig;
import com.legacyvault.module.auth.mapper.TotpConfigMapper;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.delivery.entity.DeliveryLink;
import com.legacyvault.module.delivery.mapper.DeliveryLinkMapper;
import com.legacyvault.module.heartbeat.entity.HeartbeatConfig;
import com.legacyvault.module.heartbeat.mapper.HeartbeatConfigMapper;
import com.legacyvault.module.trigger.dto.ContactReplyRequest;
import com.legacyvault.module.trigger.dto.TriggerProcessResponse;
import com.legacyvault.module.trigger.entity.ContactVerification;
import com.legacyvault.module.trigger.entity.TriggerProcess;
import com.legacyvault.module.trigger.entity.TriggerStageRecord;
import com.legacyvault.module.trigger.mapper.ContactVerificationMapper;
import com.legacyvault.module.trigger.mapper.TriggerProcessMapper;
import com.legacyvault.module.trigger.mapper.TriggerStageRecordMapper;
import com.legacyvault.module.trigger.service.TriggerService;
import com.legacyvault.module.user.entity.Heir;
import com.legacyvault.module.user.entity.TrustedContact;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.HeirMapper;
import com.legacyvault.module.user.mapper.TrustedContactMapper;
import com.legacyvault.module.user.mapper.UserMapper;
import com.legacyvault.util.JwtUtil;
import com.legacyvault.util.SecurityUtil;
import com.legacyvault.util.TotpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 触发验证引擎服务实现
 * 核心流程：超时告警(T+0) → 联系人核查(T+72h) → 公证人通知(T+96h) → 最终确认(T+120h)
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class TriggerServiceImpl implements TriggerService {

    @Autowired
    private TriggerProcessMapper triggerProcessMapper;

    @Autowired
    private TriggerStageRecordMapper stageRecordMapper;

    @Autowired
    private ContactVerificationMapper contactVerificationMapper;

    @Autowired
    private HeartbeatConfigMapper heartbeatConfigMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HeirMapper heirMapper;

    @Autowired
    private TrustedContactMapper trustedContactMapper;

    @Autowired
    private TotpConfigMapper totpConfigMapper;

    @Autowired
    private DeliveryLinkMapper deliveryLinkMapper;

    @Autowired
    private MockEmailService mockEmailService;

    @Autowired
    private com.legacyvault.mock.MockSmsService mockSmsServiceBean;

    @Autowired
    private MockNotaryService mockNotaryService;

    @Autowired
    private MockBlockchainService mockBlockchainService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LegacyVaultProperties properties;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public List<TriggerProcessResponse> listProcesses(Long userId) {
        List<TriggerProcess> processes = triggerProcessMapper.selectList(
                new LambdaQueryWrapper<TriggerProcess>()
                        .eq(TriggerProcess::getUserId, userId)
                        .orderByDesc(TriggerProcess::getCreatedAt));
        return processes.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public TriggerProcessResponse getLatestProcess(Long userId) {
        TriggerProcess process = triggerProcessMapper.selectOne(
                new LambdaQueryWrapper<TriggerProcess>()
                        .eq(TriggerProcess::getUserId, userId)
                        .orderByDesc(TriggerProcess::getCreatedAt)
                        .last("LIMIT 1"));
        if (process == null) {
            return null;
        }
        return toResponse(process);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void abortProcess(Long userId, String totpCode) {
        // 查找进行中的触发流程
        TriggerProcess process = findActiveProcess(userId);
        if (process == null) {
            throw new BusinessException(ResultCode.TRIGGER_PROCESS_NOT_FOUND);
        }

        // 验证TOTP
        TotpConfig totpConfig = totpConfigMapper.selectOne(
                new LambdaQueryWrapper<TotpConfig>().eq(TotpConfig::getUserId, userId));
        if (totpConfig != null && !TotpUtil.verifyCode(totpConfig.getSecretKey(), totpCode)) {
            throw new BusinessException(ResultCode.TOTP_VERIFY_ERROR);
        }

        // 中止流程
        abortProcessInternal(process, "user", "用户主动中止（密码+TOTP）");
        log.info("触发流程已中止 | userId={} | processId={}", userId, process.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void abortProcessByRecoveryCode(Long userId, String recoveryCode) {
        TriggerProcess process = findActiveProcess(userId);
        if (process == null) {
            throw new BusinessException(ResultCode.TRIGGER_PROCESS_NOT_FOUND);
        }
        abortProcessInternal(process, "recovery_code", "恢复码中止");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyContactVerification(ContactReplyRequest request) {
        ContactVerification verification = contactVerificationMapper.selectById(request.getVerificationId());
        if (verification == null) {
            throw new BusinessException("核查记录不存在");
        }

        verification.setVerificationStatus(request.getVerificationStatus());
        verification.setReplyAt(LocalDateTime.now());
        verification.setReplyChannel(request.getReplyChannel());
        contactVerificationMapper.updateById(verification);

        // 如果回复"确认活跃"（状态2），立即中止触发流程
        if (request.getVerificationStatus() == 2) {
            TriggerProcess process = triggerProcessMapper.selectById(verification.getTriggerProcessId());
            if (process != null && !Constants.TRIGGER_STATUS_ABORTED.equals(process.getStatus())
                    && !Constants.TRIGGER_STATUS_COMPLETED.equals(process.getStatus())) {
                abortProcessInternal(process, "contact", "可信联系人确认用户活跃");
            }
        }
    }

    /**
     * 定时任务：每小时检查超时用户
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Override
    public void checkExpiredHeartbeats() {
        log.info("【定时任务】开始检查超时心跳...");

        // 查找所有心跳超期且没有进行中触发流程的用户
        List<HeartbeatConfig> expiredConfigs = heartbeatConfigMapper.selectList(
                new LambdaQueryWrapper<HeartbeatConfig>()
                        .lt(HeartbeatConfig::getNextDeadline, LocalDateTime.now()));

        for (HeartbeatConfig config : expiredConfigs) {
            // 检查是否已有进行中的触发流程
            Long activeCount = triggerProcessMapper.selectCount(
                    new LambdaQueryWrapper<TriggerProcess>()
                            .eq(TriggerProcess::getUserId, config.getUserId())
                            .in(TriggerProcess::getStatus,
                                    Constants.TRIGGER_STATUS_T0_ALERT,
                                    Constants.TRIGGER_STATUS_T72_CONTACT_CHECK,
                                    Constants.TRIGGER_STATUS_T96_NOTARY,
                                    Constants.TRIGGER_STATUS_T120_FINAL));
            if (activeCount > 0) {
                continue; // 已有进行中的流程
            }

            // 创建T+0超时告警触发流程
            TriggerProcess process = new TriggerProcess();
            process.setUserId(config.getUserId());
            process.setHeartbeatConfigId(config.getId());
            process.setStatus(Constants.TRIGGER_STATUS_T0_ALERT);
            process.setGracePeriodStart(LocalDateTime.now());
            triggerProcessMapper.insert(process);

            // 记录阶段
            saveStageRecord(process.getId(), "T0", "超时告警", "SUCCESS",
                    "用户心跳超时，进入72小时宽限期");

            // 发送全渠道告警
            sendAlertToUser(config.getUserId());

            log.info("触发流程已创建 | userId={} | processId={}", config.getUserId(), process.getId());
        }
    }

    /**
     * 定时任务：每6小时推进触发流程
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    @Override
    public void advanceTriggerProcesses() {
        log.info("【定时任务】开始推进触发流程...");

        // T+0 → T+72h（宽限期结束，开始联系人核查）
        advanceToContactCheck();
        // T+72h → T+96h（公证人通知）
        advanceToNotaryNotify();
        // T+96h → T+120h（最终确认，启动交付）
        advanceToFinalConfirm();
    }

    // ==================== 内部方法 ====================

    /**
     * 查找进行中的触发流程
     */
    private TriggerProcess findActiveProcess(Long userId) {
        return triggerProcessMapper.selectOne(
                new LambdaQueryWrapper<TriggerProcess>()
                        .eq(TriggerProcess::getUserId, userId)
                        .in(TriggerProcess::getStatus,
                                Constants.TRIGGER_STATUS_T0_ALERT,
                                Constants.TRIGGER_STATUS_T72_CONTACT_CHECK,
                                Constants.TRIGGER_STATUS_T96_NOTARY,
                                Constants.TRIGGER_STATUS_T120_FINAL)
                        .orderByDesc(TriggerProcess::getCreatedAt)
                        .last("LIMIT 1"));
    }

    /**
     * 内部中止流程
     */
    private void abortProcessInternal(TriggerProcess process, String abortBy, String reason) {
        process.setStatus(Constants.TRIGGER_STATUS_ABORTED);
        process.setAbortedAt(LocalDateTime.now());
        process.setAbortBy(abortBy);
        process.setAbortReason(reason);
        triggerProcessMapper.updateById(process);

        saveStageRecord(process.getId(), "ABORT", "流程中止", "SUCCESS", reason);

        // 区块链存证
        Map<String, Object> chainResult = mockBlockchainService.storeOnChain(
                process.getUserId(), "trigger_abort", SecurityUtil.sha256(process.getId().toString()));
        process.setBlockchainTxHash((String) chainResult.get("txHash"));
        triggerProcessMapper.updateById(process);

        auditLogService.log(process.getUserId(), Constants.AUDIT_MODULE_TRIGGER, "abort_process",
                String.format("{\"processId\":%d,\"reason\":\"%s\"}", process.getId(), reason));
    }

    /**
     * 保存阶段记录
     */
    private void saveStageRecord(Long processId, String stage, String action, String result, String detail) {
        TriggerStageRecord record = new TriggerStageRecord();
        record.setTriggerProcessId(processId);
        record.setStage(stage);
        record.setAction(action);
        record.setResult(result);
        record.setDetail(detail);
        stageRecordMapper.insert(record);
    }

    /**
     * 向用户发送全渠道告警
     */
    private void sendAlertToUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return;
        mockEmailService.sendTriggerAlert(user.getEmail(), user.getNickname());
        if (user.getPhone() != null) {
            mockSmsServiceBean.sendTriggerAlert(user.getPhone(), user.getNickname());
        }
    }

    /**
     * 推进到联系人核查阶段（T+72h）
     */
    private void advanceToContactCheck() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(properties.getHeartbeat().getGracePeriodHours());
        List<TriggerProcess> processes = triggerProcessMapper.selectList(
                new LambdaQueryWrapper<TriggerProcess>()
                        .eq(TriggerProcess::getStatus, Constants.TRIGGER_STATUS_T0_ALERT)
                        .le(TriggerProcess::getGracePeriodStart, threshold));

        for (TriggerProcess process : processes) {
            process.setStatus(Constants.TRIGGER_STATUS_T72_CONTACT_CHECK);
            process.setContactCheckStart(LocalDateTime.now());
            triggerProcessMapper.updateById(process);

            // 向可信联系人发送核查请求
            List<TrustedContact> contacts = trustedContactMapper.selectList(
                    new LambdaQueryWrapper<TrustedContact>().eq(TrustedContact::getUserId, process.getUserId()));
            for (TrustedContact contact : contacts) {
                ContactVerification cv = new ContactVerification();
                cv.setTriggerProcessId(process.getId());
                cv.setTrustedContactId(contact.getId());
                cv.setVerificationStatus(0); // 待回复
                contactVerificationMapper.insert(cv);

                // 发送核查邮件
                String verifyUrl = "/trigger/contact-reply?id=" + cv.getId();
                mockEmailService.sendContactVerification(contact.getEmail(), "用户", verifyUrl);
            }

            saveStageRecord(process.getId(), "T72", "联系人核查", "SUCCESS",
                    "已通知" + contacts.size() + "名可信联系人");
            log.info("触发流程进入联系人核查阶段 | processId={}", process.getId());
        }
    }

    /**
     * 推进到公证人通知阶段（T+96h）
     */
    private void advanceToNotaryNotify() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(properties.getTrigger().getContactCheckHours());
        List<TriggerProcess> processes = triggerProcessMapper.selectList(
                new LambdaQueryWrapper<TriggerProcess>()
                        .eq(TriggerProcess::getStatus, Constants.TRIGGER_STATUS_T72_CONTACT_CHECK)
                        .le(TriggerProcess::getContactCheckStart, threshold));

        for (TriggerProcess process : processes) {
            // 检查是否已有足够联系人确认失联
            Long confirmCount = contactVerificationMapper.selectCount(
                    new LambdaQueryWrapper<ContactVerification>()
                            .eq(ContactVerification::getTriggerProcessId, process.getId())
                            .eq(ContactVerification::getVerificationStatus, 1));
            if (confirmCount < properties.getTrigger().getMinConfirmCount()) {
                log.info("联系人确认失联数量不足 | processId={} | 需要={} | 实际={}",
                        process.getId(), properties.getTrigger().getMinConfirmCount(), confirmCount);
                continue;
            }

            process.setStatus(Constants.TRIGGER_STATUS_T96_NOTARY);
            process.setNotaryNotifyAt(LocalDateTime.now());
            triggerProcessMapper.updateById(process);

            // 通知公证人
            mockNotaryService.notifyNotary(process.getUserId(), process.getId());

            saveStageRecord(process.getId(), "T96", "公证人通知", "SUCCESS", "已通知公证机构");
            log.info("触发流程进入公证人通知阶段 | processId={}", process.getId());
        }
    }

    /**
     * 推进到最终确认阶段（T+120h），生成继承交付链接
     */
    private void advanceToFinalConfirm() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(properties.getTrigger().getNotaryNotifyHours());
        List<TriggerProcess> processes = triggerProcessMapper.selectList(
                new LambdaQueryWrapper<TriggerProcess>()
                        .eq(TriggerProcess::getStatus, Constants.TRIGGER_STATUS_T96_NOTARY)
                        .le(TriggerProcess::getNotaryNotifyAt, threshold));

        for (TriggerProcess process : processes) {
            process.setStatus(Constants.TRIGGER_STATUS_T120_FINAL);
            process.setFinalConfirmAt(LocalDateTime.now());
            triggerProcessMapper.updateById(process);

            // 区块链存证触发事件
            Map<String, Object> chainResult = mockBlockchainService.storeOnChain(
                    process.getUserId(), "trigger_final_confirm",
                    SecurityUtil.sha256(process.getId().toString()));
            process.setBlockchainTxHash((String) chainResult.get("txHash"));
            triggerProcessMapper.updateById(process);

            // 为每个继承人生成一次性交付链接
            List<Heir> heirs = heirMapper.selectList(
                    new LambdaQueryWrapper<Heir>()
                            .eq(Heir::getUserId, process.getUserId())
                            .eq(Heir::getConfirmationStatus, Constants.HEIR_STATUS_CONFIRMED));

            for (Heir heir : heirs) {
                String linkToken = SecurityUtil.generateToken();
                String jwtToken = jwtUtil.generateDeliveryToken(0L, heir.getId());

                DeliveryLink link = new DeliveryLink();
                link.setUserId(process.getUserId());
                link.setHeirId(heir.getId());
                link.setTriggerProcessId(process.getId());
                link.setJwtToken(jwtToken);
                link.setLinkToken(linkToken);
                link.setStatus(Constants.LINK_STATUS_VALID);
                link.setExpiresAt(LocalDateTime.now().plusDays(properties.getDelivery().getLinkExpireDays()));
                link.setFailCount(0);
                link.setMaxFailCount(properties.getDelivery().getMaxFailCount());
                deliveryLinkMapper.insert(link);

                // 发送交付链接邮件
                String deliveryUrl = "/delivery/verify?token=" + linkToken;
                mockEmailService.sendDeliveryLink(heir.getEmail(), deliveryUrl, "用户");
            }

            // 标记流程完成
            process.setStatus(Constants.TRIGGER_STATUS_COMPLETED);
            process.setCompletedAt(LocalDateTime.now());
            triggerProcessMapper.updateById(process);

            saveStageRecord(process.getId(), "T120", "最终确认", "SUCCESS",
                    "已生成" + heirs.size() + "个交付链接");
            log.info("触发流程完成 | processId={} | 继承人数={}", process.getId(), heirs.size());
        }
    }

    /**
     * 实体转响应VO
     */
    private TriggerProcessResponse toResponse(TriggerProcess process) {
        TriggerProcessResponse response = new TriggerProcessResponse();
        response.setId(process.getId());
        response.setStatus(process.getStatus());

        String[] statusTexts = {
                "待触发", "超时告警(宽限期)", "联系人核查", "公证人通知", "最终确认", "已完成", "已中止"
        };
        String[] statusKeys = {
                Constants.TRIGGER_STATUS_PENDING, Constants.TRIGGER_STATUS_T0_ALERT,
                Constants.TRIGGER_STATUS_T72_CONTACT_CHECK, Constants.TRIGGER_STATUS_T96_NOTARY,
                Constants.TRIGGER_STATUS_T120_FINAL, Constants.TRIGGER_STATUS_COMPLETED,
                Constants.TRIGGER_STATUS_ABORTED
        };
        for (int i = 0; i < statusKeys.length; i++) {
            if (statusKeys[i].equals(process.getStatus())) {
                response.setStatusText(statusTexts[i]);
                break;
            }
        }

        response.setGracePeriodStart(process.getGracePeriodStart());
        response.setContactCheckStart(process.getContactCheckStart());
        response.setNotaryNotifyAt(process.getNotaryNotifyAt());
        response.setFinalConfirmAt(process.getFinalConfirmAt());
        response.setCompletedAt(process.getCompletedAt());
        response.setAbortedAt(process.getAbortedAt());
        response.setAbortReason(process.getAbortReason());
        response.setBlockchainTxHash(process.getBlockchainTxHash());
        response.setCreatedAt(process.getCreatedAt());
        return response;
    }

}
