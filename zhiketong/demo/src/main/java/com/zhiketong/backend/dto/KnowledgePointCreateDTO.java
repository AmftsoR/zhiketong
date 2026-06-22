package com.zhiketong.backend.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class KnowledgePointCreateDTO {
    private Long parentId;
    @NotBlank(message = "名称不能为空")
    private String name;
    @NotBlank(message = "学科不能为空")
    private String subject;
}
