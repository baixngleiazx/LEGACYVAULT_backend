package com.legacyvault.module.trigger.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 可信联系人核查回复请求DTO
 */
@Data
public class ContactReplyRequest {
    @NotNull(message = "核查记录ID不能为空")
    private Long verificationId;

    @NotNull(message = "回复状态不能为空")
    private Integer verificationStatus;  // 1-确认失联 2-确认活跃

    private String replyChannel;
}
