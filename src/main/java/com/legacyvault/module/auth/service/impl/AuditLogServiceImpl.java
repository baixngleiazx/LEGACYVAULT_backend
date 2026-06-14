package com.legacyvault.module.auth.service.impl;

import com.legacyvault.module.auth.entity.AuditLog;
import com.legacyvault.module.auth.mapper.AuditLogMapper;
import com.legacyvault.module.auth.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 审计日志服务实现
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    public void log(Long userId, String module, String action, String targetType, Long targetId, String detail, String ipAddress, String userAgent) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setModule(module);
        auditLog.setAction(action);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setDetail(detail);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLogMapper.insert(auditLog);
        log.debug("审计日志 | 用户={} | 模块={} | 动作={}", userId, module, action);
    }

    @Override
    public void log(Long userId, String module, String action, String detail) {
        String ip = "";
        String ua = "";
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                ip = request.getHeader("X-Real-IP");
                if (ip == null) ip = request.getRemoteAddr();
                ua = request.getHeader("User-Agent");
            }
        } catch (Exception ignored) {}
        log(userId, module, action, null, null, detail, ip, ua);
    }
}
