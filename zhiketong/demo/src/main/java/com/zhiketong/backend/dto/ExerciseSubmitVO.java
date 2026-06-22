package com.zhiketong.backend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExerciseSubmitVO {
    private int totalCount;
    private int correctCount;
    private int wrongCount;
    private double score;
    private List<AnswerResult> details = new ArrayList<>();

    @Data
    public static class AnswerResult {
        private Long questionId;
        private String userAnswer;
        private String correctAnswer;
        private Boolean correct;
        private String explanation;
        private Boolean addedToMistakeBook;
    }
}
