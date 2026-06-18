package com.zhiketong.backend.dto;

import com.zhiketong.backend.entity.Question;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExercisePushVO {
    private Long userId;
    private List<Long> weakKnowledgePointIds;
    private Map<String, Integer> difficultyDistribution;
    private List<Question> questions;
}
