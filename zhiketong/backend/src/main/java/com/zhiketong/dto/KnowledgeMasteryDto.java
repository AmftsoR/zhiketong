package com.zhiketong.dto;

import lombok.Data;

/**
 * 知识点掌握度 DTO
 */
@Data
public class KnowledgeMasteryDto {
    private Long knowledgeId;
    private String knowledgeName;
    /** 正确率 0.0 ~ 1.0 */
    private Double correctRate;
    /** green / yellow / red */
    private String color;
}
