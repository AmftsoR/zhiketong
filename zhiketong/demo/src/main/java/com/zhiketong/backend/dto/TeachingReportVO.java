package com.zhiketong.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TeachingReportVO {
    private String title;
    private String className;
    private LocalDateTime generatedAt;
    private String summary;
    private Boolean generatedByAi;
    private String aiAnalysis;
    private List<KnowledgePointStatVO> knowledgePointStats = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
}
