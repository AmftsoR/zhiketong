package com.zhiketong.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExercisePushRequestDTO {
    private Long userId;
    private List<Long> knowledgePointIds;
    private Integer totalCount = 10;
    private Integer easyCount;
    private Integer mediumCount;
    private Integer hardCount;
}
