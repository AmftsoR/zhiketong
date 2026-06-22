package com.zhiketong.backend.dto;

import lombok.Data;

@Data
public class QuestionQuery {
    private Long knowledgePointId;
    private String type;
    private String difficulty;
    private String keyword;
    private Integer page = 1;
    private Integer size = 10;
}
