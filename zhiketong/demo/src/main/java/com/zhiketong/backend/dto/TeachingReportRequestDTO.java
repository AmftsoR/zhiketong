package com.zhiketong.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeachingReportRequestDTO {
    private Long teacherId;
    private String className;
    private List<Long> studentIds;
    private List<Long> knowledgePointIds;
    private Boolean useAI = true;
}
