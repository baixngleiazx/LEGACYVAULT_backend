package com.legacyvault.module.user.dto;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 解锁门槛设置请求 DTO
 *
 * @author LegacyVault
 */
@Data
public class UnlockThresholdRequest {

    /** 解锁资产所需最低核验继承人数（至少 1） */
    @Min(value = 1, message = "最低解锁人数至少为 1")
    private Integer minHeirsToUnlock;
}
