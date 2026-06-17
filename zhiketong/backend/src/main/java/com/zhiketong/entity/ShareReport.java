package com.zhiketong.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报告分享记录
 */
@Data
@TableName("share_report")
public class ShareReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String token;
    private Long userId;
    private String reportData;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
