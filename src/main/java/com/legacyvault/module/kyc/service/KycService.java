package com.legacyvault.module.kyc.service;

import com.legacyvault.common.PageResult;
import com.legacyvault.module.kyc.dto.KycRecordVo;
import com.legacyvault.module.kyc.dto.KycStatusResponse;
import com.legacyvault.module.kyc.dto.KycSubmitRequest;

/**
 * KYC 服务接口
 *
 * @author LegacyVault
 */
public interface KycService {

    /**
     * 用户提交 KYC 材料
     */
    void submit(Long userId, KycSubmitRequest request);

    /**
     * 查询当前用户 KYC 状态
     */
    KycStatusResponse getStatus(Long userId);

    /**
     * 管理员：分页查询待人工审核的 KYC 单据
     */
    PageResult<KycRecordVo> listPending(int page, int size);

    /**
     * 管理员：通过审核
     */
    void approve(Long recordId, Long reviewerId);

    /**
     * 管理员：驳回审核
     */
    void reject(Long recordId, Long reviewerId, String rejectReason);

    /**
     * 管理员：全量分页查询 KYC 单据
     */
    PageResult<KycRecordVo> listAll(int page, int size, Integer status);
}
