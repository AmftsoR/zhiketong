package com.zhiketong.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class KnowledgePointGenerateDTO {
    @NotBlank(message = "学科不能为空")
    private String subject;
    private String topic;
}
