package com.zhiketong.dto;

import lombok.Data;
import java.util.Map;

/**
 * 班级整体学情统计 DTO
 */
@Data
public class ClassStatisticsDto {
    private Long classId;
    private String className;
    /** 班级平均正确率 */
    private Double averageCorrectRate;
    /** 各知识点平均正确率（可选） */
    private Map<String, Double> knowledgeAvgRates;
}
