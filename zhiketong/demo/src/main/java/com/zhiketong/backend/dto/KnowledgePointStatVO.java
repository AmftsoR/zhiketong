package com.zhiketong.backend.dto;

import lombok.Data;

@Data
public class KnowledgePointStatVO {
    private Long knowledgePointId;
    private String knowledgePointName;
    private Integer totalCount;
    private Integer wrongCount;
    private Double accuracyRate;
}
