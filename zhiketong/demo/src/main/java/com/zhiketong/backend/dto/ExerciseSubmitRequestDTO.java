package com.zhiketong.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExerciseSubmitRequestDTO {
    private Long userId;
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private String userAnswer;
    }
}
